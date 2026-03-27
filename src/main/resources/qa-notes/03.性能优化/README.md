# 性能优化 Q&A

## 1. 排查CPU飙高，哪些代码是热点

CPU飙高定位热点代码，两步走：

第一步，找到高CPU线程：
```bash
# 找到Java进程中CPU最高的线程
top -Hp <pid>
# 把线程ID转16进制
printf '%x\n' <tid>
# 用jstack导出线程栈，搜这个16进制tid
jstack <pid> | grep <tid_hex> -A 30
```

第二步，用Arthas更方便：
```bash
# 一键看CPU最高的线程栈
thread -n 3
# 生成火焰图，直观看热点方法
profiler start
# 采样30秒后停止
profiler stop --format html
```

async-profiler生成火焰图是最直观的，横轴越宽的方法占CPU越多，一眼就能看出热点在哪。

注意区分：如果是用户线程CPU高，看业务代码死循环或正则回溯；如果是GC线程CPU高，那是GC问题不是代码热点。

## 2. 有大量请求，线程阻塞了，多个线程在运行，怎么确定到底哪一个线程出现问题

核心是找BLOCKED和WAITING状态的线程，看它们在等什么锁。

Arthas最快：
```bash
# 一键找到阻塞线程，-b参数专门找阻塞源
thread -b
# 查看所有线程状态分布
thread --state BLOCKED
thread --state WAITING
```

jstack方式：
```bash
# 导出线程栈
jstack <pid> > thread_dump.txt
# 搜阻塞线程，看waiting to lock和locked by
grep -A 20 "BLOCKED" thread_dump.txt
```

关键看三个信息：线程状态（BLOCKED/WAITING/TIMED_WAITING）、等待的锁对象地址、持有该锁的线程ID。jstack里会显示 `waiting to lock <0x...>` 和 `locked <0x...>`，顺着锁地址找到持有者就是问题线程。

多dump几次对比，如果某个线程一直持有锁不释放，那就是它。如果是死锁，jstack底部会直接打印 `Found one Java-level deadlock`。

## 3. 压测过程中，负载高，怎么看真实是哪个线程阻塞最终造成了性能瓶颈

压测场景下要多次采样对比，单次dump不够准。

连续采样定位：
```bash
# 连续dump 5次，每次间隔2秒
for i in {1..5}; do jstack <pid> > dump_$i.txt; sleep 2; done
# 对比多次dump，同一个线程如果一直卡在同一个位置，那就是瓶颈
```

Arthas精准定位：
```bash
# trace追踪方法耗时，找到最慢的调用
trace com.xxx.Service handleRequest '#cost > 200'
# monitor统计方法调用次数和平均RT
monitor -c 5 com.xxx.Service handleRequest
# 火焰图看整体热点，横轴宽的就是瓶颈
profiler start --event wall
profiler stop --format html
```

关键点：压测时用 `--event wall` 而不是默认的cpu事件，wall-clock模式会采样所有线程包括阻塞等待的，能看到线程到底在等什么（锁、IO、sleep）。cpu模式只采样运行中的线程，看不到阻塞。

再配合 `thread --state TIMED_WAITING,WAITING,BLOCKED` 看阻塞线程数量趋势，数量持续增长说明有资源瓶颈（连接池不够、锁竞争、下游慢）。

## 4. 假设线上CPU特别高，怎么定位问题？

分两种情况：业务线程CPU高还是GC线程CPU高。

先判断是谁吃的CPU：
```bash
# 找到Java进程
top -c | grep java
# 看进程内哪个线程CPU高
top -Hp <pid>
```

如果是业务线程CPU高：
```bash
# 线程ID转16进制
printf '%x\n' <tid>
# jstack找到对应线程栈
jstack <pid> | grep <tid_hex> -A 30
```
常见原因：死循环、正则回溯、大量序列化/反序列化、频繁fullGC触发的finalize。

如果是GC线程CPU高：
```bash
# 看GC频率和耗时
jstat -gcutil <pid> 1000
# GC太频繁说明内存不够或有内存泄漏
jmap -histo <pid> | head -30
```

Arthas一把梭：
```bash
# 直接看CPU最高的3个线程
thread -n 3
# dashboard实时看CPU、内存、GC全貌
dashboard
```

定位到具体方法后，review代码找根因。

## 5. 线上出现OOM问题的时候，怎么排查

先确保提前配好了自动dump参数，不然OOM时来不及抓现场：
```bash
# JVM启动参数必须加
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/tmp/heapdump.hprof
```

