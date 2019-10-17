package com.huanhong.decathlonstb.util;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class DateFormatUitl {

    private static Map<String, SimpleDateFormat> maps = new HashMap<>();

    private static SimpleDateFormat getFormat(String format) {
        if (maps.get(format) == null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            maps.put(format, simpleDateFormat);
            return simpleDateFormat;
        }
        return maps.get(format);
    }

    public static String yyyyMMdd(Object o) {
        return getFormat("yyyy-MM-dd").format(o);
    }

    public static String yyyyMMddmmss(Object o) {
        return getFormat("yyyy-MM-dd mm:ss").format(o);
    }
}