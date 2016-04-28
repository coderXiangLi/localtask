package com.opensource.leo.localtask.entrance;

import com.google.common.collect.Sets;
import com.opensource.leo.localtask.annotation.Annotationer;
import com.opensource.leo.localtask.annotation.Executor;
import com.opensource.leo.localtask.cron.Task;
import com.opensource.leo.localtask.cron.TaskExecutor;
import org.apache.commons.collections.CollectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * scan task in the package of com.
 * User:leo.lx
 * Date:15/8/13
 */
public class TaskInitializer {
    private final Generator<Map<String, TaskExecutor>> executorGenerator = new ExecutorGenerator();
    private final Generator<Map<String, Task>> taskGenerator = new TaskGenarator();

    Map<String, Task> init() throws Exception {
        Map<String, TaskExecutor> executorMap = executorGenerator.generate();
        Map<String, Task> taskMap = taskGenerator.generate();
        for (Map.Entry<String, Task> entry : taskMap.entrySet()) {
            Task task = entry.getValue();
            Executor executor = Annotationer.classAnnotation(task.getClass(), Executor.class);
            String executorName = executor.name();
            if (!executorMap.containsKey(executorName)) {
                throw new TaskException("do not have your executor");
            } else
                task.setTaskExecutor(executorMap.get(executorName));
        }
        return taskMap;
    }

    Map<String, Task> init(Set<String> includes) throws Exception {
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
}