拿到dump文件后分析：
```bash
# MAT打开hprof文件，看Leak Suspects报告
# 重点看Dominator Tree，谁占内存最多一目了然
# Histogram看对象实例数量，异常多的就是嫌疑对象
```

没有dump文件的情况：
```bash
# 看对象分布，找数量异常多的类
jmap -histo:live <pid> | head -30
# Arthas在线看
heapdump /tmp/dump.hprof
dashboard  # 看堆内存使用趋势
```

常见OOM原因：大集合没清理（List/Map无限增长）、ThreadLocal没remove、连接池泄漏、大文件一次性加载到内存、Metaspace溢出（动态生成类太多）。

根据MAT的引用链（Path to GC Roots）找到谁持有了这些大对象不释放，就是根因。

## 6. 如果实际开发中有个定时任务跑批任务，数据量比较，线上出现了瞬时的CPU凸刺，怎么优化这个定时任务

核心思路是削峰，把集中的计算打散。

分批处理：
```bash
# 不要一次性查全量数据，分页处理
# 每批处理1000条，批次间sleep让出CPU
SELECT * FROM table WHERE id > lastId ORDER BY id LIMIT 1000
Thread.sleep(100); // 批次间歇
```

限流控速：用RateLimiter控制处理速率，比如每秒处理500条，避免瞬时打满CPU。
```java
RateLimiter limiter = RateLimiter.create(500);
for (Data d : dataList) {
    limiter.acquire();
    process(d);
}
```

时间打散：把定时任务拆成多个小任务分散到不同时间点执行，避免同一时刻集中触发。或者用分布式任务调度（XXL-JOB分片广播），多台机器分摊。

降低优先级：跑批线程池设低优先级，核心线程数控制在CPU核数的1/4，不跟业务线程抢资源。

异步化：如果跑批结果不需要实时，改成MQ驱动，消息慢慢消费，天然削峰。

## 7. A服务和B服务，分别压测的时候都是正常的；其中单独压测A服务，A服务会去调用B服务，发现这个时候B服务响应不好，可能的原因有哪些

单独压B没问题，A调B时B才慢，说明问题出在A到B的链路上或者流量模型不同。

连接池不够：A调B的HTTP/RPC连接池太小，高并发时连接排队等待。单独压B时用的是压测工具的连接，不经过A的连接池。检查A侧的连接池配置和等待队列长度。

序列化开销：A调B时有请求体序列化和响应体反序列化，单独压B可能用的是简单报文。大对象JSON序列化吃CPU和内存。

A侧线程池打满：A的业务线程池被压测请求占满，调B的请求在A侧就开始排队了，B收到请求时已经带了排队延迟。

流量模式不同：单独压B是均匀流量，A调B时可能有突发批量调用（比如一个A请求扇出调多次B），B瞬时并发比单独压测高。

网络问题：A和B之间的网络带宽、延迟、TCP连接复用。单独压B时压测机可能跟B同机房，A可能跨机房。

超时配置：A调B设了超时，B在高负载下RT抖动，触发A侧超时重试，重试又加大B的压力，恶性循环。

GC互相影响：A侧GC暂停导致对B的请求堆积，暂停结束后突发打到B。

## 8. 如果是B服务的代码有问题，怎么定位这个问题？

从外到内逐层定位：

全链路追踪先看大盘：
```bash
# SkyWalking/Zipkin看A调B的链路
# 看B侧的RT分布，是整体慢还是某些接口慢
# 看B的错误率、超时率变化趋势
```

Arthas在B服务上定位：
```bash
# trace追踪慢方法，找到耗时最长的调用链
trace com.xxx.BService handleRequest '#cost > 200'
# 逐层往下trace，定位到具体哪一行慢
trace com.xxx.Dao queryData '#cost > 100'
# watch看方法入参和返回值，确认是不是特定参数触发的慢
watch com.xxx.BService handleRequest '{params,returnObj,throwExp}' '#cost > 200'
```

如果是并发场景才出现：
```bash
# thread看B的线程状态，是否大量BLOCKED
thread -b
# monitor看方法并发调用量和平均RT
monitor -c 5 com.xxx.BService handleRequest
# profiler生成火焰图看热点
profiler start --event wall
profiler stop --format html
```

常见代码问题：慢SQL没走索引、锁粒度太大、同步调用外部服务、大对象序列化、循环内做IO操作。trace一层层往下钻就能找到。
