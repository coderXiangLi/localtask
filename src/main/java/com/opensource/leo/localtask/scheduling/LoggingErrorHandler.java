package com.opensource.leo.localtask.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingErrorHandler implements ErrorHandler {
    private final static Logger logger = LoggerFactory.getLogger(LoggingErrorHandler.class);

    public void handleError(Throwable t) {
        if (logger.isErrorEnabled()) {
            logger.error("Unexpected error occurred in scheduled task.", t);
        }
    }
}