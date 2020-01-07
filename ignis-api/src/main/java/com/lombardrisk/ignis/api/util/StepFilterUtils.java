package com.lombardrisk.ignis.api.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StepFilterUtils {

    private static final String DOUBLE_EQUALS_PATTERN = "('[^']*'|\\=+)";
    private static final String DOUBLE_EQUALS = "==";
    private static final String EQUALS = "=";

    public static String getStepFilterWithQuotes(String filter, Set<String> fields) {
        StringBuffer sb = new StringBuffer();
        String fieldPattern = "(" + fields.stream()
                .map(field -> "('.*?'|\\b" + field + "\\b)")
                .collect(Collectors.joining("|")) + ")";
        Pattern pattern = Pattern.compile(fieldPattern);
        Matcher matcher = pattern.matcher(filter);
        while (matcher.find()) {
            if (matcher.group(1).contains("'")) {
                matcher.appendReplacement(sb, matcher.group(1));
            } else {
                matcher.appendReplacement(sb, "\"" + matcher.group(1) + "\"");
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String replaceDoubleEqualsByEquals(String filter) {
        if (StringUtils.isEmpty(filter) ||
                !filter.contains(DOUBLE_EQUALS)) {
            return filter;
        }

        StringBuffer sb = new StringBuffer();
        String fieldPattern = DOUBLE_EQUALS_PATTERN;
        Pattern pattern = Pattern.compile(fieldPattern);
        Matcher matcher = pattern.matcher(filter);
        while (matcher.find()) {
            if (DOUBLE_EQUALS.equals(matcher.group())) {
                matcher.appendReplacement(sb, EQUALS);
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


}
