# Java算法 Q&A

## 1. 最长不重复子串input:"abcdeafh"out:"bcdeafh"

```java
import java.util.*;
import java.io.*;

/**
 * 最长不重复子串
 * input: "abcdeafh"
 * output: "bcdeafh"
 * 找到字符串中最长的不含重复字符的子串
 */
public class LongestNonRepeatingSubstring {

    public static String longestNonRepeating(String s) {
        if (s == null || s.isEmpty()) return "";
        // 记录字符最近出现的位置
        Map<Character, Integer> map = new HashMap<>();
        int left = 0, maxLen = 0, start = 0;
        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            // 如果字符重复且在窗口内，左指针跳到重复字符的下一位
            if (map.containsKey(c) && map.get(c) >= left) {
                left = map.get(c) + 1;
            }
            map.put(c, right);
            // 更新最长子串的起始位置和长度
            if (right - left + 1 > maxLen) {
                maxLen = right - left + 1;
                start = left;
            }
        }
        return s.substring(start, start + maxLen);
    }

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(new BufferedOutputStream(System.out));
        String s = br.readLine().trim();
        out.println(longestNonRepeating(s));
        out.flush();
        out.close();
    }
}
```

算法思想：滑动窗口 + HashMap。维护一个无重复字符的窗口 [left, right]，右指针扩展窗口，遇到重复字符时左指针跳到重复位置的下一位。

具体步骤：
1. HashMap存每个字符最近出现的索引
2. right 遍历字符串，如果当前字符在 map 中且位置 >= left，说明窗口内有重复，left 跳到 map.get(c) + 1
3. 每次更新 map，同时比较当前窗口长度是否为最大，记录起始位置
4. 最后用 substring 截取结果

时间复杂度：O(N)，每个字符最多被访问一次
空间复杂度：O(min(N, M))，M 为字符集大小，HashMap 最多存 M 个字符

## 2. score_table(student_id,score,class)班级平均分>80的班级的SQL

```sql
SELECT class, AVG(score) AS avg_score
FROM score_table
GROUP BY class
HAVING AVG(score) > 80;
```

算法思想：GROUP BY 按班级分组，HAVING 对聚合结果过滤。

关键点：WHERE 是分组前过滤行，HAVING 是分组后过滤组。这里要对平均分这个聚合结果做条件判断，所以必须用 HAVING 而不是 WHERE。

如果还要查出这些班级的学生明细，可以用子查询：

```sql
SELECT *
FROM score_table
WHERE class IN (
    SELECT class
    FROM score_table
    GROUP BY class
    HAVING AVG(score) > 80
);
```

## 3. score_table(student_id,score,class)每个班成绩最高的学生id的SQL

```sql
SELECT class, student_id, score
FROM score_table
WHERE (class, score) IN (
    SELECT class, MAX(score)
    FROM score_table
    GROUP BY class
);
```

如果每个班最高分有并列，上面会返回多条。如果只要每班一个，用窗口函数：

```sql
SELECT class, student_id, score
FROM (
    SELECT class, student_id, score,
           ROW_NUMBER() OVER (PARTITION BY class ORDER BY score DESC) AS rn
    FROM score_table
) t
WHERE rn = 1;
```

区别：ROW_NUMBER() 并列只取一个，RANK() 并列都取。根据业务需求选择。

第一种子查询写法兼容性好，MySQL 5.x 也能用。第二种窗口函数写法更灵活，MySQL 8.0+ 支持，还能方便地取TOP N（改 rn <= N 即可）。

## 4. 如果要每个班成绩最高的学生id的SQL，要求简单点实现呢

```sql
SELECT class, student_id, score
FROM score_table t1
WHERE score = (SELECT MAX(score) FROM score_table t2 WHERE t2.class = t1.class);
```

相关子查询，对每一行去找它所在班级的最高分，等于最高分的就是结果。写法最直观，一眼能看懂。

## 5. 入参是List<Integer>表示数字，List<String> 是加减乘除的符号，用java算法实现一个通用的数学表达式的计算。（包括加减乘除，不含括号）

