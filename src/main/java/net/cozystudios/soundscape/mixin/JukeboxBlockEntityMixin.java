package net.cozystudios.soundscape.mixin;

import net.cozystudios.soundscape.jukebox.JukeboxSettings;
import net.cozystudios.soundscape.jukebox.JukeboxSettingsProvider;
import net.cozystudios.soundscape.jukebox.PlaybackMode;
import net.cozystudios.soundscape.jukebox.ShuffleQueue;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if <1.21 {
/*import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
*///?}

//? if >=1.21.6 {
/*import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
*///?} elif >=1.20.5 {
import net.minecraft.registry.RegistryWrapper;
//?}

@Mixin(JukeboxBlockEntity.class)
public class JukeboxBlockEntityMixin implements JukeboxSettingsProvider {

    @Unique
    private JukeboxSettings soundscape$settings = new JukeboxSettings();

    @Unique
    private ShuffleQueue soundscape$shuffleQueue = new ShuffleQueue();

    @Unique
    private void soundscape$initQueueCallback() {
        soundscape$shuffleQueue.setDirtyCallback(() -> ((JukeboxBlockEntity)(Object)this).markDirty());
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

    //? if >=1.21.6 {
    /*@Inject(method = "writeData", at = @At("TAIL"))
    private void soundscape$writeSettings(WriteView view, CallbackInfo ci) {
        view.putInt("SoundscapeVolume", soundscape$settings.getVolume());
        view.putInt("SoundscapePitch", soundscape$settings.getPitch());
        view.putInt("SoundscapeRange", soundscape$settings.getRange());
        view.putString("SoundscapePlaybackMode", soundscape$settings.getPlaybackMode().name());
        view.putBoolean("SoundscapeWasPlaying", soundscape$settings.wasPlaying());
        view.putBoolean("SoundscapeShowParticles", soundscape$settings.showParticles());
        view.putBoolean("SoundscapePaused", soundscape$settings.isPaused());
        soundscape$shuffleQueue.writeData(view);
    }

    @Inject(method = "readData", at = @At("TAIL"))
    private void soundscape$readSettings(ReadView view, CallbackInfo ci) {
        soundscape$settings.setVolume(view.getInt("SoundscapeVolume", 100));
        soundscape$settings.setPitch(view.getInt("SoundscapePitch", 100));
        soundscape$settings.setRange(view.getInt("SoundscapeRange", 64));
        String mode = view.getString("SoundscapePlaybackMode", "SINGLE");
        try {
            soundscape$settings.setPlaybackMode(PlaybackMode.valueOf(mode));
        } catch (IllegalArgumentException e) {
            soundscape$settings.setPlaybackMode(PlaybackMode.SINGLE);
        }
        soundscape$settings.setWasPlaying(view.getBoolean("SoundscapeWasPlaying", false));
        soundscape$settings.setShowParticles(view.getBoolean("SoundscapeShowParticles", true));
        soundscape$settings.setPaused(view.getBoolean("SoundscapePaused", false));
        soundscape$shuffleQueue.readData(view);
    }
    *///?} elif >=1.21.5 {
    /*@Inject(method = "writeNbt", at = @At("TAIL"))
    private void soundscape$writeSettings(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        nbt.put("SoundscapeSettings", soundscape$settings.toNbt());
        nbt.put("SoundscapeShuffleQueue", soundscape$shuffleQueue.toNbt(registryLookup));
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void soundscape$readSettings(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        nbt.getCompound("SoundscapeSettings").ifPresent(settingsNbt -> {
            soundscape$settings = JukeboxSettings.fromNbt(settingsNbt);
        });
        nbt.getCompound("SoundscapeShuffleQueue").ifPresent(queueNbt -> {
            soundscape$shuffleQueue = ShuffleQueue.fromNbt(queueNbt, registryLookup);
        });
    }
    *///?} elif >=1.20.5 {
    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void soundscape$writeSettings(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        nbt.put("SoundscapeSettings", soundscape$settings.toNbt());
        nbt.put("SoundscapeShuffleQueue", soundscape$shuffleQueue.toNbt(registryLookup));
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void soundscape$readSettings(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        if (nbt.contains("SoundscapeSettings")) {
            soundscape$settings = JukeboxSettings.fromNbt(nbt.getCompound("SoundscapeSettings"));
        }
        if (nbt.contains("SoundscapeShuffleQueue")) {
            soundscape$shuffleQueue = ShuffleQueue.fromNbt(nbt.getCompound("SoundscapeShuffleQueue"), registryLookup);
        }
    }

    //?} else {
    /*@Inject(method = "writeNbt", at = @At("TAIL"))
    private void soundscape$writeSettings(NbtCompound nbt, CallbackInfo ci) {
        nbt.put("SoundscapeSettings", soundscape$settings.toNbt());
        nbt.put("SoundscapeShuffleQueue", soundscape$shuffleQueue.toNbt());
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void soundscape$readSettings(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("SoundscapeSettings")) {
            soundscape$settings = JukeboxSettings.fromNbt(nbt.getCompound("SoundscapeSettings"));
        }
        if (nbt.contains("SoundscapeShuffleQueue")) {
            soundscape$shuffleQueue = ShuffleQueue.fromNbt(nbt.getCompound("SoundscapeShuffleQueue"));
        }
    }
    *///?}

    //? if >=1.21.4 {
    @Inject(method = "startPlaying", at = @At("HEAD"), cancellable = true, require = 0)
    private void soundscape$cancelStartIfPaused(CallbackInfo ci) {
        if (soundscape$settings.isPaused()) {
            ci.cancel();
        }
    }

    @Inject(method = "startPlaying", at = @At("TAIL"), require = 0)
    private void soundscape$onStartPlaying(CallbackInfo ci) {
        soundscape$settings.setWasPlaying(true);
    }
    //?}

    //? if <1.21 {
    /*@Inject(method = "spawnNoteParticle", at = @At("HEAD"), cancellable = true)
    private void soundscape$blockParticlesIfDisabled(World world, BlockPos pos, CallbackInfo ci) {
        if (!soundscape$settings.showParticles()) {
            ci.cancel();
        }
    }
    *///?}
}
