package com.opensource.leo.localtask.cron;

/**
 * Created by leo.lx on 4/20/16.
 */
public interface TaskExecutor {
    /**
     * task init
     *
     * @throws Exception the container will shutdown
     */
    void init(Task task) throws Exception;

    /**
     * schedule do
     */
    boolean execute(Task task);

    /**
     * name of executor
     */
    String name();
}