```java
import java.util.*;
import java.io.*;

/**
 * 通用数学表达式计算（加减乘除，不含括号）
 * 入参：List<Integer> 数字列表，List<String> 运算符列表
 * 思路：先处理乘除（优先级高），再处理加减
 */
public class ExpressionCalculator {

    public static int calculate(List<Integer> nums, List<String> ops) {
        // 先处理乘除，结果压入新的数字列表
        Deque<Integer> numStack = new ArrayDeque<>();
        numStack.push(nums.get(0));

        for (int i = 0; i < ops.size(); i++) {
            String op = ops.get(i);
            int next = nums.get(i + 1);
            if (op.equals("*")) {
                numStack.push(numStack.pop() * next);
            } else if (op.equals("/")) {
                numStack.push(numStack.pop() / next);
            } else {
                // 加减先不算，把符号带入数字（减法存负数）
                if (op.equals("-")) {
                    numStack.push(-next);
                } else {
                    numStack.push(next);
                }
            }
        }

        // 剩下的全部求和
        int result = 0;
        for (int n : numStack) {
            result += n;
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        // 测试: 2 + 3 * 4 - 6 / 2 = 2 + 12 - 3 = 11
        List<Integer> nums = Arrays.asList(2, 3, 4, 6, 2);
        List<String> ops = Arrays.asList("+", "*", "-", "/");
        System.out.println(calculate(nums, ops)); // 11
    }
}
```

算法思想：一次遍历，遇到乘除立即计算，遇到加减把数字（减法存负数）压栈，最后栈内所有数求和。

具体步骤：
1. 第一个数直接入栈
2. 遍历运算符，乘除直接弹栈顶计算后压回，加法直接压入，减法压入负数
3. 栈内所有元素求和即为结果

时间复杂度：O(N)，一次遍历 + 一次求和
空间复杂度：O(N)，栈最多存 N 个数

## 6. 如果要支持加上括号的数学表达式，入参是List<Integer>表示数字，List<String> 是加、减、乘、除、括号的符号集合。具体支持括号的代码逻辑应该加到以上哪一行？

支持括号的话，核心逻辑要加在遍历运算符的for循环里，也就是 `for (int i = 0; i < ops.size(); i++)` 这一行的循环体内。

思路是遇到左括号时递归调用calculate处理括号内的子表达式，遇到右括号时返回当前结果。

具体改动点：

1. 遍历ops时，遇到 `(` ，递归调用自身计算括号内的表达式，把递归返回的结果当作一个数字参与后续运算
2. 遇到 `)` ，结束当前递归，返回当前栈内求和结果

但说实话，如果入参要支持括号，用 `List<Integer>` + `List<String>` 这种分离结构就不太合适了，因为括号打破了"数字和运算符交替出现"的规律。更好的做法是改成一个混合的token列表 `List<String>`，然后用经典的双栈法：

```java
// 在遍历token的循环体内加这两个分支：
if (token.equals("(")) {
    opStack.push(token);  // 左括号直接压运算符栈
} else if (token.equals(")")) {
    // 弹出运算符计算，直到遇到左括号
    while (!opStack.peek().equals("(")) {
        compute(numStack, opStack);
    }
    opStack.pop(); // 弹出左括号
}
```

核心就是左括号入栈做"屏障"，右括号触发计算直到遇到配对的左括号。这段逻辑加在原来判断 `* / + -` 的同级位置，作为新的分支条件。

## 7.

时间限制：1秒
空间限制：32MB
64bit IO Format: %lld
求a+b的和
输入描述
多组读入，每一行有两个数A， B。0 < A, B < 1000000
输出描述
每行输出一个结果
示例 1
输入
1 1
输出
2

```java
import java.util.*;
import java.io.*;

/**
 * A+B问题
 * 多组读入，每行两个数A B，输出A+B的和
 * 0 < A, B < 1000000
 */
public class Solution {

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(new BufferedOutputStream(System.out));
        String line;
        while ((line = br.readLine()) != null && !line.isEmpty()) {
            String[] parts = line.trim().split("\\s+");
            long a = Long.parseLong(parts[0]);
            long b = Long.parseLong(parts[1]);
            out.println(a + b);
        }
        out.flush();
        out.close();
    }
}
```

算法思想：逐行读入，解析两个数相加输出。

具体步骤：
1. while循环多组读入，readLine()返回null时结束
2. 按空白分割取两个数，用long防溢出
3. 输出和

时间复杂度：O(T)，T为输入组数
空间复杂度：O(1)

## 8.

第一行为一个数N，表示成员总数，成员编号1-N，1<=N<=1000
第二行为N个空格分隔的数，表示编号1-N的成员的财富值。0<=财富值<=1000000
接下来N-1行，每行两个空格分隔的整数(N1,N2)，表示N1是N2的父节点。
输出描述
最富裕的小家庭的财富和
示例 1
输入
4
100 200 300 500
1 2
1 3
2 4
输出
700

