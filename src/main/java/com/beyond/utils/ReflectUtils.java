package com.beyond.utils;


import com.beyond.repository.impl.LocalDocumentRepository;

import java.lang.reflect.Field;

public class ReflectUtils {

    public static <T> Object getValueByField(Class c, T value, String propertyName) {
        Object result = null;
        Field field = null;
        try {
            field = c.getDeclaredField(propertyName);
            field.setAccessible(true);
            result = field.get(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Object getSourceObjectFromProxy(Object proxy,Class proxyClass, Class targetClass) throws NoSuchFieldException, IllegalAccessException {
        Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
        h.setAccessible(true);

        Object targetProxy = proxyClass.cast(h.get(proxy));
        Field declaredField = targetProxy.getClass().getDeclaredField(targetClass.getSimpleName().substring(0,1).toLowerCase()+targetClass.getSimpleName().substring(1));
        declaredField.setAccessible(true);
        return declaredField.get(targetProxy);
    }
}
