package com.opensource.leo.localtask.cron;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: leo.lx
 * Date: 15-9-6
 */
public abstract class LocalTask extends Task {
    public LocalTask(String group, String key, int delay, int period, TimeUnit unit) {
        super(group, key, delay, period, unit, false);
    }

    public LocalTask(String group, String key, int delay, int period, TimeUnit unit, boolean fixedRate) {
        super(group, key, delay, period, unit, fixedRate);
    }

    @Override
    public final void init() {
        logger.warn(String.format("[Task]:%s\01preparing", getIdentify()));
        // do prepare
        prepare();
        logger.warn(String.format("[Task]:%s\01prepared", getIdentify()));
    }

    protected void prepare() {
    }

}