```java
import java.util.*;
import java.io.*;

/**
 * 最富裕的小家庭
 * N个成员组成树形结构，小家庭=一个节点+其直接子节点
 * 求所有小家庭中财富和的最大值
 * 示例：节点2的小家庭={2,4}，财富=200+500=700
 */
public class Solution {

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int n = Integer.parseInt(br.readLine().trim());
        StringTokenizer st = new StringTokenizer(br.readLine());
        int[] wealth = new int[n + 1];
        for (int i = 1; i <= n; i++) {
            wealth[i] = Integer.parseInt(st.nextToken());
        }

        // 每个节点的小家庭财富 = 自身 + 所有直接子节点
        // 初始化为自身财富
        long[] familyWealth = new long[n + 1];
        for (int i = 1; i <= n; i++) {
            familyWealth[i] = wealth[i];
        }

        // 读取父子关系，子节点财富累加到父节点的小家庭
        for (int i = 0; i < n - 1; i++) {
            st = new StringTokenizer(br.readLine());
            int parent = Integer.parseInt(st.nextToken());
            int child = Integer.parseInt(st.nextToken());
            familyWealth[parent] += wealth[child];
        }

        // 取最大值
        long max = 0;
        for (int i = 1; i <= n; i++) {
            max = Math.max(max, familyWealth[i]);
        }
        System.out.println(max);
    }
}
```

算法思想：小家庭 = 节点自身 + 直接子节点。读取父子关系时直接把子节点财富累加到父节点的家庭财富上，最后取最大值。

具体步骤：
1. familyWealth[i] 初始化为自身财富
2. 每读一条边 (parent, child)，把 wealth[child] 加到 familyWealth[parent]
3. 遍历所有节点取 familyWealth 最大值

时间复杂度：O(N)，遍历边和节点各一次
空间复杂度：O(N)，存储财富数组

## 9.

绘图机器的绘图笔初始位置在原点（0, 0），机器启动后其绘图笔按下面规则绘制直线：
1）尝试沿着横向坐标轴正向绘制直线，直到给定的终点值E。
2）期间可通过指令在纵坐标轴方向进行偏移，并同时绘制直线，偏移后按规则1 绘制直线；指令的格式为X offsetY，表示在横坐标X 沿纵坐标方向偏移，offsetY为正数表示正向偏移，为负数表示负向偏移。
给定了横坐标终点值E、以及若干条绘制指令，请计算绘制的直线和横坐标轴、以及 X=E 的直线组成图形的面积。
输入描述
首行为两个整数 N E，表示有N条指令，机器运行的横坐标终点值E。
接下来N行，每行两个整数表示一条绘制指令X offsetY，用例保证横坐标X以递增排序方式出现，且不会出现相同横坐标X。
取值范围：0 < N <= 10000, 0 <= X <= E <=20000, -10000 <= offsetY <= 10000。
输出描述
一个整数，表示计算得到的面积，用例保证，结果范围在0~4294967295内
示例 1
输入
4 10
1 1
2 1
3 1
4 -2
输出
12
示例 2
输入
2 4
0 1
2 -2
输出
4

```java
import java.util.*;
import java.io.*;

/**
 * 绘图机器面积计算
 * 绘图笔从(0,0)出发沿x轴正向画到E，期间按指令在y方向偏移
 * 计算绘制的直线与x轴、x=E围成的面积
 * 每段面积 = 宽度 * |当前y|（矩形面积，因为y在段内不变）
 */
public class Solution {

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        int n = Integer.parseInt(st.nextToken());
        int e = Integer.parseInt(st.nextToken());

        long area = 0;
        int curX = 0, curY = 0;

        for (int i = 0; i < n; i++) {
            st = new StringTokenizer(br.readLine());
            int x = Integer.parseInt(st.nextToken());
            int offsetY = Integer.parseInt(st.nextToken());
            // 从curX到x这段，y值不变，面积=宽*|y|
            area += (long)(x - curX) * Math.abs(curY);
            // 执行偏移
            curY += offsetY;
            curX = x;
        }
        // 最后一段从curX到E
        area += (long)(e - curX) * Math.abs(curY);

        System.out.println(area);
    }
}
```

算法思想：按指令把x轴分成若干段，每段内y值不变，面积就是每段宽度乘以|y|的累加。

具体步骤：
1. 从 (0,0) 出发，维护当前坐标 (curX, curY)
2. 每条指令 (x, offsetY)：先算 curX 到 x 这段的面积 = (x - curX) * |curY|，然后 curY += offsetY，curX = x
3. 所有指令处理完后，补算 curX 到 E 的最后一段面积

验证示例1：(0→1) |0|×1=0，(1→2) |1|×1=1，(2→3) |2|×1=2，(3→4) |3|×1=3，(4→10) |1|×6=6，总计=12 ✓

时间复杂度：O(N)
空间复杂度：O(1)

## 10.

