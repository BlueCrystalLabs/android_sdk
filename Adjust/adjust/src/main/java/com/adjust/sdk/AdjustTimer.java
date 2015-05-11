package com.adjust.sdk;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by pfms on 08/05/15.
 */
public class AdjustTimer {
    private ScheduledExecutorService scheduler;
    private ScheduledFuture waitingTask;
    private Runnable command;
    private long initialDelay;
    private Long cycleMilli;
    private long fireIn;

    public AdjustTimer(ScheduledExecutorService scheduler, Runnable command, long initialDelay, Long delay) {
        if (scheduler == null) {
            this.scheduler = Executors.newSingleThreadScheduledExecutor();
        } else {
            this.scheduler = scheduler;
        }
        this.command = command;
        this.initialDelay = initialDelay;
        this.cycleMilli = delay;
        setFireIn(0);
    }

    public AdjustTimer(ScheduledExecutorService scheduler, Runnable command) {
        this(scheduler, command, 0, null);
    }

    public AdjustTimer(Runnable command, long initialDelay, Long delay) {
        this(null, command, initialDelay, delay);
    }

    public void resume() {
        long start;
        if (fireIn > 0) {
            start = fireIn;
        } else {
            start = initialDelay;
        }

        if (cycleMilli == null) {
            waitingTask = scheduler.schedule(command, start, TimeUnit.MILLISECONDS);
        } else {
            waitingTask = scheduler.scheduleWithFixedDelay(command, start, cycleMilli, TimeUnit.MILLISECONDS);
        }
    }

    public void suspend() {
        // get the time until the event is fired
        long savedFireIn = getFireIn();

        cancel();

        // save the time left until the timer is resumed
        setFireIn(savedFireIn);
    }

    public void cancel() {
        if (waitingTask == null) {
            return;
        }
        // cancel the timer
        waitingTask.cancel(false);
        waitingTask = null;
        // reset the time of the next fire
        setFireIn(0);
    }

    public long getFireIn() {
        if (waitingTask == null) {
            return 0;
        }
        return waitingTask.getDelay(TimeUnit.MILLISECONDS);
    }

    public void setFireIn(long fireIn) {
        if (fireIn < 0) {
            this.fireIn = 0;
            return;
        }
        this.fireIn = fireIn;
    }
}
