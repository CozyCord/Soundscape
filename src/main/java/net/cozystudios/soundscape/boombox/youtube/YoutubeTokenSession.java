package net.cozystudios.soundscape.boombox.youtube;

import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.http.YoutubeAccessTokenTracker;
import net.cozystudios.soundscape.Soundscape;

public final class YoutubeTokenSession {

    public static final int DEFAULT_BURST = 8;
    public static final long DEFAULT_REFILL_MS = 7_500L;

    private final YoutubeAudioSourceManager manager;
    private final RollBudget budget;

    private volatile long generation;

    public YoutubeTokenSession(YoutubeAudioSourceManager manager, RollBudget budget) {
        this.manager = manager;
        this.budget = budget;
    }

    public YoutubeTokenSession(YoutubeAudioSourceManager manager) {
        this(manager, new RollBudget(DEFAULT_BURST, DEFAULT_REFILL_MS, System::currentTimeMillis));
    }

    public long generation() {
        return generation;
    }

    public synchronized boolean rollIfStale(long seen) {
        if (seen != generation) {
            return true;
        }
        if (!budget.take()) {
            Soundscape.LOGGER.warn("YouTube session rotation budget exhausted; giving up");
            return false;
        }
        try {
            manager.getContextFilter().setTokenTracker(
                    new YoutubeAccessTokenTracker(manager.getHttpInterfaceManager()));
        } catch (Throwable t) {
            Soundscape.LOGGER.warn("Failed to rotate YouTube session", t);
            return false;
        }
        generation++;
        Soundscape.LOGGER.info("Rotated YouTube session (generation {})", generation);
        return true;
    }
}
