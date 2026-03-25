package org.algorithm;

import java.io.*;
import java.util.*;

/**
 * 题目：入参是List<Integer>表示数字，List<String>是加减乘除的符号，
 * 实现一个通用的数学表达式计算（包括加减乘除，不含括号）
 * 
 * 核心思路：一次遍历用栈处理优先级，遇到乘除直接和栈顶计算，遇到加减压栈，最后求和
 */
public class ExpressionCalculator {

    /**
     * 一次遍历，栈处理运算符优先级
     * 遇到乘除直接和栈顶计算，遇到加减把数字压栈，最后栈内求和
     */
    public static double calculate(List<Integer> nums, List<String> ops) {
        if (nums == null || nums.isEmpty()) return 0;

        Deque<Double> stack = new ArrayDeque<>();
        stack.push(nums.get(0).doubleValue());

        for (int i = 0; i < ops.size(); i++) {
            String op = ops.get(i);
            double num = nums.get(i + 1);

            switch (op) {
                case "*":
                    // 乘法：弹出栈顶，计算后压回
                    stack.push(stack.pop() * num);
                    break;
                case "/":
                    // 除法：检查除零
                    if (num == 0) throw new ArithmeticException("除数不能为0");
                    stack.push(stack.pop() / num);
                    break;
                case "+":
                    // 加法：直接压栈
                    stack.push(num);
                    break;
                case "-":
                    // 减法：压入负数
                    stack.push(-num);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的运算符: " + op);
            }
        }

        // 栈内所有数求和即为结果
        double result = 0;
        for (double val : stack) {
            result += val;
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(new BufferedOutputStream(System.out));

        // 测试用例: 3 + 5 * 2 - 8 / 4 = 3 + 10 - 2 = 11
        List<Integer> nums = Arrays.asList(3, 5, 2, 8, 4);
        List<String> ops = Arrays.asList("+", "*", "-", "/");
        out.println("表达式: 3 + 5 * 2 - 8 / 4");
        out.println("结果: " + calculate(nums, ops));

        // 测试用例2: 10 * 2 + 3 = 23
        List<Integer> nums2 = Arrays.asList(10, 2, 3);
        List<String> ops2 = Arrays.asList("*", "+");
        out.println("表达式: 10 * 2 + 3");
        out.println("结果: " + calculate(nums2, ops2));

        // 从标准输入读取：第一行数字用空格分隔，第二行运算符用空格分隔
        out.println("请输入数字（空格分隔）：");
        out.flush();
        String numLine = br.readLine();
        if (numLine != null && !numLine.isEmpty()) {
            out.println("请输入运算符（空格分隔）：");
            out.flush();
            String opLine = br.readLine();

            String[] numArr = numLine.trim().split("\\s+");
            String[] opArr = opLine.trim().split("\\s+");

            List<Integer> inputNums = new ArrayList<>();
            for (String s : numArr) inputNums.add(Integer.parseInt(s));
            List<String> inputOps = Arrays.asList(opArr);

            out.println("结果: " + calculate(inputNums, inputOps));
        }

        out.flush();
        out.close();
    }
}
