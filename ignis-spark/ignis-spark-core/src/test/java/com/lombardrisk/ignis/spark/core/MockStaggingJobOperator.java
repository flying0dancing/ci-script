package com.lombardrisk.ignis.spark.core;

import org.springframework.stereotype.Component;

@Component
public class MockStaggingJobOperator implements JobOperator {

    @Override
    public void runJob() {
        //do nothing for test
    }
}
