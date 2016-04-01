package com.opensource.leo.localtask.entrance;

/**
 * Bts Sdk 异常
 * User:leo.lx
 * Date:15/8/18
 * Time:下午2:06
 */
public class TaskException extends RuntimeException {
    private static final long serialVersionUID = -6116290849361250558L;

    public TaskException(Throwable t) {
        super(t);
    }

    public TaskException(String msg) {
        super(msg);
    }

    public TaskException(String msg, Throwable t) {
        super(msg, t);
    }
}
