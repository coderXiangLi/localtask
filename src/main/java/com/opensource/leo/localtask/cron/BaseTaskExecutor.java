package com.opensource.leo.localtask.cron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by leo.lx on 4/20/16.
 */
public abstract class BaseTaskExecutor implements TaskExecutor {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public final void init(Task task) throws Exception {
        String unique = task.getTaskMeta().getUnique();
        logger.warn(String.format("[Task]:%s\01preparing", unique));
        // do prepare
        prepare(task);
        logger.warn(String.format("[Task]:%s\01prepared", unique));
    }

    protected abstract void prepare(Task task);
}
