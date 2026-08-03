package net.cozystudios.soundscape.mixin;

import net.cozystudios.soundscape.jukebox.JukeboxSettings;
import net.cozystudios.soundscape.jukebox.JukeboxSettingsProvider;
import net.cozystudios.soundscape.jukebox.PlaybackMode;
import net.cozystudios.soundscape.jukebox.ShuffleQueue;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JukeboxBlockEntity.class)
public class JukeboxBlockEntityMixin implements JukeboxSettingsProvider {

    @Unique
    private JukeboxSettings soundscape$settings = new JukeboxSettings();

    @Unique
    private ShuffleQueue soundscape$shuffleQueue = new ShuffleQueue();

    @Unique
    private void soundscape$initQueueCallback() {
        soundscape$shuffleQueue.setDirtyCallback(() -> ((JukeboxBlockEntity)(Object)this).setChanged());
    }

    @Override
    public JukeboxSettings soundscape$getSettings() {
        return this.soundscape$settings;
    }

    @Override
    public void soundscape$setSettings(JukeboxSettings settings) {
        this.soundscape$settings = new JukeboxSettings(settings);
    }

    @Override
    public ShuffleQueue soundscape$getShuffleQueue() {
        soundscape$initQueueCallback();
        return this.soundscape$shuffleQueue;
    }

    @Override
    public void soundscape$setShuffleQueue(ShuffleQueue queue) {
        this.soundscape$shuffleQueue = queue;
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void soundscape$writeSettings(ValueOutput view, CallbackInfo ci) {
        view.putInt("SoundscapeVolume", soundscape$settings.getVolume());
        view.putInt("SoundscapePitch", soundscape$settings.getPitch());
        view.putInt("SoundscapeRange", soundscape$settings.getRange());
        view.putString("SoundscapePlaybackMode", soundscape$settings.getPlaybackMode().name());
        view.putBoolean("SoundscapeWasPlaying", soundscape$settings.wasPlaying());
        view.putBoolean("SoundscapeShowParticles", soundscape$settings.showParticles());
        view.putBoolean("SoundscapePaused", soundscape$settings.isPaused());
        soundscape$shuffleQueue.writeData(view);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void soundscape$readSettings(ValueInput view, CallbackInfo ci) {
        soundscape$settings.setVolume(view.getIntOr("SoundscapeVolume", 100));
        soundscape$settings.setPitch(view.getIntOr("SoundscapePitch", 100));
        soundscape$settings.setRange(view.getIntOr("SoundscapeRange", 64));
        String mode = view.getStringOr("SoundscapePlaybackMode", "SINGLE");
        try {
            soundscape$settings.setPlaybackMode(PlaybackMode.valueOf(mode));
        } catch (IllegalArgumentException e) {
            soundscape$settings.setPlaybackMode(PlaybackMode.SINGLE);
        }
        soundscape$settings.setWasPlaying(view.getBooleanOr("SoundscapeWasPlaying", false));
        soundscape$settings.setShowParticles(view.getBooleanOr("SoundscapeShowParticles", true));
        soundscape$settings.setPaused(view.getBooleanOr("SoundscapePaused", false));
        soundscape$shuffleQueue.readData(view);
    }
}
