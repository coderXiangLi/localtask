package com.opensource.leo.localtask.entrance;


import com.opensource.leo.localtask.annotation.Annotationer;
import com.opensource.leo.localtask.annotation.PersonalTask;
import com.opensource.leo.localtask.cron.TaskExecutor;
import com.opensource.leo.util.ReflectionUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Create executor
 * Created by leo.lx on 4/21/16.
 */
public class ExecutorGenerator implements Generator<Map<String, TaskExecutor>> {

    /**
     * @return Map<String, TaskExecutor>  key:executor name value:executor
     * @throws TaskException if defaultCreate error or
     */
    @Override
    public Map<String, TaskExecutor> generate() throws TaskException {
        Map<String, TaskExecutor> executors = new HashMap<String, TaskExecutor>();
        try {
            // begin task from project
            List<Class<TaskExecutor>> clazzs = Annotationer.findClass(TaskExecutor.class, PersonalTask.class, TaskConfig.WORK_PACKAGE_DIR);
            for (Class<TaskExecutor> clazz : clazzs) {
                TaskExecutor executor = ReflectionUtil.defaultCreate(clazz);
                if (executors.containsKey(executor.name()))
                    throw new RuntimeException(String.format("error same executor,\01executor name\02%s", clazz.getName()));
                executors.put(executor.name(), executor);
            }
        } catch (Exception e) {
            throw new TaskException("[ExecutorGenerator] generate error", e);
        }
        return executors;
    }
}
