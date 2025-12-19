package com.ramblingpenguin.icefloe.context;

public interface SequenceStateService {

    void beginExecution(SequenceContext context);

    void endExecution(SequenceContext context);
}
