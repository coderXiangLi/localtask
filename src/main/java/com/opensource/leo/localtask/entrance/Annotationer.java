package com.opensource.leo.localtask.entrance;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * Author:leo.lx
 * Date:2016-01-15 19:15
 */
public class Annotationer {
    private final static Logger logger = LoggerFactory.getLogger(Annotationer.class);

    private Annotationer() {
    }

    public static List<Class> findClass(Class tClass, Class<? extends Annotation> annotation, String packageName) {
        List<Class> clazzs = new ArrayList<Class>();
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
    public static <T> T createOne(Class<T> tClass, Class<? extends Annotation> annotation, String packageName) {
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
                        if (reward != null) {
                            return (T) reward;
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("create", e);
        } catch (InstantiationException e) {
            logger.error("create", e);
        } catch (IllegalAccessException e) {
            logger.error("create", e);
        }
        return null;
    }

    /**
     * Create a instance of by a class which has the specific annotation
     *
     * @param tClass     class of instance which wants to be created
     * @param annotation annotation that class must has
     * @return
     */
    /**
     * Create a instance of by a class which has the specific annotation
     *
     * @param tClass     class of instance which wants to be created
     * @param annotation annotation that class must has
     * @return
     */
    public static <T> List<T> createAll(Class<T> tClass, Class<? extends Annotation> annotation, String packageName) {
        List<T> obj = new ArrayList<T>();
        List<Class> clazzs = findClass(tClass, annotation, packageName);
        if (CollectionUtils.isEmpty(clazzs)) return obj;
        for (Class clazz : clazzs) {
            Object o = null;
            try {
                o = clazz.newInstance();
            } catch (InstantiationException e) {
                logger.error("create", e);
            } catch (IllegalAccessException e) {
                logger.error("create", e);
            }
            if (o != null) {
                obj.add((T) o);
            }
        }
        return obj;
    }
}
