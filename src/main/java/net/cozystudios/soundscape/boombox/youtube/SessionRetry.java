package net.cozystudios.soundscape.boombox.youtube;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.cozystudios.soundscape.Soundscape;

public final class SessionRetry {

    public static final int DEFAULT_MAX_ATTEMPTS = 5;
    public static final long DEFAULT_DEADLINE_MS = 20_000L;
    public static final long DEFAULT_BACKOFF_MS = 300L;

    private SessionRetry() {}

    public static void loadWithRetry(
            AudioPlayerManager playerManager,
            YoutubeTokenSession session,
            String identifier,
            AudioLoadResultHandler delegate
    ) {
        attempt(playerManager, session, identifier, delegate, 1,
                DEFAULT_MAX_ATTEMPTS, DEFAULT_DEADLINE_MS, DEFAULT_BACKOFF_MS,
                System.currentTimeMillis());
    }

    private static void attempt(
            AudioPlayerManager playerManager,
            YoutubeTokenSession session,
            String identifier,
            AudioLoadResultHandler delegate,
            int attempt,
            int maxAttempts,
            long deadlineMs,
            long backoffMs,
            long startedMs
    ) {
        long seenGen = session.generation();
        playerManager.loadItem(identifier, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                delegate.trackLoaded(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                delegate.playlistLoaded(playlist);
            }

            @Override
            public void noMatches() {
                delegate.noMatches();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                if (!isRetryable(exception)) {
                    delegate.loadFailed(exception);
                    return;
                }
                if (attempt >= maxAttempts) {
                    Soundscape.LOGGER.warn("YouTube load gave up after {} attempts: {}", attempt, exception.getMessage());
                    delegate.loadFailed(exception);
                    return;
                }
                long elapsed = System.currentTimeMillis() - startedMs;
                if (elapsed >= deadlineMs) {
                    Soundscape.LOGGER.warn("YouTube load hit {}ms deadline: {}", deadlineMs, exception.getMessage());
                    delegate.loadFailed(exception);
                    return;
                }
                if (!session.rollIfStale(seenGen)) {
                    delegate.loadFailed(exception);
                    return;
                }
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    delegate.loadFailed(exception);
                    return;
                }
                attempt(playerManager, session, identifier, delegate,
                        attempt + 1, maxAttempts, deadlineMs, backoffMs, startedMs);
            }
        });
    }

    private static boolean isRetryable(FriendlyException exception) {
        if (exception.severity == FriendlyException.Severity.COMMON) {
            return false;
        }
        String msg = exception.getMessage();
        if (msg == null) return true;
        String lower = msg.toLowerCase();
        if (lower.contains("private") || lower.contains("age-restricted")
                || lower.contains("members-only") || lower.contains("removed")
                || lower.contains("unavailable in your country")) {
            return false;
        }
        return true;
    }
}
