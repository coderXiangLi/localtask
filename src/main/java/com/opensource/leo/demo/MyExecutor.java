package com.opensource.leo.demo;


import com.opensource.leo.localtask.annotation.PersonalTask;
import com.opensource.leo.localtask.cron.BaseTaskExecutor;
import com.opensource.leo.localtask.cron.Task;

/**
 * Created by leo.lx on 4/28/16.
 */
@PersonalTask
public class MyExecutor extends BaseTaskExecutor {
    @Override
    protected void prepare(Task task) {

    }

    @Override
    public boolean execute(Task task) {
        logger.info("i am running");
        return true;
    }

    @Override
    public String name() {
        return "MyExecutor";
    }
}
