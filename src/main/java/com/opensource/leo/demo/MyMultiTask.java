package com.opensource.leo.demo;

import com.opensource.leo.localtask.annotation.*;
import com.opensource.leo.localtask.cron.Task;

import java.util.concurrent.TimeUnit;

/**
 * Created by leo.lx on 4/28/16.
 */
@PersonalTask
@Executor(name = "MyExecutor")
@Partition(by = "Counter", value = "10")
public class MyMultiTask extends Task {
    @TaskUnique(group = "group", taskName = "multiTask")
    @Scheduled(delay = 0, period = 1, unit = TimeUnit.SECONDS, fixedRate = true)
    public MyMultiTask() {
    }
}
