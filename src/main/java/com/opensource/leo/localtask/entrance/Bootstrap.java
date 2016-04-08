package com.opensource.leo.localtask.entrance;

import com.opensource.leo.localtask.cron.Task;
import com.opensource.leo.localtask.cron.TaskRegister;
import com.opensource.leo.localtask.cron.TaskScheduler;
import com.opensource.leo.localtask.init.Initor;
import com.opensource.leo.localtask.init.InitorInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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
            bootstrap.registerTasks(args);
        } catch (TaskException e) {
            logger.error("[Bootstrap] : task error", e);
            System.exit(1);
        } catch (Throwable t) {
            logger.error("[Bootstrap] : error", t);
            System.exit(1);
        }
    }

    private void init() throws Exception {
        this.taskRegister = new TaskRegister();
        this.taskScheduler = new TaskScheduler(taskRegister);
        List<Initor> initors = new InitorInitializer().init();
        for (Initor initor : initors) {
            initor.init();
        }
    }

    private void registerTasks(String[] args) {
        try {
            Set<String> runTasks = new OptionsParser().parse(args);
            Map<String, Task> tasks = new TaskInitializer().init(runTasks);
            taskRegister.register(tasks.values());
            taskScheduler.begin();
        } catch (Throwable t) {
            throw new TaskException("[Bootstrap] : register task", t);
        }
    }
}
