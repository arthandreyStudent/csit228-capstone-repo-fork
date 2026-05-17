
package com.csit228.capstone.observer;

import java.util.List;
import java.util.concurrent.*;

public abstract class BaseWatcher<O> {

    protected final List<O> observers = new CopyOnWriteArrayList<>();

    private final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, getThreadName());
                t.setDaemon(true);
                return t;
            });

    private ScheduledFuture<?> task;

    protected abstract String getThreadName();

    protected abstract void fetchAndNotify();

    public void addObserver(O observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(O observer) {
        observers.remove(observer);
        if (observers.isEmpty()) stop();
    }

    public void start(int intervalSeconds) {
        if (task != null && !task.isCancelled()) return;
        int safeInterval = Math.max(1, intervalSeconds);
        task = executor.scheduleAtFixedRate(this::fetchAndNotify, safeInterval, safeInterval, TimeUnit.SECONDS);
    }

    public void stop() {
        if (task != null) task.cancel(false);
    }

    public void shutdown() {
        stop();
        executor.shutdownNow();
    }
}