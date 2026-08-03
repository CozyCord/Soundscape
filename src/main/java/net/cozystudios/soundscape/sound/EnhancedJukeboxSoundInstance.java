package net.cozystudios.soundscape.sound;

import net.cozystudios.soundscape.jukebox.JukeboxSettings;
import net.cozystudios.soundscape.jukebox.JukeboxSettingsProvider;
import net.cozystudios.soundscape.jukebox.PlaybackMode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

@Environment(EnvType.CLIENT)
public class EnhancedJukeboxSoundInstance extends AbstractSoundInstance implements TickableSoundInstance {

    private static final float FADE_DISTANCE = 6.0f;
    private static final int CALLBACK_MISS_THRESHOLD = 3;

    private final BlockPos pos;
    private final JukeboxBlockEntity jukebox;
    private final SoundEvent soundEvent;
    private final int songDurationTicks;
    private boolean done = false;
    private int tickCount = 0;
    private float pitchAdjustedTicks = 0;
    private boolean wasPaused = false;
    private boolean hasBeenInSources = false;
    private int ticksSinceCallback = 0;
    private volatile boolean markedStoppedByOpenAL = false;

    private boolean hasConfirmedDisc = false;

    private JukeboxSettings cachedSettings;

    private volatile int pendingSeekTicks = 0;
    private volatile int seekAttemptsRemaining = 0;
    private static final int MAX_SEEK_ATTEMPTS = 100;

    public EnhancedJukeboxSoundInstance(SoundEvent sound, BlockPos pos, JukeboxBlockEntity jukebox, int durationTicks) {
        this(sound, pos, jukebox, durationTicks, 0);
    }

    public EnhancedJukeboxSoundInstance(SoundEvent sound, BlockPos pos, JukeboxBlockEntity jukebox, int durationTicks, int startTicks) {
        super(sound, SoundSource.RECORDS, RandomSource.create());
        this.pos = pos;
        this.jukebox = jukebox;
        this.soundEvent = sound;
        this.songDurationTicks = durationTicks;
        this.x = pos.getX() + 0.5;
        this.y = pos.getY() + 0.5;
        this.z = pos.getZ() + 0.5;
        this.delay = 0;
        this.attenuation = Attenuation.NONE;
        this.looping = false;

        this.tickCount = Math.max(0, startTicks);
        this.pitchAdjustedTicks = this.tickCount;
        if (this.tickCount > 0) {
            this.pendingSeekTicks = this.tickCount;
            this.seekAttemptsRemaining = MAX_SEEK_ATTEMPTS;
        }

        if (jukebox instanceof JukeboxSettingsProvider provider) {
            JukeboxSettings settings = provider.soundscape$getSettings();
            if (settings != null) {
                this.cachedSettings = new JukeboxSettings(settings);
            }
        }

        updateVolumeAndPitch();
    }

    public int getPendingSeekTicks() {
        return pendingSeekTicks;
    }

    public boolean shouldRetrySeek() {
        if (pendingSeekTicks <= 0) return false;
        if (seekAttemptsRemaining <= 0) {
            pendingSeekTicks = 0;
            return false;
        }
        seekAttemptsRemaining--;
        return true;
    }

    public void confirmSeek() {
        pendingSeekTicks = 0;
        seekAttemptsRemaining = 0;
    }

    private JukeboxBlockEntity getCurrentJukebox() {
        Minecraft client = Minecraft.getInstance();
        if (client.level != null) {
            if (client.level.getBlockEntity(pos) instanceof JukeboxBlockEntity be) {
                return be;
            }
        }
        return jukebox;
    }

    private JukeboxSettings getSettings() {
        JukeboxBlockEntity currentJukebox = getCurrentJukebox();
        if (currentJukebox != null && !currentJukebox.isRemoved() && currentJukebox instanceof JukeboxSettingsProvider provider) {
            JukeboxSettings settings = provider.soundscape$getSettings();
            if (settings != null) {
                cachedSettings = new JukeboxSettings(settings);
                return settings;
            }
        }
        if (cachedSettings == null) {
            cachedSettings = new JukeboxSettings();
        }
        return cachedSettings;
    }

