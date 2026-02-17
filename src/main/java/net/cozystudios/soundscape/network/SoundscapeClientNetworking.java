package net.cozystudios.soundscape.network;

import net.cozystudios.soundscape.jukebox.JukeboxSettings;
import net.cozystudios.soundscape.jukebox.JukeboxSettingsProvider;
import net.cozystudios.soundscape.jukebox.PlaybackMode;
import net.cozystudios.soundscape.sound.JukeboxSoundManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

//? if >=1.20.5 {
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
//?} else {
/*import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.cozystudios.soundscape.SoundscapeId;
import net.minecraft.network.PacketByteBuf;
*///?}

@Environment(EnvType.CLIENT)
public class SoundscapeClientNetworking {

    public static void registerClient() {
        //? if >=1.20.5 {
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

            context.client().execute(() -> {
                handleSettingsSync(pos, volume, pitch, range, mode, paused, wasPlaying, songId, songDuration);
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
        //?} else {
        /*ClientPlayNetworking.registerGlobalReceiver(SoundscapeId.of("jukebox_settings_sync"), (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            int volume = buf.readInt();
            int pitch = buf.readInt();
            int range = buf.readInt();
            int mode = buf.readInt();
            boolean paused = buf.readBoolean();
            boolean wasPlaying = buf.readBoolean();
            String songId = buf.readString();
            int songDuration = buf.readInt();

            client.execute(() -> {
                handleSettingsSync(pos, volume, pitch, range, mode, paused, wasPlaying, songId, songDuration);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SoundscapeId.of("boombox_state_sync"), (client, handler, buf, responseSender) -> {
            BlockPos bPos = buf.readBlockPos();
            String bUrl = buf.readString();
            int bStateId = buf.readInt();
            float bVolume = buf.readFloat();
            boolean bLoop = buf.readBoolean();
            String bErrorMessage = buf.readString();
            long bTrackPositionMs = buf.readLong();
            int bTrackIndex = buf.readInt();
            int bRange = buf.readInt();

            client.execute(() -> {
                handleBoomboxStateSync(bPos, bUrl, bStateId, bVolume, bLoop, bErrorMessage, bTrackPositionMs, bTrackIndex, bRange);
            });
        });
        *///?}
    }

    public static void requestJukeboxSettings(BlockPos pos) {
        //? if >=1.20.5 {
        ClientPlayNetworking.send(new SoundscapeNetworking.JukeboxSettingsRequestPayload(pos));
        //?} else {
        /*PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        ClientPlayNetworking.send(SoundscapeId.of("jukebox_settings_request"), buf);
        *///?}
    }

    public static void requestShuffleNext(BlockPos pos) {
        //? if >=1.20.5 {
        ClientPlayNetworking.send(new SoundscapeNetworking.JukeboxShuffleNextPayload(pos));
        //?} else {
        /*PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        ClientPlayNetworking.send(SoundscapeId.of("jukebox_shuffle_next"), buf);
        *///?}
    }

    private static void handleSettingsSync(BlockPos pos, int volume, int pitch, int range, int mode, boolean paused, boolean wasPlaying, String songId, int songDuration) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

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

        if (client.world.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
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
                    JukeboxSoundManager.tryResumeJukeboxWithSong(pos, jukebox, songId, songDuration);
                }
                return;
            }
        }

        if (wasPlaying && !paused && !songId.isEmpty()) {
            JukeboxSoundManager.playSoundWithSettings(pos, songId, songDuration, syncedSettings);
        }
    }

    public static void sendBoomboxAction(BlockPos pos, int action, String url, float volume, long positionMs) {
        //? if >=1.20.5 {
        ClientPlayNetworking.send(new SoundscapeNetworking.BoomboxActionPayload(pos, action, url, volume, positionMs));
        //?} else {
        /*PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeInt(action);
        buf.writeString(url);
        buf.writeFloat(volume);
        buf.writeLong(positionMs);
        ClientPlayNetworking.send(SoundscapeId.of("boombox_action"), buf);
        *///?}
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

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof net.cozystudios.soundscape.boombox.client.BoomboxScreen screen
                && screen.getBoomboxPos().equals(pos)) {
            screen.updateState(url, stateId, volume, loop, errorMessage, range);
        }
    }
}
