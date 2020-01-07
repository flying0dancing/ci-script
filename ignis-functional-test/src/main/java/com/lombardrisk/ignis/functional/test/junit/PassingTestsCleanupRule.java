package com.lombardrisk.ignis.functional.test.junit;

import lombok.extern.slf4j.Slf4j;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.util.Arrays;

@Slf4j
public final class PassingTestsCleanupRule extends TestWatcher {

    private final Runnable cleanupSteps;

    private PassingTestsCleanupRule(final Runnable cleanupSteps) {
        this.cleanupSteps = cleanupSteps;
    }

    public static PassingTestsCleanupRule withSteps(final Runnable cleanupSteps) {
        return new PassingTestsCleanupRule(cleanupSteps);
    }

    @Override
    public void succeeded(final Description description) {
        try {
            log.info("Cleaning up");

            cleanupSteps.run();
        } catch (Exception | AssertionError e) {
            log.error("Error while cleaning up", e);
        }
    }

    @Override
    protected void failed(final Throwable e, final Description description) {
        StackTraceElement testMethodStackElement = findMethodNameStackTraceElement(e, description);

        log.error(
                "Cleanup skipped because @Test {} failed ({}:{})",
                description.getMethodName(),
                testMethodStackElement.getFileName(),
                testMethodStackElement.getLineNumber());
    }

    private static StackTraceElement findMethodNameStackTraceElement(final Throwable e, final Description description) {
        return Arrays.stream(e.getStackTrace())
                .filter(stackElem -> stackElem.getMethodName().contains(description.getMethodName()))
                .findFirst()
                .orElse(new StackTraceElement(
                        description.getClassName(),
                        description.getMethodName(),
                        description.getClassName() + ".java",
                        1));
    }
}
