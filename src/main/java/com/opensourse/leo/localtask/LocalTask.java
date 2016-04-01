package com.opensourse.leo.localtask;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: leo.lx
 * Date: 15-9-6
 */
public abstract class LocalTask extends Task {
    private AtomicBoolean inited = new AtomicBoolean(false);

    public final void init() {
        if (inited.compareAndSet(false, true)) {
            logger.warn(String.format("[Task]:%s\01preparing", getIdentify()));
            // do prepare
            prepare();
            logger.warn(String.format("[Task]:%s\01prepared", getIdentify()));
        }
    }

    protected void prepare() {
    }

}
