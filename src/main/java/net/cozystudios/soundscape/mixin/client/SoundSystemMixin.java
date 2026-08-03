package net.cozystudios.soundscape.mixin.client;

import net.cozystudios.soundscape.sound.EnhancedJukeboxSoundInstance;
import net.cozystudios.soundscape.sound.JukeboxSoundManager;
import net.minecraft.client.sound.Channel;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.openal.AL10;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {

    @Shadow
    @Final
    private Map<SoundInstance, Channel.SourceManager> sources;

    //? if >=1.21.11 {
    /*@Shadow
    private float getAdjustedVolume(float volume, SoundCategory category) {
        throw new AssertionError();
    }
    *///?} else {
    @Shadow
    private float getSoundVolume(SoundCategory category) {
        throw new AssertionError();
    }
    //?}

    private static final Map<BlockPos, Long> lastRestartTime = new HashMap<>();
    private static final long RESTART_COOLDOWN_MS = 300;

    @Inject(method = "getAdjustedVolume(Lnet/minecraft/client/sound/SoundInstance;)F",
            at = @At("HEAD"), cancellable = true)
    private void soundscape$unclampedVolumeForJukebox(SoundInstance instance, CallbackInfoReturnable<Float> cir) {
        if (instance instanceof EnhancedJukeboxSoundInstance) {
            //? if >=1.21.11 {
            /*float catMultiplier = getAdjustedVolume(1.0f, instance.getCategory());
            *///?} else {
            float catMultiplier = getSoundVolume(instance.getCategory());
            //?}
            cir.setReturnValue(instance.getVolume() * catMultiplier);
        }
    }

    @Inject(method = "resumeAll", at = @At("TAIL"))
    private void soundscape$rePauseAfterResumeAll(CallbackInfo ci) {
        if (sources == null || sources.isEmpty()) return;

        for (Map.Entry<SoundInstance, Channel.SourceManager> entry : sources.entrySet()) {
            SoundInstance sound = entry.getKey();
            if (sound instanceof EnhancedJukeboxSoundInstance jukeboxSound) {
                BlockPos pos = jukeboxSound.getPos();
                EnhancedJukeboxSoundInstance activeSound = JukeboxSoundManager.getSound(pos);
                if (activeSound != jukeboxSound) continue;

                boolean shouldBePaused = JukeboxSoundManager.isPausedOpenAL(pos) || jukeboxSound.isPausedInSettings();
                if (shouldBePaused) {
                    entry.getValue().run(source -> source.pause());
                }
            }
        }
    }

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void soundscape$checkForLoopingSounds(CallbackInfo ci) {
        long currentTime = System.currentTimeMillis();
        Set<BlockPos> positionsToRestart = new HashSet<>();
        Set<BlockPos> soundsFoundInSources = new HashSet<>();

        if (sources != null && !sources.isEmpty()) {
            for (Map.Entry<SoundInstance, Channel.SourceManager> entry : sources.entrySet()) {
                SoundInstance sound = entry.getKey();
                if (sound instanceof EnhancedJukeboxSoundInstance jukeboxSound) {
                    BlockPos pos = jukeboxSound.getPos();

                    EnhancedJukeboxSoundInstance activeSound = JukeboxSoundManager.getSound(pos);
                    if (activeSound != jukeboxSound) {
                        continue;
                    }

                    soundsFoundInSources.add(pos);
                    jukeboxSound.markInSources();

                    Channel.SourceManager sourceManager = entry.getValue();
                    boolean shouldBePaused = JukeboxSoundManager.isPausedOpenAL(pos) || jukeboxSound.isPausedInSettings();

                    if (shouldBePaused) {
                        sourceManager.run(source -> source.pause());
                        continue;
                    }

                    sourceManager.run(source -> {
                        if (source.isStopped()) {
                            jukeboxSound.markStoppedByOpenAL();
                        } else {
                            source.setPitch(jukeboxSound.getPitch());
                            int ptr = ((SourceAccessor) (Object) source).soundscape$getPointer();
                            AL10.alSourcef(ptr, AL10.AL_MAX_GAIN, 2.0f);
                            source.setVolume(jukeboxSound.getVolume());
                        }
                    });
                }
            }
        }

        for (BlockPos pos : JukeboxSoundManager.getActivePositions()) {
            EnhancedJukeboxSoundInstance sound = JukeboxSoundManager.getSound(pos);
            if (sound == null) continue;

            if (JukeboxSoundManager.isPausedOpenAL(pos) || sound.isPausedInSettings()) {
                sound.resetMissedCallbacks();
                continue;
            }

            Long lastRestart = lastRestartTime.get(pos);
            if (lastRestart != null && (currentTime - lastRestart) < RESTART_COOLDOWN_MS) {
                continue;
            }

            boolean shouldRestart = false;

            if (sound.isMarkedStoppedByOpenAL()) {
                shouldRestart = true;
                sound.clearStoppedFlag();
            }

            if (!soundsFoundInSources.contains(pos)) {
                sound.incrementMissedCallbacks();
                if (sound.shouldConsiderStopped()) {
                    shouldRestart = true;
                }
            }

            if (shouldRestart) {
                int songDuration = sound.getSongDurationTicks();
                int ticksPlayed = sound.getTickCount();
                boolean songActuallyFinished = songDuration > 0 && ticksPlayed >= songDuration - 40;

                if (sound.isLoopModeActive()) {
                    positionsToRestart.add(pos);
                } else if (sound.isShuffleModeActive()) {
                    JukeboxSoundManager.handleSongEnded(pos);
                } else {
                    if (!songActuallyFinished) {
                        positionsToRestart.add(pos);
                    }
                }
            }
        }

        for (BlockPos pos : positionsToRestart) {
            lastRestartTime.put(pos, currentTime);
            JukeboxSoundManager.restartForLoop(pos);
        }
    }
}
