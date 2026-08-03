package net.cozystudios.soundscape.sound;

import com.mojang.blaze3d.audio.Channel;
import net.cozystudios.soundscape.jukebox.JukeboxSettings;
import net.cozystudios.soundscape.jukebox.JukeboxSettingsProvider;
import net.cozystudios.soundscape.mixin.client.SoundEngineAccessor;
import net.cozystudios.soundscape.mixin.client.SoundManagerAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class JukeboxSoundManager {

    private static final Map<BlockPos, EnhancedJukeboxSoundInstance> activeSounds = new HashMap<>();
    private static final Set<BlockPos> pausedSounds = new HashSet<>();
    private static final Map<BlockPos, Integer> pendingResume = new HashMap<>();
    private static final int MAX_RESUME_WAIT_TICKS = 100;

    private static final Map<BlockPos, int[]> pendingPlayback = new HashMap<>();
    private static final int MAX_PLAYBACK_WAIT_TICKS = 60;

    public static void playSound(BlockPos pos, SoundEvent sound, JukeboxBlockEntity jukebox, int durationTicks) {
        stopSound(pos);

        EnhancedJukeboxSoundInstance soundInstance = new EnhancedJukeboxSoundInstance(sound, pos, jukebox, durationTicks);
        activeSounds.put(pos, soundInstance);

        if (jukebox instanceof JukeboxSettingsProvider provider) {
            JukeboxSettings settings = provider.soundscape$getSettings();
            if (settings != null) {
                settings.setWasPlaying(true);
                sendWasPlayingToServer(pos, true);
            }
        }

        Minecraft.getInstance().getSoundManager().play(soundInstance);
    }

    public static void playSound(BlockPos pos, SoundEvent sound, JukeboxBlockEntity jukebox) {
        playSound(pos, sound, jukebox, 0);
    }

    private static void sendWasPlayingToServer(BlockPos pos, boolean wasPlaying) {
        ClientPlayNetworking.send(new net.cozystudios.soundscape.network.SoundscapeNetworking.JukeboxWasPlayingPayload(pos, wasPlaying ? 1 : 0));
    }

    public static void stopSound(BlockPos pos) {
        EnhancedJukeboxSoundInstance sound = activeSounds.remove(pos);
        if (sound != null) {
            JukeboxBlockEntity jukebox = sound.getJukebox();
            if (jukebox instanceof JukeboxSettingsProvider provider) {
                JukeboxSettings settings = provider.soundscape$getSettings();
                if (settings != null) {
                    settings.setWasPlaying(false);
                    sendWasPlayingToServer(pos, false);
                }
            }
            sound.stop();
            Minecraft.getInstance().getSoundManager().stop(sound);
        }
        pausedSounds.remove(pos);
    }

    public static boolean handleSongEnded(BlockPos pos) {
        EnhancedJukeboxSoundInstance sound = activeSounds.get(pos);
        if (sound == null) {
            return false;
        }

        int ticksPlayed = sound.getTickCount();
        float pitchAdjustedTicks = sound.getPitchAdjustedTicks();
        int minTicksBeforeStop = 40;

        if (ticksPlayed < minTicksBeforeStop) {
            return true;
        }

        int songDuration = sound.getSongDurationTicks();
        if (songDuration > 0 && pitchAdjustedTicks < songDuration - 40) {
            return true;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.level != null) {
            if (!(client.level.getBlockState(pos).getBlock() instanceof net.minecraft.world.level.block.JukeboxBlock)) {
                return false;
            }
        }

        if (sound.isLoopModeActive()) {
            SoundEvent soundEvent = sound.getSoundEvent();
            int durationTicks = sound.getSongDurationTicks();
            JukeboxBlockEntity jukebox = sound.getJukebox();

            sound.stop();
            Minecraft.getInstance().getSoundManager().stop(sound);
            activeSounds.remove(pos);
            pausedSounds.remove(pos);

            if (jukebox != null && !jukebox.isRemoved()) {
                EnhancedJukeboxSoundInstance newSound = new EnhancedJukeboxSoundInstance(soundEvent, pos, jukebox, durationTicks);
                activeSounds.put(pos, newSound);
                Minecraft.getInstance().getSoundManager().play(newSound);

                if (jukebox instanceof JukeboxSettingsProvider provider) {
                    JukeboxSettings settings = provider.soundscape$getSettings();
                    if (settings != null) {
                        settings.setWasPlaying(true);
                    }
                }
                sendWasPlayingToServer(pos, true);
            } else {
                playSoundWithCachedSettings(pos, soundEvent, durationTicks, sound);
                sendWasPlayingToServer(pos, true);
            }

            return true;
        }

        if (sound.isShuffleModeActive()) {
            sound.stop();
            Minecraft.getInstance().getSoundManager().stop(sound);
            activeSounds.remove(pos);
            pausedSounds.remove(pos);

            net.cozystudios.soundscape.network.SoundscapeClientNetworking.requestShuffleNext(pos);

            return true;
        }

        return false;
    }

    private static void playSoundWithCachedSettings(BlockPos pos, SoundEvent soundEvent, int durationTicks, EnhancedJukeboxSoundInstance previousSound) {
        JukeboxBlockEntity cachedJukebox = previousSound.getJukebox();
        EnhancedJukeboxSoundInstance newSound = new EnhancedJukeboxSoundInstance(soundEvent, pos, cachedJukebox, durationTicks);
        activeSounds.put(pos, newSound);
        Minecraft.getInstance().getSoundManager().play(newSound);
    }

    public static boolean isPlaying(BlockPos pos) {
        EnhancedJukeboxSoundInstance sound = activeSounds.get(pos);
        return sound != null && !sound.isStopped();
    }

    public static EnhancedJukeboxSoundInstance getSound(BlockPos pos) {
        return activeSounds.get(pos);
    }

    public static Set<BlockPos> getActivePositions() {
        return new HashSet<>(activeSounds.keySet());
    }

    public static void tick() {
        activeSounds.entrySet().removeIf(entry -> entry.getValue().isStopped());

        Minecraft client = Minecraft.getInstance();
        ClientLevel world = client.level;

        for (Map.Entry<BlockPos, EnhancedJukeboxSoundInstance> entry : new HashMap<>(activeSounds).entrySet()) {
            BlockPos pos = entry.getKey();
            EnhancedJukeboxSoundInstance sound = entry.getValue();

            int songDuration = sound.getSongDurationTicks();
            float pitchAdjustedTicks = sound.getPitchAdjustedTicks();

            if (songDuration > 0 && pitchAdjustedTicks >= songDuration) {
                if (sound.isLoopModeActive() || sound.isShuffleModeActive()) {
                    handleSongEnded(pos);
                }
            }
        }

        if (!pendingPlayback.isEmpty() && world != null) {
            pendingPlayback.entrySet().removeIf(entry -> {
                BlockPos pos = entry.getKey();
                int[] data = entry.getValue();
                int ticksWaited = data[0] + 1;
                int itemRawId = data[1];

                if (ticksWaited > MAX_PLAYBACK_WAIT_TICKS) {
                    return true;
                }

                if (activeSounds.containsKey(pos)) {
                    return true;
                }

                if (tryPlayFromPending(pos, itemRawId, world)) {
                    return true;
                }

                data[0] = ticksWaited;
                return false;
            });
        }

        if (!pendingResume.isEmpty() && world != null) {
            pendingResume.entrySet().removeIf(entry -> {
                BlockPos pos = entry.getKey();
                int ticksWaited = entry.getValue() + 1;

                if (ticksWaited > MAX_RESUME_WAIT_TICKS) {
                    return true;
                }

                if (activeSounds.containsKey(pos)) {
                    return true;
                }

                if (world.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
                    if (tryResumeJukeboxNow(pos, jukebox, world)) {
                        return true;
                    }
                } else {
                    return true;
                }

                entry.setValue(ticksWaited);
                return false;
            });
        }
    }

    public static void schedulePendingPlayback(BlockPos pos, int itemRawId) {
        if (!pendingPlayback.containsKey(pos) && !activeSounds.containsKey(pos)) {
            pendingPlayback.put(pos, new int[]{0, itemRawId});
        }
    }

    private static boolean tryPlayFromPending(BlockPos pos, int itemRawId, ClientLevel world) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be == null) return false;
        if (!(be instanceof JukeboxBlockEntity jukebox)) return false;

        ItemStack disc = jukebox.getTheItem();

        if (disc.isEmpty() && itemRawId != 0) {
            Item item = BuiltInRegistries.ITEM.byId(itemRawId);
            if (item != null) {
                disc = item.getDefaultInstance();
            }
        }

        if (disc.isEmpty()) return false;

        var songOptional = JukeboxSong.fromStack(disc);
        if (songOptional.isEmpty()) {
            if (itemRawId != 0) {
                Item item = BuiltInRegistries.ITEM.byId(itemRawId);
                if (item != null) {
                    ItemStack fallbackDisc = item.getDefaultInstance();
                    songOptional = JukeboxSong.fromStack(fallbackDisc);
                }
            }
            if (songOptional.isEmpty()) return false;
        }

        JukeboxSong song = songOptional.get().value();
        SoundEvent sound = song.soundEvent().value();
        int durationTicks = Math.round(song.lengthInSeconds() * 20);

        playSound(pos, sound, jukebox, durationTicks);
        return true;
    }

    public static void stopAll() {
        for (EnhancedJukeboxSoundInstance sound : activeSounds.values()) {
            sound.stop();
            Minecraft.getInstance().getSoundManager().stop(sound);
        }
        activeSounds.clear();
        pausedSounds.clear();
    }

    public static void pauseSound(BlockPos pos) {
        EnhancedJukeboxSoundInstance sound = activeSounds.get(pos);
        if (sound == null || pausedSounds.contains(pos)) {
            return;
        }

        try {
            SoundManager soundManager = Minecraft.getInstance().getSoundManager();
            SoundEngine soundEngine = ((SoundManagerAccessor) soundManager).soundscape$getSoundEngine();
            Map<SoundInstance, ChannelAccess.ChannelHandle> sources = ((SoundEngineAccessor) soundEngine).soundscape$getInstanceToChannel();

            ChannelAccess.ChannelHandle handle = sources.get(sound);

            if (handle == null) {
                for (Map.Entry<SoundInstance, ChannelAccess.ChannelHandle> entry : sources.entrySet()) {
                    if (entry.getKey() == sound) {
                        handle = entry.getValue();
                        break;
                    }
                }
            }

            if (handle != null) {
                handle.execute(Channel::pause);
                pausedSounds.add(pos);
            }
        } catch (Exception e) {
        }
    }

    public static void resumeSound(BlockPos pos) {
        EnhancedJukeboxSoundInstance sound = activeSounds.get(pos);
        if (sound == null || !pausedSounds.contains(pos)) {
            return;
        }

        try {
            SoundManager soundManager = Minecraft.getInstance().getSoundManager();
            SoundEngine soundEngine = ((SoundManagerAccessor) soundManager).soundscape$getSoundEngine();
            Map<SoundInstance, ChannelAccess.ChannelHandle> sources = ((SoundEngineAccessor) soundEngine).soundscape$getInstanceToChannel();

            ChannelAccess.ChannelHandle handle = sources.get(sound);

            if (handle == null) {
                for (Map.Entry<SoundInstance, ChannelAccess.ChannelHandle> entry : sources.entrySet()) {
                    if (entry.getKey() == sound) {
                        handle = entry.getValue();
                        break;
                    }
                }
            }

            if (handle != null) {
                handle.execute(Channel::unpause);
                pausedSounds.remove(pos);
            }
        } catch (Exception e) {
        }
    }

    public static boolean isPausedOpenAL(BlockPos pos) {
        return pausedSounds.contains(pos);
    }

    public static void restartForLoop(BlockPos pos) {
        EnhancedJukeboxSoundInstance oldSound = activeSounds.get(pos);
        if (oldSound == null) {
            return;
        }

        SoundEvent soundEvent = oldSound.getSoundEvent();
        JukeboxBlockEntity jukebox = oldSound.getJukebox();
        int durationTicks = oldSound.getSongDurationTicks();

        if (jukebox == null || jukebox.isRemoved()) {
            activeSounds.remove(pos);
            pausedSounds.remove(pos);
            return;
        }

        oldSound.stop();
        Minecraft.getInstance().getSoundManager().stop(oldSound);
        activeSounds.remove(pos);
        pausedSounds.remove(pos);

        EnhancedJukeboxSoundInstance newSound = new EnhancedJukeboxSoundInstance(soundEvent, pos, jukebox, durationTicks);
        activeSounds.put(pos, newSound);
        Minecraft.getInstance().getSoundManager().play(newSound);
    }

    public static void checkForResumableJukeboxes() {
        Minecraft client = Minecraft.getInstance();
        ClientLevel world = client.level;
        if (world == null || client.player == null) {
            return;
        }

        BlockPos playerPos = client.player.blockPosition();
        int playerChunkX = playerPos.getX() >> 4;
        int playerChunkZ = playerPos.getZ() >> 4;
        int viewDistance = client.options.renderDistance().get();

        for (int dx = -viewDistance; dx <= viewDistance; dx++) {
            for (int dz = -viewDistance; dz <= viewDistance; dz++) {
                int cx = playerChunkX + dx;
                int cz = playerChunkZ + dz;
                if (!world.hasChunk(cx, cz)) {
                    continue;
                }
                LevelChunk chunk = world.getChunk(cx, cz);
                if (chunk == null) {
                    continue;
                }

                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (blockEntity instanceof JukeboxBlockEntity) {
                        BlockPos pos = blockEntity.getBlockPos();
                        if (!activeSounds.containsKey(pos)) {
                            net.cozystudios.soundscape.network.SoundscapeClientNetworking.requestJukeboxSettings(pos);
                        }
                    }
                }
            }
        }
    }

    public static void tryResumeJukeboxWithSong(BlockPos pos, JukeboxBlockEntity jukebox, String songId, int songDuration) {
        tryResumeJukeboxWithSong(pos, jukebox, songId, songDuration, 0);
    }

    public static void tryResumeJukeboxWithSong(BlockPos pos, JukeboxBlockEntity jukebox, String songId, int songDuration, int startTicks) {
        if (activeSounds.containsKey(pos)) {
            return;
        }

        if (songId == null || songId.isEmpty()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return;
        }

        var soundId = net.minecraft.resources.Identifier.tryParse(songId);
        if (soundId == null) {
            return;
        }
        var soundEvent = net.minecraft.sounds.SoundEvent.createVariableRangeEvent(soundId);
        stopSound(pos);

        EnhancedJukeboxSoundInstance soundInstance = new EnhancedJukeboxSoundInstance(soundEvent, pos, jukebox, songDuration, startTicks);
        activeSounds.put(pos, soundInstance);

        if (jukebox instanceof JukeboxSettingsProvider provider) {
            JukeboxSettings settings = provider.soundscape$getSettings();
            if (settings != null) {
                settings.setWasPlaying(true);
                sendWasPlayingToServer(pos, true);
            }
        }

        Minecraft.getInstance().getSoundManager().play(soundInstance);
    }

    public static void playSoundWithSettings(BlockPos pos, String songId, int songDuration, JukeboxSettings settings) {
        playSoundWithSettings(pos, songId, songDuration, settings, 0);
    }

    public static void playSoundWithSettings(BlockPos pos, String songId, int songDuration, JukeboxSettings settings, int startTicks) {
        if (activeSounds.containsKey(pos)) {
            return;
        }

        if (songId == null || songId.isEmpty()) {
            return;
        }

        if (settings == null) {
            return;
        }

        var soundIdParsed = net.minecraft.resources.Identifier.tryParse(songId);
        if (soundIdParsed == null) {
            return;
        }
        var soundEvent = net.minecraft.sounds.SoundEvent.createVariableRangeEvent(soundIdParsed);

        EnhancedJukeboxSoundInstance soundInstance = new EnhancedJukeboxSoundInstance(soundEvent, pos, null, songDuration, startTicks);
        soundInstance.injectCachedSettings(settings);
        activeSounds.put(pos, soundInstance);
        Minecraft.getInstance().getSoundManager().play(soundInstance);
    }

    @Deprecated
    public static void tryResumeJukebox(BlockPos pos, JukeboxBlockEntity jukebox) {
        Minecraft client = Minecraft.getInstance();
        ClientLevel world = client.level;
        if (world == null) {
            return;
        }

        if (activeSounds.containsKey(pos)) {
            return;
        }

        if (!tryResumeJukeboxNow(pos, jukebox, world)) {
            if (!pendingResume.containsKey(pos)) {
                pendingResume.put(pos, 0);
            }
        }
    }

    private static boolean tryResumeJukeboxNow(BlockPos pos, JukeboxBlockEntity jukebox, ClientLevel world) {
        if (activeSounds.containsKey(pos)) {
            return true;
        }

        ItemStack disc = jukebox.getTheItem();
        if (disc.isEmpty()) {
            return false;
        }

        var songOptional = JukeboxSong.fromStack(disc);
        if (songOptional.isEmpty()) {
            return true;
        }

        JukeboxSong song = songOptional.get().value();
        SoundEvent sound = song.soundEvent().value();
        int durationTicks = Math.round(song.lengthInSeconds() * 20);

        playSound(pos, sound, jukebox, durationTicks);
        return true;
    }
}
