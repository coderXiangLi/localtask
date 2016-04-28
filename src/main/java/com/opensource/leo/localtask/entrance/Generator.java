package com.opensource.leo.localtask.entrance;

/**
 * Created by leo.lx on 4/21/16.
 */
public interface Generator<T> {
    T generate() throws Exception;
}
