package com.opensource.leo.demo;

import com.opensource.leo.localtask.cron.LocalTask;
import com.opensource.leo.localtask.entrance.PersonalTask;

import java.util.concurrent.TimeUnit;

@PersonalTask
public class MyTask extends LocalTask {
    public MyTask() {
        super("group", "task", 0, 1, TimeUnit.SECONDS);
    }

    @Override
    protected boolean doTask() {
        logger.warn("i am running task1");
        return true;
    }
}
