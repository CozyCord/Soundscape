package net.cozystudios.soundscape.sound;

import net.cozystudios.soundscape.jukebox.JukeboxSettings;
import net.cozystudios.soundscape.jukebox.JukeboxSettingsProvider;
import net.cozystudios.soundscape.jukebox.PlaybackMode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

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

    //? if >=1.21 {
    public EnhancedJukeboxSoundInstance(SoundEvent sound, BlockPos pos, JukeboxBlockEntity jukebox, int durationTicks) {
        super(sound, SoundCategory.RECORDS, Random.create());
        this.pos = pos;
        this.jukebox = jukebox;
        this.soundEvent = sound;
        this.songDurationTicks = durationTicks;
        this.x = pos.getX() + 0.5;
        this.y = pos.getY() + 0.5;
        this.z = pos.getZ() + 0.5;
        this.repeatDelay = 0;
        this.attenuationType = AttenuationType.NONE;
        this.repeat = false;

        if (jukebox instanceof JukeboxSettingsProvider provider) {
            JukeboxSettings settings = provider.soundscape$getSettings();
            if (settings != null) {
                this.cachedSettings = new JukeboxSettings(settings);
            }
        }

        updateVolumeAndPitch();
    }
    //?} else {
    /*public EnhancedJukeboxSoundInstance(SoundEvent sound, BlockPos pos, JukeboxBlockEntity jukebox, int durationTicks) {
        super(sound, SoundCategory.RECORDS, Random.create());
        this.pos = pos;
        this.jukebox = jukebox;
        this.soundEvent = sound;
        this.songDurationTicks = durationTicks;
        this.x = pos.getX() + 0.5;
        this.y = pos.getY() + 0.5;
        this.z = pos.getZ() + 0.5;
        this.repeatDelay = 0;
        this.attenuationType = AttenuationType.NONE;
        this.repeat = false;

        if (jukebox instanceof JukeboxSettingsProvider provider) {
            JukeboxSettings settings = provider.soundscape$getSettings();
            if (settings != null) {
                this.cachedSettings = new JukeboxSettings(settings);
            }
        }

        updateVolumeAndPitch();
    }
    *///?}

    private JukeboxBlockEntity getCurrentJukebox() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null) {
            if (client.world.getBlockEntity(pos) instanceof JukeboxBlockEntity be) {
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
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                double distance = Math.sqrt(client.player.squaredDistanceTo(this.x, this.y, this.z));
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
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world != null) {
            WorldChunk chunk = client.world.getChunkManager().getWorldChunk(pos.getX() >> 4, pos.getZ() >> 4, false);
            if (chunk != null) {
                if (!(client.world.getBlockState(pos).getBlock() instanceof JukeboxBlock)) {
                    done = true;
                    return;
                }
                if (client.world.getBlockEntity(pos) instanceof JukeboxBlockEntity currentJukebox) {
                    //? if >=1.21 {
                    boolean hasDisc = !currentJukebox.getStack(0).isEmpty();
                    //?} else {
                    /*boolean hasDisc = !currentJukebox.getStack().isEmpty();
                    *///?}

                    if (hasDisc) {
                        hasConfirmedDisc = true;
                    } else if (hasConfirmedDisc) {
                        done = true;
                        return;
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
    public boolean isDone() {
        return done;
    }

    @Override
    public boolean shouldAlwaysPlay() {
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
