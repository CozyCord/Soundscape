package net.cozystudios.soundscape.sound;

import net.cozystudios.soundscape.jukebox.JukeboxSettings;
import net.cozystudios.soundscape.jukebox.JukeboxSettingsProvider;
import net.cozystudios.soundscape.mixin.client.SoundManagerAccessor;
import net.cozystudios.soundscape.mixin.client.SoundSystemAccessor;
import net.cozystudios.soundscape.network.SoundscapeNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.Channel;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

//? if >=1.20.5 {
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
//?} else {
/*import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
*///?}

//? if >=1.21 {
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
//?} else {
/*import net.minecraft.item.Item;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.item.ItemStack;
*///?}

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

        MinecraftClient.getInstance().getSoundManager().play(soundInstance);
    }

    public static void playSound(BlockPos pos, SoundEvent sound, JukeboxBlockEntity jukebox) {
        playSound(pos, sound, jukebox, 0);
    }

    private static void sendWasPlayingToServer(BlockPos pos, boolean wasPlaying) {
        //? if >=1.20.5 {
        ClientPlayNetworking.send(new SoundscapeNetworking.JukeboxWasPlayingPayload(pos, wasPlaying ? 1 : 0));
        //?} else {
        /*PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeBoolean(wasPlaying);
        ClientPlayNetworking.send(SoundscapeNetworking.JUKEBOX_WAS_PLAYING_C2S, buf);
        *///?}
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
            MinecraftClient.getInstance().getSoundManager().stop(sound);
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

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null) {
            if (!(client.world.getBlockState(pos).getBlock() instanceof net.minecraft.block.JukeboxBlock)) {
                return false;
            }
        }

        if (sound.isLoopModeActive()) {
            SoundEvent soundEvent = sound.getSoundEvent();
            int durationTicks = sound.getSongDurationTicks();
            JukeboxBlockEntity jukebox = sound.getJukebox();

            sound.stop();
            MinecraftClient.getInstance().getSoundManager().stop(sound);
            activeSounds.remove(pos);
            pausedSounds.remove(pos);

            if (jukebox != null && !jukebox.isRemoved()) {
                EnhancedJukeboxSoundInstance newSound = new EnhancedJukeboxSoundInstance(soundEvent, pos, jukebox, durationTicks);
                activeSounds.put(pos, newSound);
                MinecraftClient.getInstance().getSoundManager().play(newSound);

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
            MinecraftClient.getInstance().getSoundManager().stop(sound);
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
        MinecraftClient.getInstance().getSoundManager().play(newSound);
    }

    public static boolean isPlaying(BlockPos pos) {
        EnhancedJukeboxSoundInstance sound = activeSounds.get(pos);
        return sound != null && !sound.isDone();
    }

    public static EnhancedJukeboxSoundInstance getSound(BlockPos pos) {
        return activeSounds.get(pos);
    }

    public static Set<BlockPos> getActivePositions() {
        return new HashSet<>(activeSounds.keySet());
    }

    public static void tick() {
        activeSounds.entrySet().removeIf(entry -> entry.getValue().isDone());

        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;

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

    private static boolean tryPlayFromPending(BlockPos pos, int itemRawId, ClientWorld world) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be == null) return false;
        if (!(be instanceof JukeboxBlockEntity jukebox)) return false;

        //? if >=1.21 {
        ItemStack disc = jukebox.getStack(0);

        if (disc.isEmpty() && itemRawId != 0) {
            Item item = Item.byRawId(itemRawId);
            if (item != null) {
                disc = item.getDefaultStack();
            }
        }

        if (disc.isEmpty()) return false;

        var songOptional = JukeboxSong.getSongEntryFromStack(world.getRegistryManager(), disc);
        if (songOptional.isEmpty()) {
            if (itemRawId != 0) {
                Item item = Item.byRawId(itemRawId);
                if (item != null) {
                    ItemStack fallbackDisc = item.getDefaultStack();
                    songOptional = JukeboxSong.getSongEntryFromStack(world.getRegistryManager(), fallbackDisc);
                }
            }
            if (songOptional.isEmpty()) return false;
        }

        JukeboxSong song = songOptional.get().value();
        SoundEvent sound = song.soundEvent().value();
        int durationTicks = Math.round(song.lengthInSeconds() * 20);

        playSound(pos, sound, jukebox, durationTicks);
        return true;
        //?} else {
        /*return false;
        *///?}
    }

    public static void stopAll() {
        for (EnhancedJukeboxSoundInstance sound : activeSounds.values()) {
            sound.stop();
            MinecraftClient.getInstance().getSoundManager().stop(sound);
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
            SoundManager soundManager = MinecraftClient.getInstance().getSoundManager();
            SoundSystem soundSystem = ((SoundManagerAccessor) soundManager).soundscape$getSoundSystem();
            Map<SoundInstance, Channel.SourceManager> sources = ((SoundSystemAccessor) soundSystem).soundscape$getSources();

            Channel.SourceManager sourceManager = sources.get(sound);

            if (sourceManager == null) {
                for (Map.Entry<SoundInstance, Channel.SourceManager> entry : sources.entrySet()) {
                    if (entry.getKey() == sound) {
                        sourceManager = entry.getValue();
                        break;
                    }
                }
            }

            if (sourceManager != null) {
                sourceManager.run(source -> source.pause());
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
            SoundManager soundManager = MinecraftClient.getInstance().getSoundManager();
            SoundSystem soundSystem = ((SoundManagerAccessor) soundManager).soundscape$getSoundSystem();
            Map<SoundInstance, Channel.SourceManager> sources = ((SoundSystemAccessor) soundSystem).soundscape$getSources();

            Channel.SourceManager sourceManager = sources.get(sound);

            if (sourceManager == null) {
                for (Map.Entry<SoundInstance, Channel.SourceManager> entry : sources.entrySet()) {
                    if (entry.getKey() == sound) {
                        sourceManager = entry.getValue();
                        break;
                    }
                }
            }

            if (sourceManager != null) {
                sourceManager.run(source -> source.resume());
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
        MinecraftClient.getInstance().getSoundManager().stop(oldSound);
        activeSounds.remove(pos);
        pausedSounds.remove(pos);

        EnhancedJukeboxSoundInstance newSound = new EnhancedJukeboxSoundInstance(soundEvent, pos, jukebox, durationTicks);
        activeSounds.put(pos, newSound);
        MinecraftClient.getInstance().getSoundManager().play(newSound);
    }

    public static void checkForResumableJukeboxes() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        if (world == null || client.player == null) {
            return;
        }

        BlockPos playerPos = client.player.getBlockPos();
        int playerChunkX = playerPos.getX() >> 4;
        int playerChunkZ = playerPos.getZ() >> 4;
        int viewDistance = client.options.getViewDistance().getValue();

        for (int dx = -viewDistance; dx <= viewDistance; dx++) {
            for (int dz = -viewDistance; dz <= viewDistance; dz++) {
                WorldChunk chunk = world.getChunkManager().getWorldChunk(playerChunkX + dx, playerChunkZ + dz, false);
                if (chunk == null) {
                    continue;
                }

                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (blockEntity instanceof JukeboxBlockEntity) {
                        BlockPos pos = blockEntity.getPos();
                        if (!activeSounds.containsKey(pos)) {
                            net.cozystudios.soundscape.network.SoundscapeClientNetworking.requestJukeboxSettings(pos);
                        }
                    }
                }
            }
        }
    }

    public static void tryResumeJukeboxWithSong(BlockPos pos, JukeboxBlockEntity jukebox, String songId, int songDuration) {
        if (activeSounds.containsKey(pos)) {
            return;
        }

        if (songId == null || songId.isEmpty()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return;
        }

        //? if >=1.21 {
        var soundId = net.minecraft.util.Identifier.tryParse(songId);
        if (soundId == null) {
            return;
        }
        var soundEvent = net.minecraft.sound.SoundEvent.of(soundId);
        playSound(pos, soundEvent, jukebox, songDuration);
        //?} else {
        /*var soundId = new net.minecraft.util.Identifier(songId);
        var soundEvent = net.minecraft.sound.SoundEvent.of(soundId);
        playSound(pos, soundEvent, jukebox, songDuration);
        *///?}
    }

    public static void playSoundWithSettings(BlockPos pos, String songId, int songDuration, JukeboxSettings settings) {
        if (activeSounds.containsKey(pos)) {
            return;
        }

        if (songId == null || songId.isEmpty()) {
            return;
        }

        if (settings == null) {
            return;
        }

        //? if >=1.21 {
        var soundIdParsed = net.minecraft.util.Identifier.tryParse(songId);
        if (soundIdParsed == null) {
            return;
        }
        var soundEvent = net.minecraft.sound.SoundEvent.of(soundIdParsed);

        EnhancedJukeboxSoundInstance soundInstance = new EnhancedJukeboxSoundInstance(soundEvent, pos, null, songDuration);
        soundInstance.injectCachedSettings(settings);
        activeSounds.put(pos, soundInstance);
        MinecraftClient.getInstance().getSoundManager().play(soundInstance);
        //?} else {
        /*var soundIdParsed = new net.minecraft.util.Identifier(songId);
        var soundEvent = net.minecraft.sound.SoundEvent.of(soundIdParsed);

        EnhancedJukeboxSoundInstance soundInstance = new EnhancedJukeboxSoundInstance(soundEvent, pos, null, songDuration);
        soundInstance.injectCachedSettings(settings);
        activeSounds.put(pos, soundInstance);
        MinecraftClient.getInstance().getSoundManager().play(soundInstance);
        *///?}
    }

    @Deprecated
    public static void tryResumeJukebox(BlockPos pos, JukeboxBlockEntity jukebox) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
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

    private static boolean tryResumeJukeboxNow(BlockPos pos, JukeboxBlockEntity jukebox, ClientWorld world) {
        if (activeSounds.containsKey(pos)) {
            return true;
        }

        //? if >=1.21 {
        ItemStack disc = jukebox.getStack(0);
        if (disc.isEmpty()) {
            return false;
        }

        var songOptional = JukeboxSong.getSongEntryFromStack(world.getRegistryManager(), disc);
        if (songOptional.isEmpty()) {
            return true;
        }

        JukeboxSong song = songOptional.get().value();
        SoundEvent sound = song.soundEvent().value();
        int durationTicks = Math.round(song.lengthInSeconds() * 20);

        playSound(pos, sound, jukebox, durationTicks);
        return true;
        //?} else {
        /*var discStack = jukebox.getStack();
        if (discStack.isEmpty()) {
            return false;
        }

        if (!(discStack.getItem() instanceof MusicDiscItem musicDisc)) {
            return true;
        }

        SoundEvent sound = musicDisc.getSound();
        if (sound == null) {
            return true;
        }

        playSound(pos, sound, jukebox);
        return true;
        *///?}
    }
}
