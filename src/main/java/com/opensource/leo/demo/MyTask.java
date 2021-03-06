package com.opensource.leo.demo;


import com.opensource.leo.localtask.annotation.*;
import com.opensource.leo.localtask.cron.Task;

import java.util.concurrent.TimeUnit;

@PersonalTask
@Executor(name = "MyExecutor")
@Partition
public class MyTask extends Task {
    @TaskUnique(group = "group", taskName = "task")
    @Scheduled(delay = 0, period = 1, unit = TimeUnit.SECONDS, fixedRate = true)
    public MyTask() {
    }
}
