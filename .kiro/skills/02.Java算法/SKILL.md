---
name: Java算法面试
description: 当用户提出算法相关的面试题目时激活此技能，直接写解题函数（LeetCode风格，不处理输入输出），使用Java语言实现最优解法。未明确要求ACM模式时默认走本技能
---

# Java算法面试技能

## 触发条件
当用户提出算法相关的面试题目时激活，直接写解题函数（LeetCode风格，不处理输入输出），包括但不限于：数据结构、排序、查找、动态规划、贪心、回溯、图论、字符串处理、数学问题等。未明确要求ACM模式时默认走本技能。

## 代码实现要求
- 使用Java语言实现
- 追求最优的时间复杂度和空间复杂度
- 使用中文注释，注释简洁明了
- **只写解题函数，不读标准输入、不打印结果给评测系统**——入参从方法签名进、结果 return 出去，跟 LeetCode 提交框里一样
- main 方法只作本地自测入口，用硬编码用例调用解题函数并打印，**不要用 Scanner/BufferedReader 读 System.in**
- 代码可直接编译运行
- 类注释中需包含原始题目描述，方便复盘

**题目要求自行处理标准输入输出（ACM 模式／OJ 提交）时，不走本技能，走 `02.Acm算法`。**

## 代码模板
```java
import java.util.*;

/**
 * 题目描述：xxx
 */
public class Solution {

    // 核心算法实现：入参出参即题目签名
    public int solve(int[] nums, int target) {
        return 0;
    }

    public static void main(String[] args) {
        Solution s = new Solution();
        // 硬编码用例自测，含边界case
        System.out.println(s.solve(new int[]{1, 2, 3}, 3));
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
- 涉及大数运算时考虑long溢出问题
- 考虑边界条件和特殊case：空输入、单元素、重复元素、越界索引
- IO 性能优化（BufferedReader、StreamTokenizer）属于 ACM 模式的事，本技能不涉及
