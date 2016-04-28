package com.opensource.leo.localtask.entrance;

public class TaskException extends Exception {
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
