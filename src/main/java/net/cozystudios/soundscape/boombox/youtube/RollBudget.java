package net.cozystudios.soundscape.boombox.youtube;

import java.util.function.LongSupplier;

public final class RollBudget {

    private final int capacity;
    private final long refillIntervalMs;
    private final LongSupplier clockMs;

    private int tokens;
    private long lastRefillMs;

    public RollBudget(int capacity, long refillIntervalMs, LongSupplier clockMs) {
        this.capacity = capacity;
        this.refillIntervalMs = refillIntervalMs;
        this.clockMs = clockMs;
        this.tokens = capacity;
        this.lastRefillMs = clockMs.getAsLong();
    }

    public synchronized boolean take() {
        refill();
        if (tokens <= 0) return false;
        tokens--;
        return true;
    }

    private void refill() {
        long now = clockMs.getAsLong();
        long elapsed = now - lastRefillMs;
        if (elapsed < refillIntervalMs) return;
        long granted = elapsed / refillIntervalMs;
        tokens = (int) Math.min(capacity, tokens + granted);
        lastRefillMs += granted * refillIntervalMs;
    }
}
