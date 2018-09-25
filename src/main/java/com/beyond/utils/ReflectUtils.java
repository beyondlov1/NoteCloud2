package com.beyond.utils;


import java.lang.reflect.Field;

public class ReflectUtils {

    public static <T> Object getValueByField(Class c, T value, String propertyName) {
        Object result=null;
        Field field  = null;
        try {
            field = c.getDeclaredField(propertyName);
            field.setAccessible(true);
            result = field.get(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
