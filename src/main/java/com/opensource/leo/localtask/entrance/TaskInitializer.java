package com.opensource.leo.localtask.entrance;

import com.opensource.leo.localtask.cron.LocalTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * scan task in the package of com.
 * User:leo.lx
 * Date:15/8/13
 */
public class TaskInitializer {

    Map<String, LocalTask> init() throws IOException, IllegalAccessException, InstantiationException {
        Map<String, LocalTask> tasks = new HashMap<String, LocalTask>();
        // init task from project
        List<Class> clazzs = Annotationer.findClass(LocalTask.class, PersonalTask.class, TaskConfig.WORK_PACKAGE_DIR);
        for (Class clazz : clazzs) {
            initTask(clazz, tasks);
        }
        return tasks;
    }

    private void initTask(Class clazz, Map<String, LocalTask> tasks) throws IllegalAccessException, InstantiationException {
        Object taskObj = clazz.newInstance();
        LocalTask task;
        if (taskObj != null && taskObj instanceof LocalTask)
            task = (LocalTask) taskObj;
        else
            return;
        if (tasks.containsKey(task.getIdentify())) {
            // 如果工程中有重复的task,在初始化时失败
            throw new TaskException(String.format("[TaskInitializer] : you have same task:%s", task.getIdentify()));
        }
        tasks.put(task.getIdentify(), task);
    }
}
