package com.kintex.check.utils;

import java.lang.reflect.Field;

public class IDUtils {


    /**
     * 根据字符串获取资源ID
     *
     * @param variableName
     * @param c
     * @return
     */
    public static int getResId(String variableName, Class<?> c) {
        try {
            String trim = variableName.replace(" ","");
            trim = trim.replace("-","");
            Field idField = c.getDeclaredField(trim);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

    }
}
