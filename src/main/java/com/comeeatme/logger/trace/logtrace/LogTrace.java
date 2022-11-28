package com.comeeatme.logger.trace.logtrace;

import com.comeeatme.logger.trace.TraceStatus;

public interface LogTrace {

    TraceStatus begin(String message);
    void end(TraceStatus status);
    void exception(TraceStatus status, Exception e);
}
