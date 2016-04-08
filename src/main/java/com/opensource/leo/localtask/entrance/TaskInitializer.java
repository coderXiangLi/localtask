package com.opensource.leo.localtask.entrance;

import com.google.common.collect.Sets;
import com.opensource.leo.localtask.cron.LocalTask;
import com.opensource.leo.localtask.cron.Task;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.SetUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * scan task in the package of com.
 * User:leo.lx
 * Date:15/8/13
 */
public class TaskInitializer {

    Map<String, Task> init() throws IOException, IllegalAccessException, InstantiationException {
        Map<String, Task> tasks = new HashMap<String, Task>();
        // begin task from project
        List<Class> clazzs = Annotationer.findClass(LocalTask.class, PersonalTask.class, TaskConfig.WORK_PACKAGE_DIR);
        for (Class clazz : clazzs) {
            initTask(clazz, tasks);
        }
        return tasks;
    }

    Map<String, Task> init(Set<String> includes) throws IOException, IllegalAccessException, InstantiationException {
        if (CollectionUtils.isEmpty(includes)) {
            return init();
        }
        Map<String, Task> tasks = init();
        Set<String> intersection = Sets.intersection(includes, tasks.keySet());
        Map<String, Task> intersectionMap = new HashMap<String, Task>();
        for (String task : intersection) {
            intersectionMap.put(task, tasks.get(task));
        }
        return intersectionMap;
    }

    private void initTask(Class clazz, Map<String, Task> tasks) throws IllegalAccessException, InstantiationException {
        Object taskObj = clazz.newInstance();
        Task task;
        if (taskObj != null && taskObj instanceof Task)
            task = (Task) taskObj;
        else
            return;
        if (tasks.containsKey(task.getIdentify())) {
            // 如果工程中有重复的task,在初始化时失败
            throw new TaskException(String.format("[TaskInitializer] : you have same task:%s", task.getIdentify()));
        }
        tasks.put(task.getIdentify(), task);
    }
}