    private void updateVolumeAndPitch() {
        JukeboxSettings settings = getSettings();
        if (settings != null) {
            Minecraft client = Minecraft.getInstance();
            if (client.player != null) {
                double distance = Math.sqrt(client.player.distanceToSqr(this.x, this.y, this.z));
                int range = settings.getRange();
                float baseVolume = settings.getEffectiveVolume();

                if (distance <= range) {
                    this.volume = baseVolume;
                } else if (distance < range + FADE_DISTANCE) {
                    float fadeProgress = (float) ((distance - range) / FADE_DISTANCE);
                    float smoothFade = 1.0f - smoothstep(fadeProgress);
                    this.volume = baseVolume * smoothFade;
                } else {
                    this.volume = 0.0f;
                }
            } else {
                this.volume = settings.getEffectiveVolume();
            }
            this.pitch = settings.getPitchFloat();
        } else {
            this.volume = 1.0f;
            this.pitch = 1.0f;
        }
    }

    private float smoothstep(float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        return t * t * (3.0f - 2.0f * t);
    }

    @Override
    public void tick() {
        Minecraft client = Minecraft.getInstance();

        if (client.level != null) {
            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;
            if (client.level.hasChunk(chunkX, chunkZ)) {
                LevelChunk chunk = client.level.getChunk(chunkX, chunkZ);
                if (chunk != null) {
                    if (!(client.level.getBlockState(pos).getBlock() instanceof JukeboxBlock)) {
                        done = true;
                        return;
                    }
                    if (client.level.getBlockEntity(pos) instanceof JukeboxBlockEntity currentJukebox) {
                        boolean hasDisc = !currentJukebox.getTheItem().isEmpty();

                        if (hasDisc) {
                            hasConfirmedDisc = true;
                        } else if (hasConfirmedDisc) {
                            done = true;
                            return;
                        }
                    }
                }
            }
        }

        JukeboxSettings settings = getSettings();

        boolean isPaused = settings.isPaused();

        if (isPaused != wasPaused) {
            if (isPaused) {
                JukeboxSoundManager.pauseSound(pos);
            } else {
                JukeboxSoundManager.resumeSound(pos);
            }
            wasPaused = isPaused;
        }

        if (!isPaused) {
            updateVolumeAndPitch();
            tickCount++;
            pitchAdjustedTicks += settings.getPitchFloat();
        }
    }

    @Override
    public boolean isStopped() {
        return done;
    }

    @Override
    public boolean canPlaySound() {
        return true;
    }

    public void stop() {
        done = true;
    }

    public BlockPos getPos() {
        return pos;
    }

    public JukeboxBlockEntity getJukebox() {
        return getCurrentJukebox();
    }

    public SoundEvent getSoundEvent() {
        return soundEvent;
    }

    public int getSongDurationTicks() {
        return songDurationTicks;
    }

    public int getTickCount() {
        return tickCount;
    }

    public float getPitchAdjustedTicks() {
        return pitchAdjustedTicks;
    }

    public boolean hasBeenInSources() {
        return hasBeenInSources;
    }

    public void markInSources() {
        this.hasBeenInSources = true;
        this.ticksSinceCallback = 0;
    }

    public void incrementMissedCallbacks() {
        this.ticksSinceCallback++;
    }

    public boolean shouldConsiderStopped() {
        return hasBeenInSources && ticksSinceCallback >= CALLBACK_MISS_THRESHOLD;
    }

    public void resetMissedCallbacks() {
        this.ticksSinceCallback = 0;
    }

    public void markStoppedByOpenAL() {
        this.markedStoppedByOpenAL = true;
    }

    public boolean isMarkedStoppedByOpenAL() {
        return markedStoppedByOpenAL;
    }

    public void clearStoppedFlag() {
        this.markedStoppedByOpenAL = false;
    }

    public boolean isLoopModeActive() {
        JukeboxSettings settings = getSettings();
        return settings != null && settings.getPlaybackMode() == PlaybackMode.LOOP;
    }

    public boolean isShuffleModeActive() {
        JukeboxSettings settings = getSettings();
        return settings != null && settings.getPlaybackMode() == PlaybackMode.SHUFFLE;
    }

    public boolean isPausedInSettings() {
        JukeboxSettings settings = getSettings();
        return settings != null && settings.isPaused();
    }

    public void injectCachedSettings(JukeboxSettings settings) {
        if (settings != null) {
            this.cachedSettings = new JukeboxSettings(settings);
            updateVolumeAndPitch();
        }
    }

    public JukeboxSettings getCachedSettings() {
        return cachedSettings;
    }
}
