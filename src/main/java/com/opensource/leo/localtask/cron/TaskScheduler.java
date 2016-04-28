package com.opensource.leo.localtask.cron;

import com.opensource.leo.localtask.scheduling.ThreadPoolTaskScheduler;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: leo.lx
 * Date: 15-08-13
 */
public final class TaskScheduler {
    private static Logger logger = LoggerFactory.getLogger(TaskScheduler.class);
    private static final int CORE_COUNT = NumberUtils.toInt(System.getProperty("core")) > 0 ?
            NumberUtils.toInt(System.getProperty("core")) : Runtime.getRuntime().availableProcessors();
    private AtomicInteger taskCounter = new AtomicInteger(0);

    private ThreadPoolTaskScheduler executor;
    private Map<String, ScheduledFuture<?>> taskFutures = new HashMap<String, ScheduledFuture<?>>();
    private TaskRegister taskRegister;

    public TaskScheduler(TaskRegister taskRegister) {
        this(taskRegister, CORE_COUNT);
    }

    public TaskScheduler(TaskRegister taskRegister, int corePoolSize) {
        this.taskRegister = taskRegister;
        this.executor = new ThreadPoolTaskScheduler(corePoolSize);
    }


    public ScheduledFuture<?> getTaskFuture(String identify) {
        return getTaskFutures().get(identify);
    }

    public ScheduledFuture<?> removeTaskFuture(String identify) {
        return getTaskFutures().remove(identify);
    }

    public Map<String, ScheduledFuture<?>> getTaskFutures() {
        return taskFutures;
    }

    public int getTaskCount() {
        return taskCounter.get();
    }

    /**
     * get unsubmited task from task_register every 15 min
     */
    public void begin() {
        logger.warn("TaskSchedulerExecutor is initiating!");
        Task task = newFlushTask();
        submitTask(task);
        logger.warn("TaskSchedulerExecutor has been initialized!");
    }

    Task newFlushTask() {
        TaskExecutor executor = new TaskExecutor() {
            @Override
            public void init(Task task) throws Exception {

            }

            @Override
            public boolean execute(Task task) {
                logger.warn("flushing");
                flushTasks();
                logger.warn("flushed");
                return true;
            }

            @Override
            public String name() {
                return "flush";
            }
        };
        TaskMeta.Meta meta = new TaskMeta.Meta("scheduler", "scheduler_task_flush");
        TaskMeta.SchduleMeta schduleMeta = new TaskMeta.SchduleMeta(0, 15, TimeUnit.SECONDS, true);
        TaskMeta configuration = new TaskMeta(meta, schduleMeta);
        Task task = new Task(configuration);
        task.setTaskExecutor(executor);
        task.compareAndSet(TaskStatus.WAIT, TaskStatus.READY);
        return task;
    }


    boolean flushTasks() {
        int current = taskCounter.get();
        AtomicInteger submitCounter = new AtomicInteger(0);
        AtomicInteger cancelCounter = new AtomicInteger(0);
        Collection<Set<Task>> tasks = taskRegister.getTaskCollection();
        if (tasks != null && !tasks.isEmpty()) {
            for (Set<Task> each : tasks) {
                if (each != null && !each.isEmpty()) {
                    for (Task item : each) {
                        if (item.compareAndSet(TaskStatus.WAIT, TaskStatus.READY)) {
                            ScheduledFuture<?> future = submitTask(item);
                            if (future != null) {
                                submitCounter.incrementAndGet();
                            }
                        } else if (item.getStatus() == TaskStatus.CANCEL) {
                            boolean success = cancelTask(item);
                            if (success) {
                                // remove task
                                taskRegister.delete(item);
                                cancelCounter.incrementAndGet();
                            }
                        }
                    }
                }
            }
            logger.warn(String.format("before\02%d\01submit\02%d\01cancel\02%d\01now\02%d", current, submitCounter.get(),
                    cancelCounter.get(), taskCounter.get()));
            return taskRegister.size() > 0 && submitCounter.get() + cancelCounter.get() == taskRegister.size();
        }
        return false;
    }

    /**
     * submit task to executor
     *
     * @param task
     * @return isSucess
     */
    ScheduledFuture<?> submitTask(Task task) {
        ScheduledFuture<?> future = null;
        if (task != null) {
            TaskMeta meta = task.getTaskMeta();
            if (meta.isTrigger()) {
                future = executor.schedule(task, meta.getTrigger());
            } else if (meta.isFixedRate()) {
                future = executor.scheduleAtFixedRate(task, meta.getDelay(), meta.getPeriod(), meta.getUnit());
            } else {
                future = executor.scheduleWithFixedDelay(task, meta.getDelay(), meta.getPeriod(), meta.getUnit());
            }
            taskCounter.incrementAndGet();
            taskFutures.put(meta.getUnique(), future);
        }
        return future;
    }

    boolean cancelTask(Task task) {
        if (task != null) {
            return cancelTaskAndRemove(task);
        }
        return false;
    }

    /**
     * stop task：stop the running task
     *
     * @param task
     * @return isSuccess
     */
    boolean cancelTaskAndRemove(Task task) {
        if (task != null) {
            String identify = task.getTaskMeta().getUnique();
            logger.warn(String.format("%s\02finishTask", identify));
            ScheduledFuture<?> future = getTaskFuture(identify);
            if (future != null && !future.isDone()) {
                future.cancel(true);
                removeTaskFuture(identify);
                return future.isCancelled();
            }
        }
        return false;
    }
}
