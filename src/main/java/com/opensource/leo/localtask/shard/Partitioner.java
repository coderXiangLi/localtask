package com.opensource.leo.localtask.shard;


import com.opensource.leo.localtask.cron.Task;
import com.opensource.leo.localtask.cron.TaskMeta;

import java.util.Set;

/**
 * Created by leo.lx on 4/22/16.
 */
public interface Partitioner {
    Set<TaskMeta.Meta> distributeBy(Class<Task> clazz, String value);
}
