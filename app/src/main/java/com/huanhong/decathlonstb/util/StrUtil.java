package com.huanhong.decathlonstb.util;

/**
 * Created by apple on 2017/10/27.
 */

public class StrUtil {

    /**
     * 计算str2在str1中的个数
     *
     * @param str1
     * @param str2
     * @return
     */
    private int countStr(String str1, String str2) {
        int counter = 0;
        if (str1.indexOf(str2) == -1) {
            return 0;
        }
        while (str1.indexOf(str2) != -1) {
            counter++;
            str1 = str1.substring(str1.indexOf(str2) + str2.length());
        }
        return counter;
    }
}
