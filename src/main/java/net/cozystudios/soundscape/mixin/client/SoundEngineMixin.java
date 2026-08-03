package net.cozystudios.soundscape.mixin.client;

import com.mojang.blaze3d.audio.Channel;
import net.cozystudios.soundscape.sound.EnhancedJukeboxSoundInstance;
import net.cozystudios.soundscape.sound.JukeboxSoundManager;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
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

@Mixin(SoundEngine.class)
public class SoundEngineMixin {

    @Shadow
    @Final
    private Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel;

    @Shadow
    private float calculateVolume(float volume, SoundSource category) {
        throw new AssertionError();
    }

    private static final Map<BlockPos, Long> soundscape$lastRestartTime = new HashMap<>();
    private static final long soundscape$RESTART_COOLDOWN_MS = 300;

    @Inject(method = "calculateVolume(Lnet/minecraft/client/resources/sounds/SoundInstance;)F",
            at = @At("HEAD"), cancellable = true)
    private void soundscape$unclampedVolumeForJukebox(SoundInstance instance, CallbackInfoReturnable<Float> cir) {
        if (instance instanceof EnhancedJukeboxSoundInstance) {
            float categoryMultiplier = calculateVolume(1.0f, instance.getSource());
            cir.setReturnValue(instance.getVolume() * categoryMultiplier);
        }
    }

    @Inject(method = "resume", at = @At("TAIL"))
    private void soundscape$rePauseAfterResumeAll(CallbackInfo ci) {
        if (instanceToChannel == null || instanceToChannel.isEmpty()) return;

        for (Map.Entry<SoundInstance, ChannelAccess.ChannelHandle> entry : instanceToChannel.entrySet()) {
            SoundInstance sound = entry.getKey();
            if (sound instanceof EnhancedJukeboxSoundInstance jukeboxSound) {
                BlockPos pos = jukeboxSound.getPos();
                EnhancedJukeboxSoundInstance activeSound = JukeboxSoundManager.getSound(pos);
                if (activeSound != jukeboxSound) continue;

                boolean shouldBePaused = JukeboxSoundManager.isPausedOpenAL(pos) || jukeboxSound.isPausedInSettings();
                if (shouldBePaused) {
                    entry.getValue().execute(Channel::pause);
                }
            }
        }
    }

    @Inject(method = "tick(Z)V", at = @At("HEAD"))
    private void soundscape$checkForLoopingSounds(boolean paused, CallbackInfo ci) {
        long currentTime = System.currentTimeMillis();
        Set<BlockPos> positionsToRestart = new HashSet<>();
        Set<BlockPos> soundsFoundInSources = new HashSet<>();

        if (instanceToChannel != null && !instanceToChannel.isEmpty()) {
            for (Map.Entry<SoundInstance, ChannelAccess.ChannelHandle> entry : instanceToChannel.entrySet()) {
                SoundInstance sound = entry.getKey();
                if (sound instanceof EnhancedJukeboxSoundInstance jukeboxSound) {
                    BlockPos pos = jukeboxSound.getPos();

                    EnhancedJukeboxSoundInstance activeSound = JukeboxSoundManager.getSound(pos);
                    if (activeSound != jukeboxSound) {
                        continue;
                    }

                    soundsFoundInSources.add(pos);
                    jukeboxSound.markInSources();

                    ChannelAccess.ChannelHandle handle = entry.getValue();
                    boolean shouldBePaused = JukeboxSoundManager.isPausedOpenAL(pos) || jukeboxSound.isPausedInSettings();

                    if (shouldBePaused) {
                        handle.execute(Channel::pause);
                        continue;
                    }

                    if (jukeboxSound.shouldRetrySeek()) {
                        final int seekTicks = jukeboxSound.getPendingSeekTicks();
                        final float seekSeconds = seekTicks / 20.0f;
                        handle.execute(source -> {
                            if (jukeboxSound.getPendingSeekTicks() <= 0) return;
                            int ptr = ((ChannelAccessor) (Object) source).soundscape$getSource();
                            int state = AL10.alGetSourcei(ptr, AL10.AL_SOURCE_STATE);
                            if (state == AL10.AL_PLAYING || state == AL10.AL_PAUSED || state == AL10.AL_INITIAL) {
                                AL10.alSourcef(ptr, AL11.AL_SEC_OFFSET, seekSeconds);
                                float actualPos = AL10.alGetSourcef(ptr, AL11.AL_SEC_OFFSET);
                                if (actualPos >= seekSeconds - 0.5f) {
                                    jukeboxSound.confirmSeek();
                                }
                            }
                        });
                    }

                    handle.execute(source -> {
                        if (source.stopped()) {
                            jukeboxSound.markStoppedByOpenAL();
                        } else {
                            source.setPitch(jukeboxSound.getPitch());
                            int ptr = ((ChannelAccessor) (Object) source).soundscape$getSource();
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

            Long lastRestart = soundscape$lastRestartTime.get(pos);
            if (lastRestart != null && (currentTime - lastRestart) < soundscape$RESTART_COOLDOWN_MS) {
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
            soundscape$lastRestartTime.put(pos, currentTime);
            JukeboxSoundManager.restartForLoop(pos);
        }
    }
}
