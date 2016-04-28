package com.opensource.leo.localtask.entrance;

import com.google.common.base.Preconditions;
import com.opensource.leo.localtask.ContainerException;
import com.opensource.leo.localtask.annotation.Annotationer;
import com.opensource.leo.localtask.annotation.Partition;
import com.opensource.leo.localtask.annotation.PersonalTask;
import com.opensource.leo.localtask.annotation.Scheduled;
import com.opensource.leo.localtask.cron.Task;
import com.opensource.leo.localtask.cron.TaskMeta;
import com.opensource.leo.localtask.scheduling.CronTrigger;
import com.opensource.leo.localtask.shard.Partitioner;
import com.opensource.leo.util.ReflectionUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * Create task
 * Created by leo.lx on 4/21/16.
 */
public class TaskGenarator implements Generator<Map<String, Task>> {
    protected static final Logger logger = LoggerFactory.getLogger(TaskGenarator.class);
    private static final Map<String, Partitioner> partitioners = new HashMap<String, Partitioner>();

    static {
        List<Class<Partitioner>> classes = Annotationer.
                findClass(Partitioner.class, PersonalTask.class, TaskConfig.WORK_PACKAGE_DIR);
        for (Class<Partitioner> clazz : classes) {
            try {
                Partitioner partitioner = ReflectionUtil.defaultCreate(clazz);
                partitioners.put(clazz.getSimpleName(), partitioner);
            } catch (Exception e) {
                logger.error(String.format("[TaskGenarator] init error:\01Partitioner\02%s", clazz.getName()), e);
                throw new ContainerException("[TaskGenarator] init error", e);
            }
        }
    }

    @Override
    public Map<String, Task> generate() throws TaskException {
        Map<String, Task> tasks = new HashMap<String, Task>();
        try {
            // begin task from project
            List<Class<Task>> clazzs = Annotationer.findClass(Task.class, PersonalTask.class, TaskConfig.WORK_PACKAGE_DIR);
            for (Class clazz : clazzs) {
                initTask(clazz, tasks);
            }
        } catch (Exception e) {
            throw new TaskException("[TaskGenarator] generate error", e);
        }
        return tasks;
    }

    private void initTask(Class<Task> clazz, Map<String, Task> taskMap) throws Exception {
        Partition partition = Annotationer.classAnnotation(clazz, Partition.class);
        Preconditions.checkNotNull(partition);
        if (!partitioners.containsKey(partition.by())) {
            throw new RuntimeException(String.format("[TaskGenarator] error:no partitioner,\01partitioner name\02%s", partition.by()));
        }
        Partitioner partitioner = partitioners.get(partition.by());
        // partition meta set
        Set<TaskMeta.Meta> metaSet = partitioner.distributeBy(clazz, partition.value());

        // scheduled meta
        Scheduled scheduled = Annotationer.constructorAnnotation(clazz, Scheduled.class);
        TaskMeta.SchduleMeta schduleMeta = createSchedule(scheduled);

        // tasks
        List<Task> tasks = new ArrayList<Task>();
        for (TaskMeta.Meta meta : metaSet) {
            TaskMeta configuration = new TaskMeta(meta, schduleMeta);
            Task task = ReflectionUtil.defaultCreate(clazz);
            task.injectConfiguration(configuration);
            tasks.add(task);
        }
        fillTask(tasks, taskMap);
    }

    private TaskMeta.SchduleMeta createSchedule(Scheduled scheduled) {
        if (StringUtils.isNotBlank(scheduled.cron())) {
            return new TaskMeta.
                    SchduleMeta(new CronTrigger(scheduled.cron()));
        } else {
            return new TaskMeta.
                    SchduleMeta(scheduled.delay(), scheduled.period(), scheduled.unit(), scheduled.fixedRate());
        }
    }

    private void fillTask(Task task, Map<String, Task> taskMap) {
        String identify = task.getTaskMeta().getUnique();
        if (taskMap.containsKey(identify))
            throw new RuntimeException(String.format("[TaskInitializer] : you have same task:%s", identify));
        taskMap.put(identify, task);
    }

    private void fillTask(List<Task> tasks, Map<String, Task> taskMap) {
        for (Task task : tasks) fillTask(task, taskMap);
    }
}
