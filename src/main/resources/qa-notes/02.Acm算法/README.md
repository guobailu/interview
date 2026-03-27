# Acm算法 Q&A

## 1.

有一个文件, 包含以一定规则写作的文本, 请统计文件中包含的文本数量
规则如下
1. 文本以";"分隔，最后一条可以没有";"，但空文本不能算语句，比如"COMMAND A; ;"只能算一条语句.注意, 无字符/空白字符/制表符都算作"空"文本
2. 文本可以跨行, 比如下面, 是一条文本, 而不是三条
COMMAND A
AND
COMMAND B;
3. 文本支持字符串, 字符串为成对的单引号(')或者成对的双引号("), 字符串可能出现用转义字符(\)处理的单双引号(比如"your input is: \"")和转义字符本身, 比如COMMAND A "Say \"hello\"";
4. 支持注释, 可以出现在字符串之外的任意位置, 注释以"--"开头, 到换行结束, 比如
COMMAND A; -- this is comment
COMMAND -- comment
A AND COMMAND B;
注意, 字符串内的"--", 不是注释
输入描述
文本文件
输出描述
包含的文本数量
示例 1
输入
COMMAND TABLE IF EXISTS "UNITED STATE";
COMMAND A GREAT (ID ADSAB,
download_length INTE-GER, -- test
file_name TEXT,
guid TEXT,
mime_type TEXT,
notifica-tionid INTEGER,
original_file_name TEXT,
pause_reason_type INTEGER,
resumable_flag INTEGER,
start_time INTEGER,
state INTEGER,
folder TEXT,
path TEXT,
total_length INTE-GER,
url TEXT);
输出
2

```java
import java.util.*;
import java.io.*;

/**
 * 统计文本数量
 * 规则：分号分隔，支持跨行，支持字符串（单双引号+转义），支持--注释
 */
public class Main {
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append('\n');
        }
        String input = sb.toString();
        int count = 0;
        boolean hasContent = false; // 当前文本是否有非空内容
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            // 字符串内处理转义和结束
            if (inSingleQuote) {
                if (c == '\\') {
                    i++; // 跳过转义字符
                } else if (c == '\'') {
                    inSingleQuote = false;
                }
                continue;
            }
            if (inDoubleQuote) {
                if (c == '\\') {
                    i++;
                } else if (c == '"') {
                    inDoubleQuote = false;
                }
                continue;
            }

            // 非字符串内
            if (c == '-' && i + 1 < input.length() && input.charAt(i + 1) == '-') {
                // 注释，跳到行尾
                while (i < input.length() && input.charAt(i) != '\n') {
                    i++;
                }
            } else if (c == '\'') {
                inSingleQuote = true;
                hasContent = true;
            } else if (c == '"') {
                inDoubleQuote = true;
                hasContent = true;
            } else if (c == ';') {
                if (hasContent) {
                    count++;
                }
                hasContent = false;
            } else if (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
                hasContent = true;
            }
        }
        // 最后一条可以没有分号
        if (hasContent) {
            count++;
        }
        System.out.println(count);
    }
}
```

算法思想：逐字符扫描，用状态标记区分"字符串内"和"字符串外"，字符串外遇到 `--` 跳过注释，遇到 `;` 且有非空内容则计数+1。

具体步骤：
1. 读取全部输入拼成一个字符串（支持跨行）
2. 遍历每个字符，维护 inSingleQuote/inDoubleQuote 状态
3. 字符串内遇到 `\` 跳过下一个字符（转义），遇到配对引号结束字符串
4. 字符串外遇到 `--` 跳到行尾（注释），遇到 `;` 判断是否有内容并计数，遇到非空白字符标记 hasContent
5. 遍历结束后如果还有未结算的内容，计数+1

时间复杂度：O(N)，N为输入总字符数，单次遍历
空间复杂度：O(N)，存储输入字符串
