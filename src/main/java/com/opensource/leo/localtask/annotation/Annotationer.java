package com.opensource.leo.localtask.annotation;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Author:leo.lx
 * Date:2016-01-15 19:15
 */
@SuppressWarnings("unchecked")
public class Annotationer {
    private Annotationer() {
    }

    public static <T extends Annotation> T constructorAnnotation(Class tClass, Class<? extends Annotation> annotation) {
        Annotation controller = null;
        try {
            Constructor constructor = tClass.getConstructor(new Class[]{});
            controller = constructor.getAnnotation(annotation);
            return (T) controller;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return (T) controller;
    }

    public static <T extends Annotation> T classAnnotation(Class tClass, Class<? extends Annotation> annotation) {
        Annotation controller = tClass.getAnnotation(annotation);
        return (T) controller;
    }

    public static <T> List<Class<T>> findClass(Class<T> tClass, Class<? extends Annotation> annotation, String packageName) {
        List<Class<T>> clazzs = new ArrayList<Class<T>>();
        ClassPath classPath;
        try {
            classPath = ClassPath.from(tClass.getClassLoader());
        } catch (IOException e) {
            return clazzs;
        }
        ImmutableSet<ClassPath.ClassInfo> immutableSet = classPath.getTopLevelClassesRecursive(packageName);
        for (ClassPath.ClassInfo classInfo : immutableSet) {
            Class clazz = classInfo.load();
            if (clazz != null && tClass.isAssignableFrom(clazz)) {
                Annotation controller = clazz.getAnnotation(annotation);
                if (controller != null) {
                    clazzs.add(clazz);
                }
            }
        }
        return clazzs;
    }

    /**
     * Create a instance of by a class which has the specific annotation
     *
     * @param tClass      class of instance which wants to be created
     * @param annotation  annotation that class must has
     * @param packageName
     * @return
     */
    public static <T> T createIntance(Class<T> tClass, Class<? extends Annotation> annotation, String packageName) {
        ClassPath classPath;
        try {
            classPath = ClassPath.from(tClass.getClassLoader());
            ImmutableSet<ClassPath.ClassInfo> immutableSet = classPath.getTopLevelClassesRecursive(packageName);
            for (ClassPath.ClassInfo classInfo : immutableSet) {
                Class clazz = classInfo.load();
                if (clazz != null) {
                    Annotation controller = clazz.getAnnotation(annotation);
                    if (controller != null && tClass.isAssignableFrom(clazz)) {
                        Object reward = clazz.newInstance();
                        return (T) reward;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("[Annotationer] create instance error", e);
        }
        return null;
    }
}