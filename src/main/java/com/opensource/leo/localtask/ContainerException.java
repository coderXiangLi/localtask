package com.opensource.leo.localtask;

/**
 * Bts Sdk 异常
 * User:leo.lx
 * Date:15/8/18
 * Time:下午2:06
 */
public class ContainerException extends RuntimeException {
    private static final long serialVersionUID = -6116290849361250558L;

    public ContainerException(Throwable t) {
        super(t);
    }

    public ContainerException(String msg) {
        super(msg);
    }

    public ContainerException(String msg, Throwable t) {
        super(msg, t);
    }
}