定义一个语言，包含以下几个概念
1. 元素：整数，如12、33，以及一个表达空值的 null
2. 序列：把元素排列起来，以 null 结尾，如 [1,2,3,null] 是一个长度为 3 的序列，空序列为 [null]
3. 指令：
   1. join(a, seq): 将 a 添加到序列 seq 的前面，形成一个新的序列，如 join(1, [2, 3, 4, null]) => [1, 2, 3, 4, null]
   2. head(seq): 返回序列里的第一个元素，如 head([1,2,3, null]) = 1, head([null]) = null
   3. tail(seq) 返回序列里去掉第一个元素之后的序列，如 tail([1,2,3, null]) = [2, 3, null]，tail([null]) 会报错
   4. if x then y else z：当 x 表达式为 true 时，返回 y，否则返回 z
   5. 运算符号 + - * / 同数学含义，% 取余数，>、<、<=、>=、== 比较运算符
   6. 函数定义：定义一个名称为 func 的函数，接收一个参数 fn func(x):     return …
   7. 函数调用：func(10) 调用名称为 func 的函数，输入参数 10

基于以上的概念，实现下面的函数：
1. isEmpty(seq)，返回一个序列是否为空，如 isEmpty([null]) = true；
2. length(seq) 计算一个序列的长度，如 length([1,2,3, null]) 应返回 3；
3. sum(seq) 计算一个数值序列的元素之和；
4. append(seq, e) 将元素 e 添加到序列 seq 的后面，形成一个新的序列，如 append([1,2,null], 3) = [1, 2, 3, null]
5. merge(seq1, seq2): 实现两个序列的合并，如 merge([1, 2, null], [3, 4, null]) = [1, 2, 3, 4, null]
6. indexOf(seq, e) 返回 e 在序列 seq 中第一次出现的下标，如果没有找到，返回 -1，如 indexOf([1,2,3, null], 2) = 1
7. filter(seq, func(x): bool) 将序列 seq 中满足条件 func 的元素筛选出来，形成一个新的序列，如 filter([1,2,3,4,5,6,null], (x) -> if x % 2 == 1 then True else False) 返回奇数序列 [1,3,5,null]
8. map(seq, func(x): elemenet): 将序列中所有的元素应用 func 变换成另一个序列，如 map([1,2,3, null], (x) => 2 * x - 1) = [1, 3, 5, null]
9. reverse(seq)：返回序列的逆序形式，如 reverse([1, 2, 3, null]) = [3, 2, 1, null]

这道题是用给定的原语（join/head/tail/if-then-else）实现高阶函数，本质是函数式编程+递归。用题目定义的伪代码语言来写：

```
// 1. isEmpty：head为null就是空序列
fn isEmpty(seq):
    return head(seq) == null

// 2. length：递归，空序列长度0，否则1+tail的长度
fn length(seq):
    return if isEmpty(seq) then 0 else 1 + length(tail(seq))

// 3. sum：递归求和
fn sum(seq):
    return if isEmpty(seq) then 0 else head(seq) + sum(tail(seq))

// 4. append：递归到末尾再join
fn append(seq, e):
    return if isEmpty(seq) then join(e, [null]) else join(head(seq), append(tail(seq), e))

// 5. merge：把seq1的元素逐个接到seq2前面
fn merge(seq1, seq2):
    return if isEmpty(seq1) then seq2 else join(head(seq1), merge(tail(seq1), seq2))

// 6. indexOf：辅助函数带下标递归
fn indexOf(seq, e):
    return indexOfHelper(seq, e, 0)

fn indexOfHelper(seq, e, i):
    return if isEmpty(seq) then -1
           else if head(seq) == e then i
           else indexOfHelper(tail(seq), e, i + 1)

// 7. filter：满足条件的join进去，不满足的跳过
fn filter(seq, func):
    return if isEmpty(seq) then [null]
           else if func(head(seq)) then join(head(seq), filter(tail(seq), func))
           else filter(tail(seq), func)

// 8. map：对每个元素应用func
fn map(seq, func):
    return if isEmpty(seq) then [null]
           else join(func(head(seq)), map(tail(seq), func))

// 9. reverse：递归，把head append到reverse后的tail末尾
fn reverse(seq):
    return if isEmpty(seq) then [null]
           else append(reverse(tail(seq)), head(seq))
```

核心思路：所有函数都是递归实现，基于三个原语操作：
- head 取首元素
- tail 取剩余序列
- join 在头部插入

递归模式统一：isEmpty 做终止条件，head 取当前元素处理，tail 递归处理剩余部分。

reverse 的时间复杂度是 O(N²)，因为每次 append 要遍历到末尾。如果要 O(N)，可以用辅助函数带累加器：

```
fn reverse(seq):
    return reverseHelper(seq, [null])

fn reverseHelper(seq, acc):
    return if isEmpty(seq) then acc
           else reverseHelper(tail(seq), join(head(seq), acc))
```

这是尾递归版本，每次把 head 插到累加器前面，O(N) 完成。
