package com.opensource.leo.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class ReflectionUtil {
    static public <T> T defaultCreate(Class<T> type) throws Exception {
        Constructor<T> cf = type.getDeclaredConstructor();
        cf.setAccessible(true);
        return cf.newInstance();
    }

    static public <T> T parameterCreate(Class<T> type, Class<?>[] parameterTypes, Object[] initargs) throws Exception {
        Constructor<T> cf = type.getDeclaredConstructor(parameterTypes);
        cf.setAccessible(true);
        return cf.newInstance(initargs);
    }

    static public Method getMethodUp(Class<?> type, String name, Class<?>... parameterTypes) {
        // super class
        Method method = null;
        for (Class<?> clazz = type; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                method = clazz.getDeclaredMethod(name, parameterTypes);
                break;
            } catch (Exception e) {
                // ignore
            }
        }
        return method;
    }

    static public void setPrivateField(Object instance, String name, Object value) {
        Field f = null;
        for (Class<?> clazz = instance.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                f = clazz.getDeclaredField(name);
                break;
            } catch (Exception e) {
                // ignore
            }
        }
        if (f == null) throw new RuntimeException("field is null");
        f.setAccessible(true);
        try {
            f.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static public Object getPrivateField(Object instance, String name) {
        Object result = null;
        Class<?> type = instance.getClass();
        do {
            try {
                Field f = type.getDeclaredField(name);
                f.setAccessible(true);
                result = f.get(instance);
            } catch (Exception e) {
                continue;
            }
        } while (null == result && null != (type = type.getSuperclass()));

        return result;
    }

    static public void setStaticField(Class<?> type, String name, Object value) throws Exception {
        Field f = type.getDeclaredField(name);
        f.setAccessible(true);
        f.set(null, value);
    }

    /**
     * Returns non-static private method with given signature defined by given
     * class, or null if none found. Access checks are disabled on the returned
     * method (if any).
     */
    static public Method getPrivateMethod(Class<?> cl, String name, Class<?>[] argTypes, Class<?> returnType) {
        Method meth = null;
        try {
            meth = cl.getDeclaredMethod(name, argTypes);
            meth.setAccessible(true);
        } catch (NoSuchMethodException ex) {
            return null;
        }

        if (meth.getReturnType() != returnType) {
            return null;
        }

        int mods = meth.getModifiers();
        return (!Modifier.isStatic(mods) && Modifier.isPrivate(mods)) ? meth : null;
    }

    static public Method getInstanceMethod(Class<?> cl, String name, Class<?>[] argTypes, Class<?> returnType) {
        Method meth = null;
        try {
            meth = cl.getDeclaredMethod(name, argTypes);
            meth.setAccessible(true);
        } catch (NoSuchMethodException ex) {
            return null;
        }

        if (meth.getReturnType() != returnType) {
            return null;
        }

        int mods = meth.getModifiers();
        return !Modifier.isStatic(mods) && !Modifier.isAbstract(mods) ? meth : null;
    }
}
