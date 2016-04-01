package com.opensourse.leo.localtask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: leo.lx
 * Date: 15-08-13
 */
public final class TaskScheduler {

    private static Logger logger = LoggerFactory.getLogger(TaskScheduler.class);
    private static final int CORE_COUNT = Runtime.getRuntime().availableProcessors();
    private static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(CORE_COUNT);
    private static Map<String, ScheduledFuture<?>> taskFutures = new HashMap<String, ScheduledFuture<?>>();
    private static AtomicInteger taskCounter = new AtomicInteger(0);
    private TaskRegister taskRegister;

    public TaskScheduler(TaskRegister taskRegister) {
        this.taskRegister = taskRegister;
    }

    public static ScheduledFuture<?> getTaskFuture(String identify) {
        return getTaskFutures().get(identify);
    }

    public static ScheduledFuture<?> removeTaskFuture(String identify) {
        return getTaskFutures().remove(identify);
    }

    public static Map<String, ScheduledFuture<?>> getTaskFutures() {
        return taskFutures;
    }

    public static int getTaskCount() {
        return taskCounter.get();
    }

    /**
     * get unsubmited task from task_register every 15 min
     */
    public void init() {
        logger.warn("TaskScheduler is initiating!");
        Task task = newFlushTask();
        submitTask(task);
        logger.warn("TaskScheduler has been initialized!");
    }

    Task newFlushTask() {
        Task task = new Task() {
            {
                initial("scheduler", "scheduler_task_flush", 0, 15);
            }

            @Override
            protected boolean doTask() {
                logger.warn("flushing");
                flushTasks();
                logger.warn("flushed");
                return true;
            }
        };
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
            if (task.isFixedRate()) {
                future = executor.scheduleAtFixedRate(task, task.getDelay(), task.getPeriod(), task.getUnit());
            } else {
                future = executor.scheduleWithFixedDelay(task, task.getDelay(), task.getPeriod(), task.getUnit());
            }
            taskCounter.incrementAndGet();
            taskFutures.put(task.getIdentify(), future);
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
            logger.warn(String.format("%s\02finishTask", task.getIdentify()));
            ScheduledFuture<?> future = getTaskFuture(task.getIdentify());
            if (future != null && !future.isDone()) {
                future.cancel(true);
                removeTaskFuture(task.getIdentify());
                return future.isCancelled();
            }
        }
        return false;
    }
}
