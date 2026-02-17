package net.cozystudios.soundscape.network;

import net.cozystudios.soundscape.Soundscape;
import net.cozystudios.soundscape.SoundscapeId;
import net.cozystudios.soundscape.jukebox.JukeboxSettings;
import net.cozystudios.soundscape.jukebox.JukeboxSettingsProvider;
import net.cozystudios.soundscape.jukebox.PlaybackMode;
import net.cozystudios.soundscape.jukebox.ShuffleQueue;
import net.minecraft.block.BlockState;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

//? if >=1.20.5 {
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
//?} else {
/*import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
*///?}

public class SoundscapeNetworking {

    //? if >=1.20.5 {
    public record JukeboxSettingsPayload(BlockPos pos, int volume, int pitch, int range, int mode) implements CustomPayload {
        public static final Id<JukeboxSettingsPayload> ID = new Id<>(SoundscapeId.of("jukebox_settings"));
        public static final PacketCodec<RegistryByteBuf, JukeboxSettingsPayload> CODEC = PacketCodec.tuple(
                BlockPos.PACKET_CODEC, JukeboxSettingsPayload::pos,
                PacketCodecs.INTEGER, JukeboxSettingsPayload::volume,
                PacketCodecs.INTEGER, JukeboxSettingsPayload::pitch,
                PacketCodecs.INTEGER, JukeboxSettingsPayload::range,
                PacketCodecs.INTEGER, JukeboxSettingsPayload::mode,
                JukeboxSettingsPayload::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record JukeboxPlaybackPayload(BlockPos pos, String command) implements CustomPayload {
        public static final Id<JukeboxPlaybackPayload> ID = new Id<>(SoundscapeId.of("jukebox_playback"));
        public static final PacketCodec<RegistryByteBuf, JukeboxPlaybackPayload> CODEC = PacketCodec.tuple(
                BlockPos.PACKET_CODEC, JukeboxPlaybackPayload::pos,
                PacketCodecs.STRING, JukeboxPlaybackPayload::command,
                JukeboxPlaybackPayload::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record JukeboxWasPlayingPayload(BlockPos pos, int wasPlaying) implements CustomPayload {
        public static final Id<JukeboxWasPlayingPayload> ID = new Id<>(SoundscapeId.of("jukebox_was_playing"));
        public static final PacketCodec<RegistryByteBuf, JukeboxWasPlayingPayload> CODEC = PacketCodec.tuple(
                BlockPos.PACKET_CODEC, JukeboxWasPlayingPayload::pos,
                PacketCodecs.INTEGER, JukeboxWasPlayingPayload::wasPlaying,
                JukeboxWasPlayingPayload::new
        );

        public boolean isPlaying() {
            return wasPlaying != 0;
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record JukeboxSettingsRequestPayload(BlockPos pos) implements CustomPayload {
        public static final Id<JukeboxSettingsRequestPayload> ID = new Id<>(SoundscapeId.of("jukebox_settings_request"));
        public static final PacketCodec<RegistryByteBuf, JukeboxSettingsRequestPayload> CODEC = PacketCodec.tuple(
                BlockPos.PACKET_CODEC, JukeboxSettingsRequestPayload::pos,
                JukeboxSettingsRequestPayload::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record JukeboxSettingsSyncPayload(BlockPos pos, int volume, int pitch, int range, int mode, int paused, int wasPlaying, String songId, int songDuration) implements CustomPayload {
        public static final Id<JukeboxSettingsSyncPayload> ID = new Id<>(SoundscapeId.of("jukebox_settings_sync"));
        public static final PacketCodec<RegistryByteBuf, JukeboxSettingsSyncPayload> CODEC = PacketCodec.of(
                (value, buf) -> {
                    BlockPos.PACKET_CODEC.encode(buf, value.pos());
                    PacketCodecs.INTEGER.encode(buf, value.volume());
                    PacketCodecs.INTEGER.encode(buf, value.pitch());
                    PacketCodecs.INTEGER.encode(buf, value.range());
                    PacketCodecs.INTEGER.encode(buf, value.mode());
                    PacketCodecs.INTEGER.encode(buf, value.paused());
                    PacketCodecs.INTEGER.encode(buf, value.wasPlaying());
                    PacketCodecs.STRING.encode(buf, value.songId());
                    PacketCodecs.INTEGER.encode(buf, value.songDuration());
                },
                buf -> new JukeboxSettingsSyncPayload(
                        BlockPos.PACKET_CODEC.decode(buf),
                        PacketCodecs.INTEGER.decode(buf),
                        PacketCodecs.INTEGER.decode(buf),
                        PacketCodecs.INTEGER.decode(buf),
                        PacketCodecs.INTEGER.decode(buf),
                        PacketCodecs.INTEGER.decode(buf),
                        PacketCodecs.INTEGER.decode(buf),
                        PacketCodecs.STRING.decode(buf),
                        PacketCodecs.INTEGER.decode(buf)
                )
        );

        public boolean isPaused() {
            return paused != 0;
        }

        public boolean wasPlayingBool() {
            return wasPlaying != 0;
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record JukeboxShuffleNextPayload(BlockPos pos) implements CustomPayload {
        public static final Id<JukeboxShuffleNextPayload> ID = new Id<>(SoundscapeId.of("jukebox_shuffle_next"));
        public static final PacketCodec<RegistryByteBuf, JukeboxShuffleNextPayload> CODEC = PacketCodec.tuple(
                BlockPos.PACKET_CODEC, JukeboxShuffleNextPayload::pos,
                JukeboxShuffleNextPayload::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record JukeboxParticlesPayload(BlockPos pos, int showParticles) implements CustomPayload {
        public static final Id<JukeboxParticlesPayload> ID = new Id<>(SoundscapeId.of("jukebox_particles"));
        public static final PacketCodec<RegistryByteBuf, JukeboxParticlesPayload> CODEC = PacketCodec.tuple(
                BlockPos.PACKET_CODEC, JukeboxParticlesPayload::pos,
                PacketCodecs.INTEGER, JukeboxParticlesPayload::showParticles,
                JukeboxParticlesPayload::new
        );

        public boolean showParticlesBool() {
            return showParticles != 0;
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record BoomboxActionPayload(BlockPos pos, int action, String url, float volume, long positionMs) implements CustomPayload {
        public static final Id<BoomboxActionPayload> ID = new Id<>(SoundscapeId.of("boombox_action"));
        public static final PacketCodec<RegistryByteBuf, BoomboxActionPayload> CODEC = PacketCodec.of(
                (value, buf) -> {
                    BlockPos.PACKET_CODEC.encode(buf, value.pos());
                    PacketCodecs.INTEGER.encode(buf, value.action());
                    PacketCodecs.STRING.encode(buf, value.url());
                    buf.writeFloat(value.volume());
                    buf.writeLong(value.positionMs());
                },
                buf -> new BoomboxActionPayload(
                        BlockPos.PACKET_CODEC.decode(buf),
                        PacketCodecs.INTEGER.decode(buf),
                        PacketCodecs.STRING.decode(buf),
                        buf.readFloat(),
                        buf.readLong()
                )
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record BoomboxStateSyncPayload(BlockPos pos, String url, int stateId, float volume,
                                          int loop, String errorMessage, long trackPositionMs,
                                          int trackIndex, int range) implements CustomPayload {
        public static final Id<BoomboxStateSyncPayload> ID = new Id<>(SoundscapeId.of("boombox_state_sync"));
        public static final PacketCodec<RegistryByteBuf, BoomboxStateSyncPayload> CODEC = PacketCodec.of(
                (value, buf) -> {
                    BlockPos.PACKET_CODEC.encode(buf, value.pos());
                    PacketCodecs.STRING.encode(buf, value.url());
                    PacketCodecs.INTEGER.encode(buf, value.stateId());
                    buf.writeFloat(value.volume());
                    PacketCodecs.INTEGER.encode(buf, value.loop());
                    PacketCodecs.STRING.encode(buf, value.errorMessage());
                    buf.writeLong(value.trackPositionMs());
                    PacketCodecs.INTEGER.encode(buf, value.trackIndex());
                    PacketCodecs.INTEGER.encode(buf, value.range());
                },
                buf -> new BoomboxStateSyncPayload(
                        BlockPos.PACKET_CODEC.decode(buf),
                        PacketCodecs.STRING.decode(buf),
                        PacketCodecs.INTEGER.decode(buf),
                        buf.readFloat(),
                        PacketCodecs.INTEGER.decode(buf),
                        PacketCodecs.STRING.decode(buf),
                        buf.readLong(),
                        PacketCodecs.INTEGER.decode(buf),
                        PacketCodecs.INTEGER.decode(buf)
                )
        );

        public boolean isLoop() {
            return loop != 0;
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    //?} else {
    /*public static final Identifier JUKEBOX_SETTINGS_C2S = SoundscapeId.of("jukebox_settings");
    public static final Identifier JUKEBOX_PLAYBACK_C2S = SoundscapeId.of("jukebox_playback");
    public static final Identifier JUKEBOX_WAS_PLAYING_C2S = SoundscapeId.of("jukebox_was_playing");
    public static final Identifier JUKEBOX_SETTINGS_REQUEST_C2S = SoundscapeId.of("jukebox_settings_request");
    public static final Identifier JUKEBOX_SHUFFLE_NEXT_C2S = SoundscapeId.of("jukebox_shuffle_next");
    public static final Identifier JUKEBOX_PARTICLES_C2S = SoundscapeId.of("jukebox_particles");
    public static final Identifier BOOMBOX_ACTION_C2S = SoundscapeId.of("boombox_action");
    public static final Identifier BOOMBOX_STATE_SYNC_S2C = SoundscapeId.of("boombox_state_sync");
    *///?}

    public static void registerServer() {
        //? if >=1.20.5 {
        PayloadTypeRegistry.playC2S().register(JukeboxSettingsPayload.ID, JukeboxSettingsPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(JukeboxPlaybackPayload.ID, JukeboxPlaybackPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(JukeboxWasPlayingPayload.ID, JukeboxWasPlayingPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(JukeboxSettingsRequestPayload.ID, JukeboxSettingsRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(JukeboxShuffleNextPayload.ID, JukeboxShuffleNextPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(JukeboxParticlesPayload.ID, JukeboxParticlesPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(JukeboxSettingsSyncPayload.ID, JukeboxSettingsSyncPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(JukeboxSettingsPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            MinecraftServer server = getServerFromPlayer(player);
            server.execute(() -> {
                handleSettings(server, player, payload.pos(), payload.volume(), payload.pitch(), payload.range(), payload.mode());
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(JukeboxPlaybackPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            MinecraftServer server = getServerFromPlayer(player);
            server.execute(() -> {
                handlePlayback(server, player, payload.pos(), payload.command());
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(JukeboxWasPlayingPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            MinecraftServer server = getServerFromPlayer(player);
            server.execute(() -> {
                handleWasPlaying(server, player, payload.pos(), payload.isPlaying());
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(JukeboxSettingsRequestPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            MinecraftServer server = getServerFromPlayer(player);
            server.execute(() -> {
                handleSettingsRequest(server, player, payload.pos());
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(JukeboxShuffleNextPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            MinecraftServer server = getServerFromPlayer(player);
            server.execute(() -> {
                handleShuffleNext(server, player, payload.pos());
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(JukeboxParticlesPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            MinecraftServer server = getServerFromPlayer(player);
            server.execute(() -> {
                handleParticles(server, player, payload.pos(), payload.showParticlesBool());
            });
        });

        PayloadTypeRegistry.playC2S().register(BoomboxActionPayload.ID, BoomboxActionPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(BoomboxStateSyncPayload.ID, BoomboxStateSyncPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(BoomboxActionPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            MinecraftServer server = getServerFromPlayer(player);
            server.execute(() -> {
                handleBoomboxAction(server, player, payload.pos(), payload.action(), payload.url(), payload.volume(), payload.positionMs());
            });
        });
        //?} else {
        /*ServerPlayNetworking.registerGlobalReceiver(JUKEBOX_SETTINGS_C2S, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            int volume = buf.readInt();
            int pitch = buf.readInt();
            int range = buf.readInt();
            int mode = buf.readInt();
            server.execute(() -> handleSettings(server, player, pos, volume, pitch, range, mode));
        });

        ServerPlayNetworking.registerGlobalReceiver(JUKEBOX_PLAYBACK_C2S, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            String command = buf.readString();
            server.execute(() -> handlePlayback(server, player, pos, command));
        });

        ServerPlayNetworking.registerGlobalReceiver(JUKEBOX_WAS_PLAYING_C2S, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            boolean wasPlaying = buf.readBoolean();
            server.execute(() -> handleWasPlaying(server, player, pos, wasPlaying));
        });

        ServerPlayNetworking.registerGlobalReceiver(JUKEBOX_SETTINGS_REQUEST_C2S, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            server.execute(() -> handleSettingsRequest(server, player, pos));
        });

        ServerPlayNetworking.registerGlobalReceiver(JUKEBOX_SHUFFLE_NEXT_C2S, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            server.execute(() -> handleShuffleNext(server, player, pos));
        });

        ServerPlayNetworking.registerGlobalReceiver(JUKEBOX_PARTICLES_C2S, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            boolean showParticles = buf.readBoolean();
            server.execute(() -> handleParticles(server, player, pos, showParticles));
        });

        ServerPlayNetworking.registerGlobalReceiver(BOOMBOX_ACTION_C2S, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            int action = buf.readInt();
            String url = buf.readString();
            float volume = buf.readFloat();
            long positionMs = buf.readLong();
            server.execute(() -> handleBoomboxAction(server, player, pos, action, url, volume, positionMs));
        });
        *///?}
    }

    private static void handleSettings(MinecraftServer server, ServerPlayerEntity player, BlockPos pos, int volume, int pitch, int range, int mode) {
        ServerWorld world = getPlayerWorld(server, player, pos);
        if (world == null) return;

        if (world.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
            if (jukebox instanceof JukeboxSettingsProvider provider) {
                JukeboxSettings settings = provider.soundscape$getSettings();
                settings.setVolume(volume);
                settings.setPitch(pitch);
                settings.setRange(range);
                PlaybackMode[] modes = PlaybackMode.values();
                if (mode >= 0 && mode < modes.length) {
                    PlaybackMode newMode = modes[mode];
                    PlaybackMode oldMode = settings.getPlaybackMode();
                    if (oldMode == PlaybackMode.LOOP && newMode != PlaybackMode.LOOP) {
                        settings.setWasPlaying(false);
                    }
                    //? if >=1.21 {
                    else if (oldMode != PlaybackMode.LOOP && newMode == PlaybackMode.LOOP && !jukebox.getStack(0).isEmpty()) {
                        settings.setWasPlaying(true);
                    }
                    //?} else {
                    /*else if (oldMode != PlaybackMode.LOOP && newMode == PlaybackMode.LOOP && !jukebox.getStack().isEmpty()) {
                        settings.setWasPlaying(true);
                    }
                    *///?}
                    settings.setPlaybackMode(newMode);
                }
                jukebox.markDirty();
                updatePlayingBlockState(world, pos, settings, jukebox);
                syncSettingsToClients(world, pos, settings);
            }
        }
    }

    private static void handlePlayback(MinecraftServer server, ServerPlayerEntity player, BlockPos pos, String command) {
        ServerWorld world = getPlayerWorld(server, player, pos);
        if (world == null) return;

        if (world.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
            if (jukebox instanceof JukeboxSettingsProvider provider) {
                JukeboxSettings settings = provider.soundscape$getSettings();
                switch (command) {
                    case "play" -> {
                        settings.setPaused(false);
                        settings.setWasPlaying(true);

                        //? if >=1.21 {
                        boolean jukeboxEmpty = jukebox.getStack(0).isEmpty();
                        //?} else {
                        /*boolean jukeboxEmpty = jukebox.getStack().isEmpty();
                        *///?}

                        if (jukeboxEmpty) {
                            ShuffleQueue queue = provider.soundscape$getShuffleQueue();
                            if (queue.hasDiscs()) {
                                int nextIndex;
                                if (settings.getPlaybackMode() == PlaybackMode.SHUFFLE) {
                                    nextIndex = queue.getRandomDiscIndex(world.getRandom());
                                } else {
                                    nextIndex = queue.getFirstDiscIndex();
                                }
                                if (nextIndex >= 0) {
                                    ItemStack nextDisc = queue.getStack(nextIndex);
                                    if (!nextDisc.isEmpty()) {
                                        queue.setStack(nextIndex, ItemStack.EMPTY);
                                        //? if >=1.21 {
                                        jukebox.setStack(0, nextDisc.copy());
                                        //?} else {
                                        /*jukebox.setStack(nextDisc.copy());
                                        *///?}
                                        jukeboxEmpty = false;
                                    }
                                }
                            }
                        }

                        if (!jukeboxEmpty) {
                            //? if >=1.21 {
                            ItemStack discStack = jukebox.getStack(0);
                            world.emitGameEvent(null, net.minecraft.world.event.GameEvent.JUKEBOX_PLAY, pos);
                            world.syncWorldEvent(null, net.minecraft.world.WorldEvents.JUKEBOX_STARTS_PLAYING, pos, net.minecraft.item.Item.getRawId(discStack.getItem()));
                            //?} else {
                            /*jukebox.startPlaying();
                            *///?}
                        }
                    }
                    case "pause" -> settings.setPaused(true);
                    case "stop" -> {
                        settings.setPaused(true);
                        settings.setPlaybackProgress(0);
                    }
                }
                jukebox.markDirty();
                updatePlayingBlockState(world, pos, settings, jukebox);
                syncSettingsToClients(world, pos, settings);
            }
        }
    }

    private static void handleWasPlaying(MinecraftServer server, ServerPlayerEntity player, BlockPos pos, boolean wasPlaying) {
        ServerWorld world = getPlayerWorld(server, player, pos);
        if (world == null) return;

        if (world.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
            if (jukebox instanceof JukeboxSettingsProvider provider) {
                JukeboxSettings settings = provider.soundscape$getSettings();
                settings.setWasPlaying(wasPlaying);
                jukebox.markDirty();
                updatePlayingBlockState(world, pos, settings, jukebox);
            }
        }
    }

    private static void handleSettingsRequest(MinecraftServer server, ServerPlayerEntity player, BlockPos pos) {
        ServerWorld world = getPlayerWorld(server, player, pos);
        if (world == null) return;

        if (world.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
            if (jukebox instanceof JukeboxSettingsProvider provider) {
                JukeboxSettings settings = provider.soundscape$getSettings();
                String[] songInfo = getSongInfo(jukebox, world);
                sendSettingsToPlayer(player, pos, settings, songInfo[0], Integer.parseInt(songInfo[1]));
            }
        }
    }

    private static void handleShuffleNext(MinecraftServer server, ServerPlayerEntity player, BlockPos pos) {
        ServerWorld world = getPlayerWorld(server, player, pos);
        if (world == null) return;

        if (world.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
            if (jukebox instanceof JukeboxSettingsProvider provider) {
                JukeboxSettings settings = provider.soundscape$getSettings();
                if (settings.getPlaybackMode() != PlaybackMode.SHUFFLE) {
                    return;
                }

                ShuffleQueue queue = provider.soundscape$getShuffleQueue();
                if (!queue.hasDiscs()) {
                    return;
                }

                //? if >=1.21 {
                ItemStack currentDisc = jukebox.getStack(0);
                //?} else {
                /*ItemStack currentDisc = jukebox.getStack();
                *///?}

                int nextIndex = queue.getRandomDiscIndex(world.getRandom());
                if (nextIndex < 0) {
                    return;
                }

                ItemStack nextDisc = queue.getStack(nextIndex);
                if (nextDisc.isEmpty()) {
                    return;
                }

                queue.setStack(nextIndex, ItemStack.EMPTY);

                if (!currentDisc.isEmpty()) {
                    for (int i = 0; i < ShuffleQueue.QUEUE_SIZE; i++) {
                        if (queue.getStack(i).isEmpty()) {
                            queue.setStack(i, currentDisc.copy());
                            break;
                        }
                    }
                }

                //? if >=1.21 {
                jukebox.setStack(0, nextDisc.copy());
                //?} else {
                /*jukebox.setStack(nextDisc.copy());
                *///?}

                settings.setWasPlaying(true);
                jukebox.markDirty();
                updatePlayingBlockState(world, pos, settings, jukebox);

                syncSettingsToClients(world, pos, settings);
            }
        }
    }

    private static void handleParticles(MinecraftServer server, ServerPlayerEntity player, BlockPos pos, boolean showParticles) {
        ServerWorld world = getPlayerWorld(server, player, pos);
        if (world == null) return;

        if (world.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
            if (jukebox instanceof JukeboxSettingsProvider provider) {
                JukeboxSettings settings = provider.soundscape$getSettings();
                settings.setShowParticles(showParticles);
                jukebox.markDirty();
            }
        }
    }

    //? if >=1.21 {
    private static String[] getSongInfo(JukeboxBlockEntity jukebox, ServerWorld world) {
        var disc = jukebox.getStack(0);
        if (disc.isEmpty()) {
            return new String[]{"", "0"};
        }
        var songOptional = net.minecraft.block.jukebox.JukeboxSong.getSongEntryFromStack(world.getRegistryManager(), disc);
        if (songOptional.isEmpty()) {
            return new String[]{"", "0"};
        }
        var song = songOptional.get().value();
        String soundId = song.soundEvent().getIdAsString();
        int duration = Math.round(song.lengthInSeconds() * 20);
        return new String[]{soundId, String.valueOf(duration)};
    }
    //?} else {
    /*private static String[] getSongInfo(JukeboxBlockEntity jukebox, ServerWorld world) {
        var disc = jukebox.getStack();
        if (disc.isEmpty()) {
            return new String[]{"", "0"};
        }
        if (disc.getItem() instanceof net.minecraft.item.MusicDiscItem musicDisc) {
            var sound = musicDisc.getSound();
            if (sound != null) {
                return new String[]{sound.getId().toString(), "0"};
            }
        }
        return new String[]{"", "0"};
    }
    *///?}

    //? if >=1.20.5 {
    private static void sendSettingsToPlayer(ServerPlayerEntity player, BlockPos pos, JukeboxSettings settings, String songId, int songDuration) {
        JukeboxSettingsSyncPayload payload = new JukeboxSettingsSyncPayload(
                pos,
                settings.getVolume(),
                settings.getPitch(),
                settings.getRange(),
                settings.getPlaybackMode().ordinal(),
                settings.isPaused() ? 1 : 0,
                settings.wasPlaying() ? 1 : 0,
                songId,
                songDuration
        );
        ServerPlayNetworking.send(player, payload);
    }
    //?} else {
    /*private static void sendSettingsToPlayer(ServerPlayerEntity player, BlockPos pos, JukeboxSettings settings, String songId, int songDuration) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeInt(settings.getVolume());
        buf.writeInt(settings.getPitch());
        buf.writeInt(settings.getRange());
        buf.writeInt(settings.getPlaybackMode().ordinal());
        buf.writeBoolean(settings.isPaused());
        buf.writeBoolean(settings.wasPlaying());
        buf.writeString(songId);
        buf.writeInt(songDuration);
        ServerPlayNetworking.send(player, SoundscapeId.of("jukebox_settings_sync"), buf);
    }
    *///?}

    //? if >=1.20.5 {
    private static void syncSettingsToClients(ServerWorld world, BlockPos pos, JukeboxSettings settings) {
        String songId = "";
        int songDuration = 0;
        if (world.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
            String[] songInfo = getSongInfo(jukebox, world);
            songId = songInfo[0];
            songDuration = Integer.parseInt(songInfo[1]);
        }
        JukeboxSettingsSyncPayload payload = new JukeboxSettingsSyncPayload(
                pos,
                settings.getVolume(),
                settings.getPitch(),
                settings.getRange(),
                settings.getPlaybackMode().ordinal(),
                settings.isPaused() ? 1 : 0,
                settings.wasPlaying() ? 1 : 0,
                songId,
                songDuration
        );
        int syncRange = settings.getRange() + 16;
        int syncRangeSquared = syncRange * syncRange;
        for (ServerPlayerEntity nearbyPlayer : world.getPlayers()) {
            if (nearbyPlayer.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) < syncRangeSquared) {
                ServerPlayNetworking.send(nearbyPlayer, payload);
            }
        }
    }
    //?} else {
    /*private static void syncSettingsToClients(ServerWorld world, BlockPos pos, JukeboxSettings settings) {
        String songId = "";
        int songDuration = 0;
        if (world.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
            String[] songInfo = getSongInfo(jukebox, world);
            songId = songInfo[0];
            songDuration = Integer.parseInt(songInfo[1]);
        }
        int syncRange = settings.getRange() + 16;
        int syncRangeSquared = syncRange * syncRange;
        for (ServerPlayerEntity nearbyPlayer : world.getPlayers()) {
            if (nearbyPlayer.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) < syncRangeSquared) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeBlockPos(pos);
                buf.writeInt(settings.getVolume());
                buf.writeInt(settings.getPitch());
                buf.writeInt(settings.getRange());
                buf.writeInt(settings.getPlaybackMode().ordinal());
                buf.writeBoolean(settings.isPaused());
                buf.writeBoolean(settings.wasPlaying());
                buf.writeString(songId);
                buf.writeInt(songDuration);
                ServerPlayNetworking.send(nearbyPlayer, SoundscapeId.of("jukebox_settings_sync"), buf);
            }
        }
    }
    *///?}

    //? if >=1.20.5 {
    private static MinecraftServer getServerFromPlayer(ServerPlayerEntity player) {
        return player.getCommandSource().getServer();
    }
    //?}

    @SuppressWarnings("unused")
    private static ServerWorld getPlayerWorld(MinecraftServer server, ServerPlayerEntity player, BlockPos pos) {
        return findWorldWithBlock(server, pos);
    }

    private static ServerWorld findWorldWithBlock(MinecraftServer server, BlockPos pos) {
        for (ServerWorld world : server.getWorlds()) {
            if (world.getBlockEntity(pos) instanceof JukeboxBlockEntity) {
                return world;
            }
        }
        return null;
    }

    private static void updatePlayingBlockState(ServerWorld world, BlockPos pos, JukeboxSettings settings, JukeboxBlockEntity jukebox) {
        //? if >=1.21 {
        boolean hasDisc = !jukebox.getStack(0).isEmpty();
        //?} else {
        /*boolean hasDisc = !jukebox.getStack().isEmpty();
        *///?}
        boolean shouldPlay = hasDisc && settings.wasPlaying() && !settings.isPaused();
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof JukeboxBlock && state.contains(Soundscape.JUKEBOX_PLAYING)) {
            if (state.get(Soundscape.JUKEBOX_PLAYING) != shouldPlay) {
                world.setBlockState(pos, state.with(Soundscape.JUKEBOX_PLAYING, shouldPlay));
            }
        }
    }

    private static void handleBoomboxAction(MinecraftServer server, ServerPlayerEntity player,
                                            BlockPos pos, int action, String url, float volume, long positionMs) {
        ServerWorld world = findWorldWithBoombox(server, pos);
        if (world == null) {
            net.cozystudios.soundscape.Soundscape.LOGGER.warn("[Boombox] Server: findWorldWithBoombox returned null for pos={}", pos);
            return;
        }

        if (player.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) > 64 * 64) {
            net.cozystudios.soundscape.Soundscape.LOGGER.warn("[Boombox] Server: player too far from boombox at {}", pos);
            return;
        }

        if (world.getBlockEntity(pos) instanceof net.cozystudios.soundscape.boombox.BoomboxBlockEntity boombox) {
            boombox.handleClientAction((byte) action, url, volume, positionMs);
        } else {
            net.cozystudios.soundscape.Soundscape.LOGGER.warn("[Boombox] Server: block entity at {} is not BoomboxBlockEntity", pos);
        }
    }

    private static ServerWorld findWorldWithBoombox(MinecraftServer server, BlockPos pos) {
        for (ServerWorld world : server.getWorlds()) {
            if (world.getBlockEntity(pos) instanceof net.cozystudios.soundscape.boombox.BoomboxBlockEntity) {
                return world;
            }
        }
        return null;
    }
}
