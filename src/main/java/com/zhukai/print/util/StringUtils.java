package com.zhukai.print.util;

/**
 * @author zhukai
 * @date 2019/1/29
 */
public class StringUtils {

    public static boolean isEmpty(String str) {
        return str == null || "".equals(str.trim());
    }
}
