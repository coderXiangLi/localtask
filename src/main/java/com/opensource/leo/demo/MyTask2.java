package com.opensource.leo.demo;

import com.opensource.leo.localtask.cron.LocalTask;
import com.opensource.leo.localtask.entrance.PersonalTask;

import java.util.concurrent.TimeUnit;

@PersonalTask
public class MyTask2 extends LocalTask {
    public MyTask2() {
        super("group", "task2", 0, 1, TimeUnit.SECONDS);
    }

    @Override
    protected boolean doTask() {
        logger.warn("i am running task2");
        return true;
    }
}
