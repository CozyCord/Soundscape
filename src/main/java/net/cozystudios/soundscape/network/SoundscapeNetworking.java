package net.cozystudios.soundscape.network;

import net.cozystudios.soundscape.Soundscape;
import net.cozystudios.soundscape.SoundscapeId;
import net.cozystudios.soundscape.jukebox.JukeboxSettings;
import net.cozystudios.soundscape.jukebox.JukeboxSettingsProvider;
import net.cozystudios.soundscape.jukebox.PlaybackMode;
import net.cozystudios.soundscape.jukebox.ShuffleQueue;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SoundscapeNetworking {

    public record JukeboxSettingsPayload(BlockPos pos, int volume, int pitch, int range, int mode) implements CustomPacketPayload {
        public static final Type<JukeboxSettingsPayload> ID = new Type<>(SoundscapeId.of("jukebox_settings"));
        public static final StreamCodec<RegistryFriendlyByteBuf, JukeboxSettingsPayload> CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, JukeboxSettingsPayload::pos,
                ByteBufCodecs.VAR_INT, JukeboxSettingsPayload::volume,
                ByteBufCodecs.VAR_INT, JukeboxSettingsPayload::pitch,
                ByteBufCodecs.VAR_INT, JukeboxSettingsPayload::range,
                ByteBufCodecs.VAR_INT, JukeboxSettingsPayload::mode,
                JukeboxSettingsPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record JukeboxPlaybackPayload(BlockPos pos, String command) implements CustomPacketPayload {
        public static final Type<JukeboxPlaybackPayload> ID = new Type<>(SoundscapeId.of("jukebox_playback"));
        public static final StreamCodec<RegistryFriendlyByteBuf, JukeboxPlaybackPayload> CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, JukeboxPlaybackPayload::pos,
                ByteBufCodecs.STRING_UTF8, JukeboxPlaybackPayload::command,
                JukeboxPlaybackPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record JukeboxWasPlayingPayload(BlockPos pos, int wasPlaying) implements CustomPacketPayload {
        public static final Type<JukeboxWasPlayingPayload> ID = new Type<>(SoundscapeId.of("jukebox_was_playing"));
        public static final StreamCodec<RegistryFriendlyByteBuf, JukeboxWasPlayingPayload> CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, JukeboxWasPlayingPayload::pos,
                ByteBufCodecs.VAR_INT, JukeboxWasPlayingPayload::wasPlaying,
                JukeboxWasPlayingPayload::new
        );

        public boolean isPlaying() {
            return wasPlaying != 0;
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record JukeboxSettingsRequestPayload(BlockPos pos) implements CustomPacketPayload {
        public static final Type<JukeboxSettingsRequestPayload> ID = new Type<>(SoundscapeId.of("jukebox_settings_request"));
        public static final StreamCodec<RegistryFriendlyByteBuf, JukeboxSettingsRequestPayload> CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, JukeboxSettingsRequestPayload::pos,
                JukeboxSettingsRequestPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record JukeboxSettingsSyncPayload(BlockPos pos, int volume, int pitch, int range, int mode, int paused, int wasPlaying, String songId, int songDuration, int songPositionTicks) implements CustomPacketPayload {
        public static final Type<JukeboxSettingsSyncPayload> ID = new Type<>(SoundscapeId.of("jukebox_settings_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, JukeboxSettingsSyncPayload> CODEC = StreamCodec.of(
                (buf, value) -> {
                    BlockPos.STREAM_CODEC.encode(buf, value.pos());
                    ByteBufCodecs.VAR_INT.encode(buf, value.volume());
                    ByteBufCodecs.VAR_INT.encode(buf, value.pitch());
                    ByteBufCodecs.VAR_INT.encode(buf, value.range());
                    ByteBufCodecs.VAR_INT.encode(buf, value.mode());
                    ByteBufCodecs.VAR_INT.encode(buf, value.paused());
                    ByteBufCodecs.VAR_INT.encode(buf, value.wasPlaying());
                    ByteBufCodecs.STRING_UTF8.encode(buf, value.songId());
                    ByteBufCodecs.VAR_INT.encode(buf, value.songDuration());
                    ByteBufCodecs.VAR_INT.encode(buf, value.songPositionTicks());
                },
                buf -> new JukeboxSettingsSyncPayload(
                        BlockPos.STREAM_CODEC.decode(buf),
                        ByteBufCodecs.VAR_INT.decode(buf),
                        ByteBufCodecs.VAR_INT.decode(buf),
                        ByteBufCodecs.VAR_INT.decode(buf),
                        ByteBufCodecs.VAR_INT.decode(buf),
                        ByteBufCodecs.VAR_INT.decode(buf),
                        ByteBufCodecs.VAR_INT.decode(buf),
                        ByteBufCodecs.STRING_UTF8.decode(buf),
                        ByteBufCodecs.VAR_INT.decode(buf),
                        ByteBufCodecs.VAR_INT.decode(buf)
                )
        );

        public boolean isPaused() {
            return paused != 0;
        }

        public boolean wasPlayingBool() {
            return wasPlaying != 0;
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record JukeboxShuffleNextPayload(BlockPos pos) implements CustomPacketPayload {
        public static final Type<JukeboxShuffleNextPayload> ID = new Type<>(SoundscapeId.of("jukebox_shuffle_next"));
        public static final StreamCodec<RegistryFriendlyByteBuf, JukeboxShuffleNextPayload> CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, JukeboxShuffleNextPayload::pos,
                JukeboxShuffleNextPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record JukeboxParticlesPayload(BlockPos pos, int showParticles) implements CustomPacketPayload {
        public static final Type<JukeboxParticlesPayload> ID = new Type<>(SoundscapeId.of("jukebox_particles"));
        public static final StreamCodec<RegistryFriendlyByteBuf, JukeboxParticlesPayload> CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, JukeboxParticlesPayload::pos,
                ByteBufCodecs.VAR_INT, JukeboxParticlesPayload::showParticles,
                JukeboxParticlesPayload::new
        );

        public boolean showParticlesBool() {
            return showParticles != 0;
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record BoomboxActionPayload(BlockPos pos, int action, String url, float volume, long positionMs) implements CustomPacketPayload {
        public static final Type<BoomboxActionPayload> ID = new Type<>(SoundscapeId.of("boombox_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf, BoomboxActionPayload> CODEC = StreamCodec.of(
                (buf, value) -> {
                    BlockPos.STREAM_CODEC.encode(buf, value.pos());
                    ByteBufCodecs.VAR_INT.encode(buf, value.action());
                    ByteBufCodecs.STRING_UTF8.encode(buf, value.url());
                    buf.writeFloat(value.volume());
                    buf.writeLong(value.positionMs());
                },
                buf -> new BoomboxActionPayload(
                        BlockPos.STREAM_CODEC.decode(buf),
                        ByteBufCodecs.VAR_INT.decode(buf),
                        ByteBufCodecs.STRING_UTF8.decode(buf),
                        buf.readFloat(),
                        buf.readLong()
                )
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record BoomboxStateRequestPayload(BlockPos pos) implements CustomPacketPayload {
        public static final Type<BoomboxStateRequestPayload> ID = new Type<>(SoundscapeId.of("boombox_state_request"));
        public static final StreamCodec<RegistryFriendlyByteBuf, BoomboxStateRequestPayload> CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, BoomboxStateRequestPayload::pos,
                BoomboxStateRequestPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }


    public record BoomboxStateSyncPayload(BlockPos pos, String url, int stateId, float volume,
                                          int loop, String errorMessage, long trackPositionMs,
                                          int trackIndex, int range) implements CustomPacketPayload {
        public static final Type<BoomboxStateSyncPayload> ID = new Type<>(SoundscapeId.of("boombox_state_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, BoomboxStateSyncPayload> CODEC = StreamCodec.of(
                (buf, value) -> {
                    BlockPos.STREAM_CODEC.encode(buf, value.pos());
                    ByteBufCodecs.STRING_UTF8.encode(buf, value.url());
                    ByteBufCodecs.VAR_INT.encode(buf, value.stateId());
                    buf.writeFloat(value.volume());
                    ByteBufCodecs.VAR_INT.encode(buf, value.loop());
                    ByteBufCodecs.STRING_UTF8.encode(buf, value.errorMessage());
                    buf.writeLong(value.trackPositionMs());
                    ByteBufCodecs.VAR_INT.encode(buf, value.trackIndex());
                    ByteBufCodecs.VAR_INT.encode(buf, value.range());
                },
                buf -> new BoomboxStateSyncPayload(
                        BlockPos.STREAM_CODEC.decode(buf),
                        ByteBufCodecs.STRING_UTF8.decode(buf),
                        ByteBufCodecs.VAR_INT.decode(buf),
                        buf.readFloat(),
                        ByteBufCodecs.VAR_INT.decode(buf),
                        ByteBufCodecs.STRING_UTF8.decode(buf),
                        buf.readLong(),
                        ByteBufCodecs.VAR_INT.decode(buf),
                        ByteBufCodecs.VAR_INT.decode(buf)
                )
        );

        public boolean isLoop() {
            return loop != 0;
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public static void registerServer() {
        PayloadTypeRegistry.serverboundPlay().register(JukeboxSettingsPayload.ID, JukeboxSettingsPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(JukeboxPlaybackPayload.ID, JukeboxPlaybackPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(JukeboxWasPlayingPayload.ID, JukeboxWasPlayingPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(JukeboxSettingsRequestPayload.ID, JukeboxSettingsRequestPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(JukeboxShuffleNextPayload.ID, JukeboxShuffleNextPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(JukeboxParticlesPayload.ID, JukeboxParticlesPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(JukeboxSettingsSyncPayload.ID, JukeboxSettingsSyncPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(JukeboxSettingsPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            MinecraftServer server = getServerFromPlayer(player);
            server.execute(() -> {
                handleSettings(server, player, payload.pos(), payload.volume(), payload.pitch(), payload.range(), payload.mode());
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(JukeboxPlaybackPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            MinecraftServer server = getServerFromPlayer(player);
            server.execute(() -> {
                handlePlayback(server, player, payload.pos(), payload.command());
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(JukeboxWasPlayingPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            MinecraftServer server = getServerFromPlayer(player);
            server.execute(() -> {
                handleWasPlaying(server, player, payload.pos(), payload.isPlaying());
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(JukeboxSettingsRequestPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            MinecraftServer server = getServerFromPlayer(player);
            server.execute(() -> {
                handleSettingsRequest(server, player, payload.pos());
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(JukeboxShuffleNextPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            MinecraftServer server = getServerFromPlayer(player);
            server.execute(() -> {
                handleShuffleNext(server, player, payload.pos());
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(JukeboxParticlesPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            MinecraftServer server = getServerFromPlayer(player);
            server.execute(() -> {
                handleParticles(server, player, payload.pos(), payload.showParticlesBool());
            });
        });

        PayloadTypeRegistry.serverboundPlay().register(BoomboxActionPayload.ID, BoomboxActionPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(BoomboxStateRequestPayload.ID, BoomboxStateRequestPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BoomboxStateSyncPayload.ID, BoomboxStateSyncPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(BoomboxActionPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            MinecraftServer server = getServerFromPlayer(player);
            server.execute(() -> {
                handleBoomboxAction(server, player, payload.pos(), payload.action(), payload.url(), payload.volume(), payload.positionMs());
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(BoomboxStateRequestPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            MinecraftServer server = getServerFromPlayer(player);
            server.execute(() -> {
                handleBoomboxStateRequest(server, player, payload.pos());
            });
        });
    }

    private static void handleBoomboxStateRequest(MinecraftServer server, ServerPlayer player, BlockPos pos) {
        ServerLevel world = findWorldWithBoombox(server, pos);
        if (world == null) return;
        if (world.getBlockEntity(pos) instanceof net.cozystudios.soundscape.boombox.BoomboxBlockEntity boombox) {
            boombox.syncToPlayer(player);
        }
    }

    private static void handleSettings(MinecraftServer server, ServerPlayer player, BlockPos pos, int volume, int pitch, int range, int mode) {
        ServerLevel world = getPlayerWorld(server, player, pos);
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
                    } else if (oldMode != PlaybackMode.LOOP && newMode == PlaybackMode.LOOP && !jukebox.getTheItem().isEmpty()) {
                        settings.setWasPlaying(true);
                    }
                    settings.setPlaybackMode(newMode);
                }
                jukebox.setChanged();
                updatePlayingBlockState(world, pos, settings, jukebox);
                syncSettingsToClients(world, pos, settings);
            }
        }
    }

    private static void handlePlayback(MinecraftServer server, ServerPlayer player, BlockPos pos, String command) {
        ServerLevel world = getPlayerWorld(server, player, pos);
        if (world == null) return;

        if (world.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
            if (jukebox instanceof JukeboxSettingsProvider provider) {
                JukeboxSettings settings = provider.soundscape$getSettings();
                switch (command) {
                    case "play" -> {
                        settings.setPaused(false);
                        settings.setWasPlaying(true);

                        boolean jukeboxEmpty = jukebox.getTheItem().isEmpty();

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
                                    ItemStack nextDisc = queue.getItem(nextIndex);
                                    if (!nextDisc.isEmpty()) {
                                        queue.setItem(nextIndex, ItemStack.EMPTY);
                                        jukebox.setTheItem(nextDisc.copy());
                                        jukeboxEmpty = false;
                                    }
                                }
                            }
                        }

                        if (!jukeboxEmpty) {
                            ItemStack discStack = jukebox.getTheItem();
                            var songPlayer = jukebox.getSongPlayer();
                            var songOpt = net.minecraft.world.item.JukeboxSong.fromStack(discStack);
                            if (!songPlayer.isPlaying() && songOpt.isPresent()) {
                                songPlayer.play(world, songOpt.get());
                            } else {
                                world.gameEvent(null, net.minecraft.world.level.gameevent.GameEvent.JUKEBOX_PLAY, pos);
                            }
                        }
                    }
                    case "pause" -> settings.setPaused(true);
                    case "stop" -> {
                        settings.setPaused(true);
                        settings.setPlaybackProgress(0);
                    }
                }
                jukebox.setChanged();
                updatePlayingBlockState(world, pos, settings, jukebox);
                syncSettingsToClients(world, pos, settings);
            }
        }
    }

    private static void handleWasPlaying(MinecraftServer server, ServerPlayer player, BlockPos pos, boolean wasPlaying) {
        ServerLevel world = getPlayerWorld(server, player, pos);
        if (world == null) return;

        if (world.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
            if (jukebox instanceof JukeboxSettingsProvider provider) {
                JukeboxSettings settings = provider.soundscape$getSettings();
                settings.setWasPlaying(wasPlaying);
                jukebox.setChanged();
                updatePlayingBlockState(world, pos, settings, jukebox);
            }
        }
    }

    private static void handleSettingsRequest(MinecraftServer server, ServerPlayer player, BlockPos pos) {
        ServerLevel world = getPlayerWorld(server, player, pos);
        if (world == null) return;

        if (world.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
            if (jukebox instanceof JukeboxSettingsProvider provider) {
                JukeboxSettings settings = provider.soundscape$getSettings();
                String[] songInfo = getSongInfo(jukebox, world);
                sendSettingsToPlayer(player, pos, settings, songInfo[0], Integer.parseInt(songInfo[1]), Integer.parseInt(songInfo[2]));
            }
        }
    }

    private static void handleShuffleNext(MinecraftServer server, ServerPlayer player, BlockPos pos) {
        ServerLevel world = getPlayerWorld(server, player, pos);
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

                ItemStack currentDisc = jukebox.getTheItem();

                int nextIndex = queue.getRandomDiscIndex(world.getRandom());
                if (nextIndex < 0) {
                    return;
                }

                ItemStack nextDisc = queue.getItem(nextIndex);
                if (nextDisc.isEmpty()) {
                    return;
                }

                queue.setItem(nextIndex, ItemStack.EMPTY);

                if (!currentDisc.isEmpty()) {
                    for (int i = 0; i < ShuffleQueue.QUEUE_SIZE; i++) {
                        if (queue.getItem(i).isEmpty()) {
                            queue.setItem(i, currentDisc.copy());
                            break;
                        }
                    }
                }

                jukebox.setTheItem(nextDisc.copy());

                settings.setWasPlaying(true);
                jukebox.setChanged();
                updatePlayingBlockState(world, pos, settings, jukebox);

                syncSettingsToClients(world, pos, settings);
            }
        }
    }

    private static void handleParticles(MinecraftServer server, ServerPlayer player, BlockPos pos, boolean showParticles) {
        ServerLevel world = getPlayerWorld(server, player, pos);
        if (world == null) return;

        if (world.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
            if (jukebox instanceof JukeboxSettingsProvider provider) {
                JukeboxSettings settings = provider.soundscape$getSettings();
                settings.setShowParticles(showParticles);
                jukebox.setChanged();
            }
        }
    }

    private static String[] getSongInfo(JukeboxBlockEntity jukebox, ServerLevel world) {
        var disc = jukebox.getTheItem();
        if (disc.isEmpty()) {
            return new String[]{"", "0", "0"};
        }
        var songOptional = net.minecraft.world.item.JukeboxSong.fromStack(disc);
        if (songOptional.isEmpty()) {
            return new String[]{"", "0", "0"};
        }
        var song = songOptional.get().value();
        String soundId = song.soundEvent().unwrapKey().map(k -> k.identifier().toString()).orElse("");
        int duration = Math.round(song.lengthInSeconds() * 20);
        long rawPos = jukebox.getSongPlayer().getTicksSinceSongStarted();
        int position = duration > 0 ? (int)(rawPos % duration) : (int)Math.min(rawPos, Integer.MAX_VALUE);
        return new String[]{soundId, String.valueOf(duration), String.valueOf(position)};
    }

    private static void sendSettingsToPlayer(ServerPlayer player, BlockPos pos, JukeboxSettings settings, String songId, int songDuration, int songPositionTicks) {
        JukeboxSettingsSyncPayload payload = new JukeboxSettingsSyncPayload(
                pos,
                settings.getVolume(),
                settings.getPitch(),
                settings.getRange(),
                settings.getPlaybackMode().ordinal(),
                settings.isPaused() ? 1 : 0,
                settings.wasPlaying() ? 1 : 0,
                songId,
                songDuration,
                songPositionTicks
        );
        ServerPlayNetworking.send(player, payload);
    }

    private static void syncSettingsToClients(ServerLevel world, BlockPos pos, JukeboxSettings settings) {
        String songId = "";
        int songDuration = 0;
        int songPositionTicks = 0;
        if (world.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
            String[] songInfo = getSongInfo(jukebox, world);
            songId = songInfo[0];
            songDuration = Integer.parseInt(songInfo[1]);
            songPositionTicks = Integer.parseInt(songInfo[2]);
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
                songDuration,
                songPositionTicks
        );
        int syncRange = settings.getRange() + 16;
        int syncRangeSquared = syncRange * syncRange;
        for (ServerPlayer nearbyPlayer : world.players()) {
            if (nearbyPlayer.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < syncRangeSquared) {
                ServerPlayNetworking.send(nearbyPlayer, payload);
            }
        }
    }

    private static MinecraftServer getServerFromPlayer(ServerPlayer player) {
        return player.level().getServer();
    }

    @SuppressWarnings("unused")
    private static ServerLevel getPlayerWorld(MinecraftServer server, ServerPlayer player, BlockPos pos) {
        return findWorldWithBlock(server, pos);
    }

    private static ServerLevel findWorldWithBlock(MinecraftServer server, BlockPos pos) {
        for (ServerLevel world : server.getAllLevels()) {
            if (world.getBlockEntity(pos) instanceof JukeboxBlockEntity) {
                return world;
            }
        }
        return null;
    }

    private static void updatePlayingBlockState(ServerLevel world, BlockPos pos, JukeboxSettings settings, JukeboxBlockEntity jukebox) {
        boolean hasDisc = !jukebox.getTheItem().isEmpty();
        boolean shouldPlay = hasDisc && settings.wasPlaying() && !settings.isPaused();
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof JukeboxBlock && state.hasProperty(Soundscape.JUKEBOX_PLAYING)) {
            if (state.getValue(Soundscape.JUKEBOX_PLAYING) != shouldPlay) {
                world.setBlockAndUpdate(pos, state.setValue(Soundscape.JUKEBOX_PLAYING, shouldPlay));
            }
        }
    }

    private static void handleBoomboxAction(MinecraftServer server, ServerPlayer player,
                                            BlockPos pos, int action, String url, float volume, long positionMs) {
        ServerLevel world = findWorldWithBoombox(server, pos);
        if (world == null) {
            Soundscape.LOGGER.warn("[Boombox] Server: findWorldWithBoombox returned null for pos={}", pos);
            return;
        }

        if (player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) > 64 * 64) {
            Soundscape.LOGGER.warn("[Boombox] Server: player too far from boombox at {}", pos);
            return;
        }

        if (world.getBlockEntity(pos) instanceof net.cozystudios.soundscape.boombox.BoomboxBlockEntity boombox) {
            boombox.handleClientAction((byte) action, url, volume, positionMs);
        } else {
            Soundscape.LOGGER.warn("[Boombox] Server: block entity at {} is not BoomboxBlockEntity", pos);
        }
    }

    private static ServerLevel findWorldWithBoombox(MinecraftServer server, BlockPos pos) {
        for (ServerLevel world : server.getAllLevels()) {
            if (world.getBlockEntity(pos) instanceof net.cozystudios.soundscape.boombox.BoomboxBlockEntity) {
                return world;
            }
        }
        return null;
    }
}
