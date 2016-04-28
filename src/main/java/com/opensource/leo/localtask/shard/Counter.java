package com.opensource.leo.localtask.shard;

import com.google.common.base.Preconditions;
import com.opensource.leo.localtask.annotation.Annotationer;
import com.opensource.leo.localtask.annotation.PersonalTask;
import com.opensource.leo.localtask.annotation.TaskUnique;
import com.opensource.leo.localtask.cron.Task;
import com.opensource.leo.localtask.cron.TaskMeta;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.util.HashSet;
import java.util.Set;


/**
 * Created by leo.lx on 4/22/16.
 */
@PersonalTask
public class Counter implements Partitioner {
    /**
     * @param value number of partition count
     */
    @Override
    public Set<TaskMeta.Meta> distributeBy(Class<Task> clazz, String value) {
        int threadCount = NumberUtils.toInt(value);
        TaskUnique taskUnique = Annotationer.constructorAnnotation(clazz, TaskUnique.class);
        String group = taskUnique.group();
        String taskName = taskUnique.taskName();
        Preconditions.checkArgument(StringUtils.isNotBlank(group), "group is blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(taskName), "taskName is blank");

        int partitonCount = NumberUtils.toInt(System.getProperty("partitionCount"), 1);
        int partitonNum = NumberUtils.toInt(System.getProperty("partitionNum"), 0);

        Set<TaskMeta.Meta> metaSet = new HashSet<TaskMeta.Meta>();
        for (int i = 0; i < threadCount; i++) {
            TaskMeta.PartitionMeta partitionMeta = new TaskMeta.PartitionMeta(partitonCount, partitonNum, "one patition", i, "thread");
            TaskMeta.Meta meta = new TaskMeta.Meta(group, taskName, partitionMeta);
            metaSet.add(meta);
        }
        return metaSet;
    }
}
