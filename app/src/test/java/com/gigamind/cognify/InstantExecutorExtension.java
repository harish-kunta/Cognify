package com.gigamind.cognify;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.arch.core.executor.TaskExecutor;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 extension that forces Architecture Components to execute tasks
 * synchronously. This mirrors the behavior of InstantTaskExecutorRule from
 * AndroidX test libraries but works with the Jupiter API.
 */
public class InstantExecutorExtension implements BeforeEachCallback, AfterEachCallback {
    @Override
    public void beforeEach(ExtensionContext context) {
        ArchTaskExecutor.getInstance().setDelegate(new TaskExecutor() {
            @Override
            public void executeOnDiskIO(Runnable runnable) { runnable.run(); }

            @Override
            public void postToMainThread(Runnable runnable) { runnable.run(); }

            @Override
            public boolean isMainThread() { return true; }
        });
    }

    @Override
    public void afterEach(ExtensionContext context) {
        ArchTaskExecutor.getInstance().setDelegate(null);
    }
}
