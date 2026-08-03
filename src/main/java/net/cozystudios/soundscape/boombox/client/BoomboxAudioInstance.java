package net.cozystudios.soundscape.boombox.client;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.openal.AL10;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.List;

@Environment(EnvType.CLIENT)
public class BoomboxAudioInstance {

    private static final int BUFFER_COUNT = 4;
    private static final int SAMPLE_RATE = 48000;
    private static final int FRAME_SIZE = 3840;
    private static final int FRAMES_PER_BUFFER = 12;
    private static final float FADE_DISTANCE = 8.0f;

    private final AudioPlayer audioPlayer;
    private final MutableAudioFrame frame;
    private final ByteBuffer frameBuffer;
    private final List<AudioTrack> playlist;

    private int alSource = -1;
    private int[] alBuffers;
    private int pendingBufferIndex = 0;
    private boolean started = false;
    private float volume;
    private boolean loop;
    private boolean destroyed = false;
    private boolean paused = false;
    private volatile int trackIndex;
    private volatile AudioTrack nextTrackToPlay = null;
    private volatile boolean needsRestart = false;
    private volatile boolean needsClientRetry = false;

    public BoomboxAudioInstance(AudioPlayer audioPlayer, float volume, boolean loop, List<AudioTrack> playlist) {
        this.audioPlayer = audioPlayer;
        this.volume = volume;
        this.loop = loop;
        this.playlist = playlist;
        this.trackIndex = 0;

        this.frame = new MutableAudioFrame();
        this.frameBuffer = ByteBuffer.allocate(FRAME_SIZE);
        this.frame.setBuffer(frameBuffer);

        audioPlayer.addListener(new AudioEventAdapter() {
            @Override
            public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                if (!endReason.mayStartNext || destroyed) return;

                if (playlist.size() > 1) {
                    int nextIdx = trackIndex + 1;
                    if (nextIdx >= playlist.size()) {
                        if (loop) {
                            nextIdx = 0;
                        } else {
                            return;
                        }
                    }
                    trackIndex = nextIdx;
                    nextTrackToPlay = playlist.get(nextIdx).makeClone();
                    needsRestart = true;
                } else if (loop) {
                    nextTrackToPlay = track.makeClone();
                    needsRestart = true;
                }
            }

            @Override
            public void onTrackException(AudioPlayer player, AudioTrack track, com.sedmelluq.discord.lavaplayer.tools.FriendlyException exception) {
                net.cozystudios.soundscape.Soundscape.LOGGER.warn(
                        "Boombox track playback error, will retry with different client: {}", exception.getMessage());
                needsClientRetry = true;
            }
        });
    }

    public void initialize() {
        try {
            org.lwjgl.openal.AL.getCapabilities();
        } catch (IllegalStateException noCaps) {
            destroyed = true;
            return;
        }

        alSource = AL10.alGenSources();
        AL10.alSourcei(alSource, AL10.AL_SOURCE_RELATIVE, AL10.AL_FALSE);
        AL10.alSource3f(alSource, AL10.AL_POSITION, 0f, 0f, 0f);
        AL10.alSourcef(alSource, AL10.AL_GAIN, volume);
        AL10.alSourcef(alSource, AL10.AL_ROLLOFF_FACTOR, 0f);

        alBuffers = new int[BUFFER_COUNT];
        AL10.alGenBuffers(alBuffers);
        pendingBufferIndex = 0;
        started = false;
    }

    public void setTrackIndex(int index) {
        this.trackIndex = index;
    }

    public int getTrackIndex() {
        return trackIndex;
    }

    public void nextTrack() {
        if (playlist.size() <= 1) return;
        trackIndex = (trackIndex + 1) % playlist.size();
        nextTrackToPlay = playlist.get(trackIndex).makeClone();
        needsRestart = true;
    }

    public void prevTrack() {
        if (playlist.size() <= 1) return;
        trackIndex = (trackIndex - 1 + playlist.size()) % playlist.size();
        nextTrackToPlay = playlist.get(trackIndex).makeClone();
        needsRestart = true;
    }

    public String getTrackInfo() {
        if (playlist.isEmpty()) return "";
        AudioTrack current = playlist.get(trackIndex);
        String title = current.getInfo().title;
        if (playlist.size() == 1) {
            return title;
        }
        return "Track " + (trackIndex + 1) + "/" + playlist.size() + ": " + title;
    }

    public long getTrackPositionMs() {
        AudioTrack track = audioPlayer.getPlayingTrack();
        if (track != null) {
            return track.getPosition();
        }
        return 0;
    }

    public void pause() {
        if (destroyed || alSource == -1) return;
        audioPlayer.setPaused(true);
        AL10.alSourcePause(alSource);
        paused = true;
    }

    public void resume() {
        if (destroyed || alSource == -1) return;
        audioPlayer.setPaused(false);
        paused = false;
        int sourceState = AL10.alGetSourcei(alSource, AL10.AL_SOURCE_STATE);
        if (sourceState == AL10.AL_PAUSED) {
            AL10.alSourcePlay(alSource);
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public void seekTo(long positionMs) {
        if (destroyed) return;
        AudioTrack track = audioPlayer.getPlayingTrack();
        if (track != null) {
            track.setPosition(Math.max(0, positionMs));

            if (paused && alSource != -1) {
                audioPlayer.setPaused(false);
                paused = false;
                int sourceState = AL10.alGetSourcei(alSource, AL10.AL_SOURCE_STATE);
                if (sourceState != AL10.AL_PLAYING) {
                    AL10.alSourcePlay(alSource);
                }
            }
        }
    }

    public void updatePositionAndGain(Vec3d playerPos, BlockPos boomboxPos, double distance, float maxRange) {
        if (alSource == -1) return;

        float x = (float) (boomboxPos.getX() + 0.5);
        float y = (float) (boomboxPos.getY() + 0.5);
        float z = (float) (boomboxPos.getZ() + 0.5);
        AL10.alSource3f(alSource, AL10.AL_POSITION, x, y, z);

        float gain;
        if (distance <= maxRange) {
            gain = volume;
        } else if (distance <= maxRange + FADE_DISTANCE) {
            float fadeProgress = (float) (distance - maxRange) / FADE_DISTANCE;
            gain = volume * (1.0f - fadeProgress);
        } else {
            gain = 0f;
        }
        AL10.alSourcef(alSource, AL10.AL_GAIN, gain);
    }

    public void tick() {
        if (destroyed || alSource == -1 || paused) return;

        if (needsRestart) {
            needsRestart = false;
            AudioTrack trackToPlay = nextTrackToPlay;
            nextTrackToPlay = null;
            if (trackToPlay != null) {
                AL10.alSourceStop(alSource);
                int queued = AL10.alGetSourcei(alSource, AL10.AL_BUFFERS_QUEUED);
                for (int i = 0; i < queued; i++) {
                    AL10.alSourceUnqueueBuffers(alSource);
                }
                audioPlayer.playTrack(trackToPlay);
                pendingBufferIndex = 0;
                started = false;
            }
        }

        while (pendingBufferIndex < alBuffers.length) {
            if (!fillBuffer(alBuffers[pendingBufferIndex])) break;
            AL10.alSourceQueueBuffers(alSource, alBuffers[pendingBufferIndex]);
            pendingBufferIndex++;
        }

        if (!started && AL10.alGetSourcei(alSource, AL10.AL_BUFFERS_QUEUED) > 0) {
            AL10.alSourcePlay(alSource);
            started = true;
        }

        int processed = AL10.alGetSourcei(alSource, AL10.AL_BUFFERS_PROCESSED);
        while (processed-- > 0) {
            int buf = AL10.alSourceUnqueueBuffers(alSource);
            if (fillBuffer(buf)) {
                AL10.alSourceQueueBuffers(alSource, buf);
            }
        }

        int sourceState = AL10.alGetSourcei(alSource, AL10.AL_SOURCE_STATE);
        if (sourceState == AL10.AL_STOPPED && started) {
            int queued = AL10.alGetSourcei(alSource, AL10.AL_BUFFERS_QUEUED);
            if (queued > 0) {
                AL10.alSourcePlay(alSource);
            }
        }
    }

    private boolean fillBuffer(int alBuffer) {
        byte[] accumulated = new byte[FRAME_SIZE * FRAMES_PER_BUFFER];
        int totalBytes = 0;

        for (int i = 0; i < FRAMES_PER_BUFFER; i++) {
            if (!audioPlayer.provide(frame)) break;
            int len = frame.getDataLength();
            frameBuffer.position(0);
            frameBuffer.get(accumulated, totalBytes, len);
            totalBytes += len;
        }

        if (totalBytes == 0) return false;

        byte[] monoData = downmixToMono(accumulated, totalBytes);

        ByteBuffer nativeBuf = MemoryUtil.memAlloc(monoData.length);
        try {
            nativeBuf.put(monoData).flip();
            AL10.alBufferData(alBuffer, AL10.AL_FORMAT_MONO16, nativeBuf, SAMPLE_RATE);
        } finally {
            MemoryUtil.memFree(nativeBuf);
        }
        return true;
    }

    private byte[] downmixToMono(byte[] stereo, int length) {
        byte[] mono = new byte[length / 2];
        for (int i = 0; i < length - 3; i += 4) {
            short left = (short) ((stereo[i] << 8) | (stereo[i + 1] & 0xFF));
            short right = (short) ((stereo[i + 2] << 8) | (stereo[i + 3] & 0xFF));
            short mixed = (short) ((left + right) / 2);
            int j = i / 2;
            mono[j] = (byte) (mixed & 0xFF);
            mono[j + 1] = (byte) ((mixed >> 8) & 0xFF);
        }
        return mono;
    }

    public void setVolume(float volume) {
        this.volume = volume;
        if (alSource != -1) {
            AL10.alSourcef(alSource, AL10.AL_GAIN, volume);
        }
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public void destroy() {
        if (destroyed) return;
        destroyed = true;

        audioPlayer.stopTrack();
        audioPlayer.destroy();

        if (alSource != -1) {
            AL10.alSourceStop(alSource);
            int queued = AL10.alGetSourcei(alSource, AL10.AL_BUFFERS_QUEUED);
            for (int i = 0; i < queued; i++) {
                AL10.alSourceUnqueueBuffers(alSource);
            }
            AL10.alDeleteSources(alSource);
            alSource = -1;
        }

        if (alBuffers != null) {
            AL10.alDeleteBuffers(alBuffers);
            alBuffers = null;
        }
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public boolean needsClientRetry() {
        return needsClientRetry;
    }
}
