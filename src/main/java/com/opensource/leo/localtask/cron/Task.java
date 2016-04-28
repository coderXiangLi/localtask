package com.opensource.leo.localtask.cron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: leo.lx
 * Date: 14-08-13
 */
public class Task implements Runnable {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    private TaskExecutor taskExecutor;
    private TaskMeta taskMeta;
    private volatile AtomicBoolean running;
    private volatile TaskStatus status;
    private volatile Lock lock = new ReentrantLock();

    public Task() {
        this.running = new AtomicBoolean(false);
        this.status = TaskStatus.WAIT;
    }

    public Task(TaskMeta taskMeta) {
        this();
        this.taskMeta = taskMeta;
    }

    public void run() {
        String unique = taskMeta.getUnique();

        // thread variable
        MDC.put("task", unique);
        long begin = System.currentTimeMillis();
        boolean success = true;
        // begin to run
        logger.warn(String.format("warn\01start\02%s\01thread\02%s", unique, Thread.currentThread().getName()));
        try {
            // none task is running & task is ready
            if (running.compareAndSet(false, true) && this.compareAndSet(TaskStatus.READY, TaskStatus.RUNNING)) {
                success = taskExecutor.execute(this);
            } else {
                success = false;
                logger.warn(String.format("running\01task\02%s\01message\02running now", unique));
            }
        } catch (Throwable t) {
            success = false;
            logger.error(String.format("error\01task\02%s", unique), t);
        } finally {
            running.compareAndSet(true, false);
            this.compareAndSet(TaskStatus.RUNNING, TaskStatus.READY);
        }
        long end = System.currentTimeMillis();
        logger.warn(String.format("access\01job\02%s\01ms\02%s\01success\02%s", unique, end - begin, success));
        MDC.remove("task");
    }

    final void init() throws Exception {
        taskExecutor.init(this);
    }

    /**
     * @param expect
     * @param update
     * @return if current status is the same with expect, return true;else return false
     */
    boolean compareAndSet(TaskStatus expect, TaskStatus update) {
        boolean success = false;
        try {
            lock.lock();
            if (expect == getStatus()) {
                setStatus(update);
                success = true;
            }
        } finally {
            lock.unlock();
        }
        return success;
    }

    public boolean isRunning() {
        return running.get();
    }

    public TaskMeta getTaskMeta() {
        return taskMeta;
    }

    public TaskStatus getStatus() {
        return status;
    }

    void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void injectConfiguration(TaskMeta taskMeta) {
        this.taskMeta = taskMeta;
    }

    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }
}
