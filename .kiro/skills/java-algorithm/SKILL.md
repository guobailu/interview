---
name: Java算法面试
description: 当用户提出算法相关的面试题目时激活此技能，使用Java语言实现最优解法
---

# Java算法面试技能

## 触发条件
当用户提出算法相关的面试题目时激活此技能，包括但不限于：数据结构、排序、查找、动态规划、贪心、回溯、图论、字符串处理、数学问题等。

## 代码实现要求
- 使用Java语言实现
- 追求最优的时间复杂度和空间复杂度
- 使用中文注释，注释简洁明了
- 使用main方法作为测试入口
- 使用标准输入输出流（System.in / System.out）
- 代码可直接编译运行
- 类注释中需包含原始题目描述，方便复盘

## 代码模板
```java
import java.util.*;
import java.io.*;

public class Solution {
    // 核心算法实现

    public static void main(String[] args) throws Exception {
        // 使用BufferedReader/PrintWriter提升IO性能
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(new BufferedOutputStream(System.out));
        // 读取输入、调用算法、输出结果
        out.flush();
        out.close();
    }
}
```

## 回答结构
1. 先给出完整可运行的Java代码
2. 然后说明：
   - 算法思想：用一两句话概括核心思路
   - 具体步骤：分步骤说明实现逻辑
   - 时间复杂度：给出具体分析
   - 空间复杂度：给出具体分析
3. 如果算法适合并行化，补充说明在多核CPU下的优化思路（如ForkJoinPool、并行流等）

## 注意事项
- 优先选择最优解法，如果有多种解法，先给最优解，再简要提及其他思路
- IO密集型场景使用BufferedReader/PrintWriter替代Scanner
- 大数据量场景考虑使用StreamTokenizer进一步优化IO
- 涉及大数运算时考虑long溢出问题
- 考虑边界条件和特殊case
