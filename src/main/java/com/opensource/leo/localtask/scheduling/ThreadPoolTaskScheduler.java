/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opensource.leo.localtask.scheduling;


import com.google.common.base.Preconditions;

import java.util.concurrent.*;

@SuppressWarnings("serial")
public class ThreadPoolTaskScheduler implements TaskSchedulerExecutor {

    public static final ErrorHandler ErrorHandler_default = new LoggingErrorHandler();
    public static final int PoolSize_default = 1;
    public static final RejectedExecutionHandler RejectedExecutionHandler_default =
            new ThreadPoolExecutor.AbortPolicy();

    private volatile int poolSize;
    private volatile ErrorHandler errorHandler;
    private volatile RejectedExecutionHandler rejectedExecutionHandler;
    private volatile ScheduledExecutorService scheduledExecutor;

    public ThreadPoolTaskScheduler() {
        this(PoolSize_default);
    }

    public ThreadPoolTaskScheduler(int poolSize) {
        this(poolSize, RejectedExecutionHandler_default, ErrorHandler_default);
    }

    public ThreadPoolTaskScheduler(int poolSize, ErrorHandler errorHandler) {
        this(poolSize, RejectedExecutionHandler_default, errorHandler);
    }

    public ThreadPoolTaskScheduler(int poolSize, RejectedExecutionHandler rejectedExecutionHandler) {
        this(poolSize, rejectedExecutionHandler, ErrorHandler_default);
    }

    public ThreadPoolTaskScheduler(int poolSize, RejectedExecutionHandler rejectedExecutionHandler, ErrorHandler errorHandler) {
        Preconditions.checkArgument(poolSize > 0, "pool size is less than 0");
        Preconditions.checkNotNull(rejectedExecutionHandler, "rejectedExecutionHandler is null");
        Preconditions.checkNotNull(errorHandler, "errorHandler is null");
        this.poolSize = poolSize;
        this.rejectedExecutionHandler = rejectedExecutionHandler;
        this.errorHandler = errorHandler;
        this.scheduledExecutor = createExecutor(this.poolSize, this.rejectedExecutionHandler);
    }


    protected ScheduledExecutorService createExecutor(int poolSize, RejectedExecutionHandler rejectedExecutionHandler) {
        return new ScheduledThreadPoolExecutor(poolSize, rejectedExecutionHandler);
    }

    /**
     * Return the underlying ScheduledExecutorService for native access.
     *
     * @return the underlying ScheduledExecutorService (never {@code null})
     * @throws IllegalStateException if the ThreadPoolTaskScheduler hasn't been initialized yet
     */
    public ScheduledExecutorService getScheduledExecutor() throws IllegalStateException {
        Preconditions.checkState(this.scheduledExecutor != null, "ThreadPoolTaskScheduler not initialized");
        return this.scheduledExecutor;
    }


    public ScheduledFuture schedule(Runnable task, Trigger trigger) {
        ScheduledExecutorService executor = getScheduledExecutor();
        try {
            return new ReschedulingRunnable(task, trigger, executor, errorHandler).schedule();
        } catch (RejectedExecutionException ex) {
            throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
        }
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        ScheduledExecutorService executor = getScheduledExecutor();
        try {
            return executor.scheduleAtFixedRate(errorHandlingTask(command), initialDelay, period, unit);
        } catch (RejectedExecutionException ex) {
            throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + command, ex);
        }
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        ScheduledExecutorService executor = getScheduledExecutor();
        try {
            return executor.scheduleWithFixedDelay(errorHandlingTask(command), initialDelay, delay, unit);
        } catch (RejectedExecutionException ex) {
            throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + command, ex);
        }
    }

    @Override
    public void shutdown() {
        ScheduledExecutorService executor = getScheduledExecutor();
        executor.shutdown();
    }

    private Runnable errorHandlingTask(Runnable task) {
        if (task instanceof DelegatingErrorHandlingRunnable) {
            return task;
        }
        return new DelegatingErrorHandlingRunnable(task, errorHandler);
    }
}
