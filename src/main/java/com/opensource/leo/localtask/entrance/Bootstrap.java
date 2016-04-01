package com.opensource.leo.localtask.entrance;

import com.google.common.collect.Sets;
import com.opensource.leo.localtask.cron.LocalTask;
import com.opensource.leo.localtask.cron.TaskRegister;
import com.opensource.leo.localtask.cron.TaskScheduler;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * User:leo.lx
 * Date:15/8/13
 */
public class Bootstrap {
    protected static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);
    private TaskRegister taskRegister;
    private TaskScheduler taskScheduler;

    public static void main(String[] args) {
        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.init();
        } catch (Throwable t) {
            logger.error("[Bootstrap] : init error", t);
            System.exit(1);
        }
        try {
            bootstrap.registerTasks(args);
        } catch (TaskException e) {
            logger.error("[Bootstrap] : registerTasks error", e);
            System.exit(1);
        } catch (Throwable t) {
            logger.error("[Bootstrap] :registerTasks error", t);
            System.exit(1);
        }
    }

    private void init() throws Exception {
        this.taskRegister = new TaskRegister();
        this.taskScheduler = new TaskScheduler(taskRegister);
    }

    private void registerTasks(String[] args) {
        try {
            Set<String> runTasks = new OptionsParser().parse(args);
            Map<String, LocalTask> tasks = new TaskInitializer().init();
            if (runTasks.isEmpty()) {
                for (LocalTask localTask : tasks.values()) {
                    localTask.init();
                    taskRegister.register(localTask);
                }
            } else {
                Set<String> intersection = Sets.intersection(runTasks, tasks.keySet());
                for (String task : intersection) {
                    LocalTask localTask = tasks.get(task);
                    localTask.init();
                    taskRegister.register(localTask);
                }
            }
            taskScheduler.init();
        } catch (Throwable t) {
            throw new TaskException("register task", t);
        }

    }
}
