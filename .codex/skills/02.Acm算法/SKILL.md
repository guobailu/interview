---
name: Java ACM模式算法
description: 当用户提出算法题目时激活此技能，使用Java语言以ACM模式（标准输入输出流）实现最优解法
---

# Java ACM模式算法技能

## 触发条件
当用户提出算法相关题目时激活此技能，包括但不限于：数据结构、排序、查找、动态规划、贪心、回溯、图论、字符串处理、数学问题等。

## 代码实现要求
- 使用Java语言实现
- 追求最优的时间复杂度和空间复杂度
- 使用中文注释，注释简洁明了
- ACM模式：从标准输入流（System.in）读取输入，打印结果到标准输出流（System.out）
- 使用BufferedReader/PrintWriter提升IO性能，大数据量场景使用StreamTokenizer
- 代码可直接编译运行
- 类注释中需包含原始题目描述

## 代码模板
```java
import java.util.*;
import java.io.*;

/**
 * 题目描述：xxx
 */
public class Main {
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(new BufferedOutputStream(System.out));

        // 读取输入
        // 调用算法
        // 输出结果

        out.flush();
        out.close();
    }

    // 核心算法实现
}
```

## 回答结构
1. 先给出完整可运行的Java代码（ACM模式）
2. 然后说明：
   - 算法思想：一两句话概括核心思路
   - 具体步骤：分步骤说明实现逻辑
   - 时间复杂度：给出具体分析
   - 空间复杂度：给出具体分析

## 注意事项
- 优先选择最优解法，如有多种解法先给最优解再简要提及其他思路
- 涉及大数运算时注意long溢出问题
- 考虑边界条件和特殊case
- 输入输出格式严格按照题目要求
