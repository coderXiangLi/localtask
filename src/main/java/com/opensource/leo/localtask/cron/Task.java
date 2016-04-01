package com.opensource.leo.localtask.cron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: leo.lx
 * Date: 14-08-13
 */
public abstract class Task implements Runnable {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    public static final int PERIOD_DEFAULT = 30;
    private static final int DELAY_DEFAULT = 1;
    public static final TimeUnit TIMEUNIT_DEFAULT = TimeUnit.SECONDS;
    private String group = "default_group";
    private String taskName = "default_name";
    private int delay;
    private int period;
    private TimeUnit unit;
    private boolean fixedRate;
    private AtomicBoolean running;
    private volatile TaskStatus status;
    private Lock lock = new ReentrantLock();

    public Task() {
    }

    public Task(String group, String taskName) {
        this(group, taskName, DELAY_DEFAULT, PERIOD_DEFAULT);
    }

    public Task(String group, String taskName, int delay, int period) {
        this(group, taskName, delay, period, TIMEUNIT_DEFAULT);
    }

    public Task(String group, String taskName, int delay, int period, TimeUnit unit) {
        this(group, taskName, delay, period, unit, false);
    }

    public void run() {
        // thread variable
        MDC.put("task", getIdentify());
        long begin = System.currentTimeMillis();
        boolean success = true;
        // begin to run
        logger.warn(String.format("warn\01start\02%s\01thread\02%s", getIdentify(), Thread.currentThread().getName()));
        try {
            // none task is running & task is ready
            if (running.compareAndSet(false, true) && this.compareAndSet(TaskStatus.READY, TaskStatus.RUNNING)) {
                success = doTask();
            } else {
                success = false;
                logger.warn(String.format("running\01task\02%s\01message\02running now", getIdentify()));
            }
        } catch (Throwable t) {
            success = false;
            logger.error(String.format("error\01task\02%s", getIdentify()), t);
        } finally {
            running.compareAndSet(true, false);
            this.compareAndSet(TaskStatus.RUNNING, TaskStatus.READY);
        }
        long end = System.currentTimeMillis();
        logger.warn(String.format("access\01job\02%s\01ms\02%s\01success\02%s", getIdentify(), end - begin, success));
        MDC.remove("task");
    }

    /**
     * do task
     *
     * @return isSuccess
     */
    protected abstract boolean doTask();

    /**
     * @param expect
     * @param update
     * @return if current status is the same with expect, return true;else return false
     */
    public boolean compareAndSet(TaskStatus expect, TaskStatus update) {
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

    /**
     * get unique id
     *
     * @return identify
     */
    public String getIdentify() {
        if (getTaskName() == null) return getGroup();
        return getGroup() + "-" + getTaskName();
    }

    /**
     * 判断任务是否正在执行
     *
     * @return 是否正在运行
     */
    public boolean isRunning() {
        return running.get();
    }

    public String getTaskName() {
        return taskName;
    }

    public TaskStatus getStatus() {
        return status;
    }

    void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getGroup() {
        return group;
    }

    public int getDelay() {
        return delay;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public int getPeriod() {
        return period;
    }

    public boolean isFixedRate() {
        return fixedRate;
    }

    public Task(String group, String taskName, int delay, int period, TimeUnit unit, boolean fixedRate) {
        this.group = group;
        this.taskName = taskName;
        this.delay = delay;
        this.period = period;
        this.unit = unit;
        this.fixedRate = fixedRate;
        this.running = new AtomicBoolean(false);
        this.status = TaskStatus.WAIT;
    }

    @Override
    public int hashCode() {
        int result = group != null ? group.hashCode() : 0;
        result = 31 * result + (taskName != null ? taskName.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Task) {
            Task task = (Task) obj;
            if (task.hashCode() == this.hashCode()) {
                // group + taskName相同，则认为equals
                return this.getIdentify().equals(task.getIdentify());
            }
        }
        return false;
    }
}
