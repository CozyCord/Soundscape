package net.cozystudios.soundscape.jukebox;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;

public class JukeboxSettings {

    private int volume = 100;
    private int pitch = 100;

    public static final int MAX_RANGE = 128;
    private int range = 64;

    private PlaybackMode playbackMode = PlaybackMode.SINGLE;
    private boolean wasPlaying = false;
    private boolean showParticles = true;
    private boolean paused = false;
    private int playbackProgress = 0;

    public JukeboxSettings() {}

    public JukeboxSettings(JukeboxSettings other) {
        this.volume = other.volume;
        this.pitch = other.pitch;
        this.range = other.range;
        this.playbackMode = other.playbackMode;
        this.wasPlaying = other.wasPlaying;
        this.showParticles = other.showParticles;
        this.paused = other.paused;
        this.playbackProgress = other.playbackProgress;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = MathHelper.clamp(volume, 0, 200);
    }

    public float getVolumeFloat() {
        return volume / 100.0f;
    }

    public int getPitch() {
        return pitch;
    }

    public void setPitch(int pitch) {
        this.pitch = MathHelper.clamp(pitch, 50, 200);
    }

    public float getPitchFloat() {
        return pitch / 100.0f;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = MathHelper.clamp(range, 0, MAX_RANGE);
    }

    public float getEffectiveVolume() {
        return volume / 100.0f;
    }

    public PlaybackMode getPlaybackMode() {
        return playbackMode;
    }

    public void setPlaybackMode(PlaybackMode mode) {
        this.playbackMode = mode;
    }

    public boolean wasPlaying() {
        return wasPlaying;
    }

    public void setWasPlaying(boolean wasPlaying) {
        this.wasPlaying = wasPlaying;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public int getPlaybackProgress() {
        return playbackProgress;
    }

    public void setPlaybackProgress(int progress) {
        this.playbackProgress = MathHelper.clamp(progress, 0, 100);
    }

    public boolean showParticles() {
        return showParticles;
    }

    public void setShowParticles(boolean showParticles) {
        this.showParticles = showParticles;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("Volume", volume);
        nbt.putInt("Pitch", pitch);
        nbt.putInt("Range", range);
        nbt.putString("PlaybackMode", playbackMode.name());
        nbt.putBoolean("WasPlaying", wasPlaying);
        nbt.putBoolean("ShowParticles", showParticles);
        nbt.putBoolean("Paused", paused);
        return nbt;
    }

    public static JukeboxSettings fromNbt(NbtCompound nbt) {
        JukeboxSettings settings = new JukeboxSettings();
        //? if >=1.21.5 {
        /*nbt.getInt("Volume").ifPresent(v -> settings.volume = MathHelper.clamp(v, 0, 200));
        nbt.getInt("Pitch").ifPresent(v -> settings.pitch = MathHelper.clamp(v, 50, 200));
        nbt.getInt("Range").ifPresent(v -> settings.range = MathHelper.clamp(v, 0, MAX_RANGE));
        nbt.getString("PlaybackMode").ifPresent(s -> {
            try {
                settings.playbackMode = PlaybackMode.valueOf(s);
            } catch (IllegalArgumentException e) {
                settings.playbackMode = PlaybackMode.SINGLE;
            }
        });
        nbt.getBoolean("WasPlaying").ifPresent(v -> settings.wasPlaying = v);
        nbt.getBoolean("ShowParticles").ifPresent(v -> settings.showParticles = v);
        nbt.getBoolean("Paused").ifPresent(v -> settings.paused = v);
        *///?} else {
        if (nbt.contains("Volume")) {
            settings.volume = MathHelper.clamp(nbt.getInt("Volume"), 0, 200);
        }
        if (nbt.contains("Pitch")) {
            settings.pitch = MathHelper.clamp(nbt.getInt("Pitch"), 50, 200);
        }
        if (nbt.contains("Range")) {
            settings.range = MathHelper.clamp(nbt.getInt("Range"), 0, 256);
        }
        if (nbt.contains("PlaybackMode")) {
            try {
                settings.playbackMode = PlaybackMode.valueOf(nbt.getString("PlaybackMode"));
            } catch (IllegalArgumentException e) {
                settings.playbackMode = PlaybackMode.SINGLE;
            }
        }
        if (nbt.contains("WasPlaying")) {
            settings.wasPlaying = nbt.getBoolean("WasPlaying");
        }
        if (nbt.contains("ShowParticles")) {
            settings.showParticles = nbt.getBoolean("ShowParticles");
        }
        if (nbt.contains("Paused")) {
            settings.paused = nbt.getBoolean("Paused");
        }
        //?}
        return settings;
    }
}
