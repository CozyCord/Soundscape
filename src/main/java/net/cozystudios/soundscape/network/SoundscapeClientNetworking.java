package net.cozystudios.soundscape.network;

import net.cozystudios.soundscape.jukebox.JukeboxSettings;
import net.cozystudios.soundscape.jukebox.JukeboxSettingsProvider;
import net.cozystudios.soundscape.jukebox.PlaybackMode;
import net.cozystudios.soundscape.sound.JukeboxSoundManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;

@Environment(EnvType.CLIENT)
public class SoundscapeClientNetworking {

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(SoundscapeNetworking.JukeboxSettingsSyncPayload.ID,
                (SoundscapeNetworking.JukeboxSettingsSyncPayload payload, ClientPlayNetworking.Context context) -> {
            BlockPos pos = payload.pos();
            int volume = payload.volume();
            int pitch = payload.pitch();
            int range = payload.range();
            int mode = payload.mode();
            boolean paused = payload.paused() != 0;
            boolean wasPlaying = payload.wasPlaying() != 0;
            String songId = payload.songId();
            int songDuration = payload.songDuration();
            int songPositionTicks = payload.songPositionTicks();

            context.client().execute(() -> {
                handleSettingsSync(pos, volume, pitch, range, mode, paused, wasPlaying, songId, songDuration, songPositionTicks);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(SoundscapeNetworking.BoomboxStateSyncPayload.ID,
                (SoundscapeNetworking.BoomboxStateSyncPayload payload, ClientPlayNetworking.Context context) -> {
            BlockPos pos = payload.pos();
            String url = payload.url();
            int stateId = payload.stateId();
            float volume = payload.volume();
            boolean loop = payload.isLoop();
            String errorMessage = payload.errorMessage();
            long trackPositionMs = payload.trackPositionMs();
            int trackIndex = payload.trackIndex();
            int range = payload.range();

            context.client().execute(() -> {
                handleBoomboxStateSync(pos, url, stateId, volume, loop, errorMessage, trackPositionMs, trackIndex, range);
            });
        });
    }

    public static void requestJukeboxSettings(BlockPos pos) {
        ClientPlayNetworking.send(new SoundscapeNetworking.JukeboxSettingsRequestPayload(pos));
    }

    public static void requestShuffleNext(BlockPos pos) {
        ClientPlayNetworking.send(new SoundscapeNetworking.JukeboxShuffleNextPayload(pos));
    }

    public static void requestBoomboxState(BlockPos pos) {
        ClientPlayNetworking.send(new SoundscapeNetworking.BoomboxStateRequestPayload(pos));
    }

    private static void handleSettingsSync(BlockPos pos, int volume, int pitch, int range, int mode, boolean paused, boolean wasPlaying, String songId, int songDuration, int songPositionTicks) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;

        JukeboxSettings syncedSettings = new JukeboxSettings();
        syncedSettings.setVolume(volume);
        syncedSettings.setPitch(pitch);
        syncedSettings.setRange(range);
        PlaybackMode[] modes = PlaybackMode.values();
        if (mode >= 0 && mode < modes.length) {
            syncedSettings.setPlaybackMode(modes[mode]);
        }
        syncedSettings.setPaused(paused);
        syncedSettings.setWasPlaying(wasPlaying);

        if (client.level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
            if (jukebox instanceof JukeboxSettingsProvider provider) {
                JukeboxSettings settings = provider.soundscape$getSettings();
                settings.setVolume(volume);
                settings.setPitch(pitch);
                settings.setRange(range);
                if (mode >= 0 && mode < modes.length) {
                    settings.setPlaybackMode(modes[mode]);
                }
                settings.setPaused(paused);
                settings.setWasPlaying(wasPlaying);

                if (wasPlaying && !paused && !songId.isEmpty()) {
                    JukeboxSoundManager.tryResumeJukeboxWithSong(pos, jukebox, songId, songDuration, songPositionTicks);
                }
                return;
            }
        }

        if (wasPlaying && !paused && !songId.isEmpty()) {
            JukeboxSoundManager.playSoundWithSettings(pos, songId, songDuration, syncedSettings, songPositionTicks);
        }
    }

    public static void sendBoomboxAction(BlockPos pos, int action, String url, float volume, long positionMs) {
        ClientPlayNetworking.send(new SoundscapeNetworking.BoomboxActionPayload(pos, action, url, volume, positionMs));
    }

    private static void handleBoomboxStateSync(BlockPos pos, String url, int stateId, float volume,
                                               boolean loop, String errorMessage, long trackPositionMs,
                                               int trackIndex, int range) {
        net.cozystudios.soundscape.boombox.client.BoomboxAudioManager mgr =
                net.cozystudios.soundscape.boombox.client.BoomboxAudioManager.getInstance();

        mgr.setRange(pos, range);
        mgr.cacheState(pos, url, stateId, volume, loop, errorMessage, range);

        switch (stateId) {
            case 1 -> {
                if (mgr.isPlaying(pos)) {
                    mgr.resume(pos);
                } else {
                    mgr.playFromPosition(pos, url, volume, loop, trackPositionMs, trackIndex);
                }
            }
            case 0, 2, 3 -> mgr.stop(pos);
            case 4 -> mgr.pause(pos);
            case 10 -> mgr.nextTrack(pos);
            case 11 -> mgr.prevTrack(pos);
            case 12, 13 -> mgr.seek(pos, trackPositionMs);
        }

        mgr.setVolume(pos, volume);
        mgr.setLoop(pos, loop);

        Minecraft client = Minecraft.getInstance();
        if (client.screen instanceof net.cozystudios.soundscape.boombox.client.BoomboxScreen screen
                && screen.getBoomboxPos().equals(pos)) {
            screen.updateState(url, stateId, volume, loop, errorMessage, range);
        }
    }
}
