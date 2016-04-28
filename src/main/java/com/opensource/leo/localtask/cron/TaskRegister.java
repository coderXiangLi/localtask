package com.opensource.leo.localtask.cron;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: leo.lx
 * Date: 15-08-13
 */
public class TaskRegister {
    private static Logger logger = LoggerFactory.getLogger(TaskRegister.class);
    private static final int MAX = 10000;
    private int maxSize = MAX;
    private AtomicInteger size = new AtomicInteger(0);
    private AtomicInteger available = new AtomicInteger(0);
    private ConcurrentMap<String, Set<Task>> tasks = new ConcurrentHashMap<String, Set<Task>>();

    public boolean register(Task task) throws Exception {
        if (task != null) {
            task.init();
            return register(task.getTaskMeta().getGroup(), task);
        }
        return false;
    }

    public boolean register(Collection<Task> tasks) throws Exception {
        for (Task task : tasks) {
            if (task != null) {
                task.init();
                if (!register(task.getTaskMeta().getGroup(), task)) return false;
            }
        }
        return true;
    }

    /**
     * add task to corresponding group
     *
     * @param group
     * @param task
     * @return isSucess
     */
    public boolean register(String group, Task task) {
        if (size.get() >= maxSize) {
            logger.warn(String.format("[TASK-REGISTER]failed to submit，number of tasks has reach top %d", MAX));
            return false;
        }
        if (StringUtils.isNotBlank(group) && task != null) {
            Set<Task> queue = getGroupTasks(group);
            if (queue == null) {
                queue = new HashSet<Task>();
                putGroupTasks(group, queue);
            }
            if (queue.contains(task)) {
                logger.warn(String.format("[TASK-REGISTER]task:%s has already been registered", task));
                return false;
            }
            size.incrementAndGet();
            available.incrementAndGet();
            return queue.add(task);
        }
        return false;
    }

    public boolean cancel(Task task) {
        if (task != null) {
            return cancel(task.getTaskMeta().getGroup(), task);
        }
        return false;
    }

    public boolean cancel(String group, Task task) {
        if (task != null) {
            Set<Task> queue = getGroupTasks(group);
            if (queue == null || !queue.contains(task)) {
                logger.warn(String.format("[TASK-REGISTER]task:%s has not been registered", task));
                return false;
            }
            task.setStatus(TaskStatus.CANCEL);
            available.decrementAndGet();
            return true;
        }
        return false;
    }

    public boolean delete(Task task) {
        if (task != null) {
            return delete(task.getTaskMeta().getGroup(), task);
        }
        return false;
    }

    boolean delete(String group, Task task) {
        Set<Task> queue = getGroupTasks(group);
        if (queue == null || !queue.contains(task)) {
            logger.warn(String.format("[TASK-REGISTER]task:%s has not been registered", task));
            return false;
        }
        size.decrementAndGet();
        if (task.getStatus() != TaskStatus.CANCEL) {
            available.decrementAndGet();
        }
        return queue.remove(task);
    }

    public Task getTask(String group, String key) {
        Set<Task> queue = getGroupTasks(group);
        if (queue != null && StringUtils.isNotBlank(key)) {
            for (Task each : queue) {
                if (key.equals(each.getTaskMeta().getTaskName())) {
                    return each;
                }
            }
        }
        return null;
    }

    boolean putGroupTasks(String group, Set<Task> queue) {
        if (queue != null && StringUtils.isNotBlank(group)) {
            // 取所有任务
            ConcurrentMap<String, Set<Task>> tasks = getAllTasks();
            tasks.putIfAbsent(group, queue);
            return true;
        }
        return false;
    }

    public Set<Task> getGroupTasks(String group) {
        if (StringUtils.isNotBlank(group)) {
            ConcurrentMap<String, Set<Task>> tasks = getAllTasks();
            Set<Task> queue = tasks.get(group);
            if (queue != null) {
                return queue;
            }
        }
        return null;
    }

    public ConcurrentMap<String, Set<Task>> getAllTasks() {
        return tasks;
    }

    public Collection<Set<Task>> getTaskCollection() {
        return getAllTasks().values();
    }

    public Set<String> getAllTaskName() {
        Set<String> names = new HashSet<String>();
        Collection<Set<Task>> collection = getTaskCollection();
        for (Set<Task> set : collection) {
            for (Task task : set) {
                names.add(task.getTaskMeta().getUnique());
            }
        }
        return names;
    }

    public int groupSize() {
        return getAllTasks().size();
    }

    public int size() {
        return size.get();
    }

    public int available() {
        return available.get();
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
}
