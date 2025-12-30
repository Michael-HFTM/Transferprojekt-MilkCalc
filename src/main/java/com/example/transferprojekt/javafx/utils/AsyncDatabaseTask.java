package com.example.transferprojekt.javafx.utils;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.layout.Region;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Generic helper class for executing database operations asynchronously
 * to prevent GUI freezing during long-running operations.
 *
 * @param <T> The return type of the database operation
 */
public class AsyncDatabaseTask<T> extends Task<T> {

    private final Callable<T> databaseOperation;

    /**
     * Creates a new asynchronous database task
     *
     * @param operation The database operation to execute
     */
    public AsyncDatabaseTask(Callable<T> operation) {
        this.databaseOperation = operation;
    }

    @Override
    protected T call() throws Exception {
        return databaseOperation.call();
    }

    /**
     * Executes the database operation with loading indicator and callbacks
     *
     * @param parent The parent region to show the loading overlay on
     * @param onSuccess Callback when operation succeeds
     * @param onError Callback when operation fails
     */
    public void executeWithProgress(
            Region parent,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError
    ) {
        // Show loading overlay
        LoadingOverlay overlay = new LoadingOverlay();
        overlay.show(parent);

        // Handle success
        setOnSucceeded(event -> {
            overlay.hide();
            if (onSuccess != null) {
                @SuppressWarnings("unchecked")
                T result = (T) event.getSource().getValue();
                onSuccess.accept(result);
            }
        });

        // Handle failure
        setOnFailed(event -> {
            overlay.hide();
            Throwable exception = event.getSource().getException();
            if (onError != null) {
                onError.accept(exception);
            }
        });

        // Execute in background thread
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Simplified version without loading overlay
     *
     * @param onSuccess Callback when operation succeeds
     * @param onError Callback when operation fails
     */
    public void execute(Consumer<T> onSuccess, Consumer<Throwable> onError) {
        setOnSucceeded(event -> {
            if (onSuccess != null) {
                @SuppressWarnings("unchecked")
                T result = (T) event.getSource().getValue();
                onSuccess.accept(result);
            }
        });

        setOnFailed(event -> {
            Throwable exception = event.getSource().getException();
            if (onError != null) {
                onError.accept(exception);
            }
        });

        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Static helper method to create and execute a task in one call
     *
     * @param operation The database operation
     * @param parent The parent region for loading overlay
     * @param onSuccess Success callback
     * @param onError Error callback
     * @param <T> Return type
     */
    public static <T> void run(
            Callable<T> operation,
            Region parent,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError
    ) {
        AsyncDatabaseTask<T> task = new AsyncDatabaseTask<>(operation);
        task.executeWithProgress(parent, onSuccess, onError);
    }

    /**
     * Static helper for void operations (no return value)
     *
     * @param operation The database operation
     * @param parent The parent region for loading overlay
     * @param onSuccess Success callback (no parameter)
     * @param onError Error callback
     */
    public static void runVoid(
            Runnable operation,
            Region parent,
            Runnable onSuccess,
            Consumer<Throwable> onError
    ) {
        AsyncDatabaseTask<Void> task = new AsyncDatabaseTask<>(() -> {
            operation.run();
            return null;
        });

        task.executeWithProgress(
                parent,
                result -> {
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                },
                onError
        );
    }
}