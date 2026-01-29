package com.zebrarfid.demo.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderReplacer {
    // 匹配{变量名}的正则
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{(\\w+)}");

    // 替换单个字符串中的占位符
    public static String replace(String content, Map<String, String> dataMap) {
        if (content == null || !content.contains("{")) {
            return content;
        }
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer(); // 改为StringBuffer
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = dataMap.getOrDefault(key, matcher.group());
            matcher.appendReplacement(sb, value); // 现在类型匹配
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
