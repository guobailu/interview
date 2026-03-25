package org.algorithm;

import java.io.*;

/**
 * 题目：最长不重复子串
 * input: "abcdeafh" output: "bcdeafh"
 * 
 * 滑动窗口 + 数组记录字符最新位置
 */
public class LongestNonRepeatingSubstring {

    public static String longestUniqueSubstring(String s) {
        if (s == null || s.isEmpty()) return "";

        // 记录每个字符最近一次出现的位置，ASCII共128个字符
        int[] lastIndex = new int[128];
        for (int i = 0; i < 128; i++) lastIndex[i] = -1;

        int left = 0;       // 窗口左边界
        int maxStart = 0;   // 最长子串起始位置
        int maxLen = 0;     // 最长子串长度

        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            // 如果字符在窗口内出现过，左边界跳到重复字符的下一个位置
            if (lastIndex[c] >= left) {
                left = lastIndex[c] + 1;
            }
            // 更新字符最新位置
            lastIndex[c] = right;
            // 更新最长子串
            if (right - left + 1 > maxLen) {
                maxLen = right - left + 1;
                maxStart = left;
            }
        }
        return s.substring(maxStart, maxStart + maxLen);
    }

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(new BufferedOutputStream(System.out));

        // 测试用例
        String test = "abcdeafh";
        out.println("input: \"" + test + "\"");
        out.println("output: \"" + longestUniqueSubstring(test) + "\"");

        // 从标准输入读取
        String line;
        while ((line = br.readLine()) != null && !line.isEmpty()) {
            out.println("input: \"" + line + "\"");
            out.println("output: \"" + longestUniqueSubstring(line) + "\"");
            out.flush();
        }

        out.flush();
        out.close();
    }
}
