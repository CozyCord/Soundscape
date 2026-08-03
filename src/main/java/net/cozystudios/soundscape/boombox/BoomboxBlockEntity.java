package net.cozystudios.soundscape.boombox;

import net.cozystudios.soundscape.registry.SoundscapeBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

//? if >=1.21.6 {
/*import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
*///?} elif >=1.20.5 {
import net.minecraft.registry.RegistryWrapper;
//?}

public class BoomboxBlockEntity extends BlockEntity {

    private String url = "";
    private float volume = 0.5f;
    private boolean loop = false;
    private int range = 48;
    private BoomboxState state = BoomboxState.IDLE;
    private String errorMessage = "";
    private long trackPositionMs = 0;
    private int trackIndex = 0;

    public enum BoomboxState {
        IDLE(0), PLAYING(1), STOPPED(2), ERROR(3), PAUSED(4);

        public final int id;

        BoomboxState(int id) {
            this.id = id;
        }

        public static BoomboxState fromId(int id) {
            return switch (id) {
                case 1 -> PLAYING;
                case 2 -> STOPPED;
                case 3 -> ERROR;
                case 4 -> PAUSED;
                default -> IDLE;
            };
        }
    }

    public BoomboxBlockEntity(BlockPos pos, BlockState state) {
        super(SoundscapeBlockEntities.BOOMBOX_BLOCK_ENTITY, pos, state);
    }

    public static final byte ACTION_SET_URL = 0;
    public static final byte ACTION_PLAY = 1;
    public static final byte ACTION_STOP = 2;
    public static final byte ACTION_SET_VOLUME = 3;
    public static final byte ACTION_TOGGLE_LOOP = 4;
    public static final byte ACTION_NEXT_TRACK = 5;
    public static final byte ACTION_PREV_TRACK = 6;
    public static final byte ACTION_PAUSE = 7;
    public static final byte ACTION_SKIP_FWD = 8;
    public static final byte ACTION_SKIP_BWD = 9;
    public static final byte ACTION_SET_RANGE = 10;

    private static final Pattern YOUTUBE_PATTERN = Pattern.compile(
            "^(https?://)?(www\\.)?(youtube\\.com/(watch\\?v=[\\w-]{11}|shorts/[\\w-]{11}|playlist\\?list=[\\w-]+)|youtu\\.be/[\\w-]{11}).*$"
    );

    public String getUrl() { return url; }
    public float getVolume() { return volume; }
    public boolean isLoop() { return loop; }
    public int getRange() { return range; }
    public BoomboxState getBoomboxState() { return state; }
    public String getErrorMessage() { return errorMessage; }
    public long getTrackPositionMs() { return trackPositionMs; }
    public int getTrackIndex() { return trackIndex; }

    public void handleClientAction(byte action, String urlParam, float volumeParam, long positionMs) {
        switch (action) {
            case ACTION_SET_URL -> {
                if (urlParam.isEmpty()) {
                    this.url = "";
                    this.state = BoomboxState.IDLE;
                    this.errorMessage = "";
                } else if (isValidYouTubeUrl(urlParam)) {
                    this.url = urlParam;
                    this.errorMessage = "";
                } else {
                    this.errorMessage = "Only YouTube URLs are supported";
                    this.state = BoomboxState.ERROR;
                }
            }
            case ACTION_PLAY -> {
                if (this.url.isEmpty()) {
                    this.errorMessage = "No URL set";
                    this.state = BoomboxState.ERROR;
                } else {
                    this.state = BoomboxState.PLAYING;
                    this.errorMessage = "";
                }
            }
            case ACTION_STOP -> {
                this.state = BoomboxState.STOPPED;
                this.errorMessage = "";
                this.trackPositionMs = 0;
            }
            case ACTION_SET_VOLUME -> {
                this.volume = Math.max(0.0f, Math.min(1.0f, volumeParam));
            }
            case ACTION_TOGGLE_LOOP -> {
                this.loop = !this.loop;
            }
            case ACTION_NEXT_TRACK -> {
                if (this.state == BoomboxState.PLAYING) {
                    broadcastToNearbyWithOverrideState(10);
                    return;
                }
            }
            case ACTION_PREV_TRACK -> {
                if (this.state == BoomboxState.PLAYING) {
                    broadcastToNearbyWithOverrideState(11);
                    return;
                }
            }
            case ACTION_PAUSE -> {
                if (this.state == BoomboxState.PLAYING) {
                    this.state = BoomboxState.PAUSED;
                    this.trackPositionMs = positionMs;
                    this.errorMessage = "";
                }
            }
            case ACTION_SKIP_FWD -> {
                if (this.state == BoomboxState.PLAYING || this.state == BoomboxState.PAUSED) {
                    this.trackPositionMs = positionMs + 15000;
                    broadcastToNearbyWithOverrideState(12);
                    return;
                }
            }
            case ACTION_SKIP_BWD -> {
                if (this.state == BoomboxState.PLAYING || this.state == BoomboxState.PAUSED) {
                    this.trackPositionMs = Math.max(0, positionMs - 15000);
                    broadcastToNearbyWithOverrideState(13);
                    return;
                }
            }
            case ACTION_SET_RANGE -> {
                this.range = Math.max(8, Math.min(128, (int) volumeParam));
            }
        }
        markDirty();
        broadcastToNearby();
    }

    private boolean isValidYouTubeUrl(String url) {
        return url != null && YOUTUBE_PATTERN.matcher(url).matches();
    }

    //? if >=1.20.5 {
    public void syncToPlayer(ServerPlayerEntity player) {
        net.cozystudios.soundscape.network.SoundscapeNetworking.BoomboxStateSyncPayload payload =
                new net.cozystudios.soundscape.network.SoundscapeNetworking.BoomboxStateSyncPayload(
                        pos, url, state.id, volume, loop ? 1 : 0, errorMessage, trackPositionMs, trackIndex, range);
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, payload);
    }

    public void broadcastToNearby() {
        if (!(this.world instanceof ServerWorld serverWorld)) return;
        net.cozystudios.soundscape.network.SoundscapeNetworking.BoomboxStateSyncPayload payload =
                new net.cozystudios.soundscape.network.SoundscapeNetworking.BoomboxStateSyncPayload(
                        pos, url, state.id, volume, loop ? 1 : 0, errorMessage, trackPositionMs, trackIndex, range);
        int syncRange = this.range + 16;
        int syncRangeSquared = syncRange * syncRange;
        for (ServerPlayerEntity nearbyPlayer : serverWorld.getPlayers()) {
            if (nearbyPlayer.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) < syncRangeSquared) {
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(nearbyPlayer, payload);
            }
        }
    }

    private void broadcastToNearbyWithOverrideState(int overrideState) {
        if (!(this.world instanceof ServerWorld serverWorld)) return;
        net.cozystudios.soundscape.network.SoundscapeNetworking.BoomboxStateSyncPayload payload =
                new net.cozystudios.soundscape.network.SoundscapeNetworking.BoomboxStateSyncPayload(
                        pos, url, overrideState, volume, loop ? 1 : 0, errorMessage, trackPositionMs, trackIndex, range);
        int syncRange = this.range + 16;
        int syncRangeSquared = syncRange * syncRange;
        for (ServerPlayerEntity nearbyPlayer : serverWorld.getPlayers()) {
            if (nearbyPlayer.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) < syncRangeSquared) {
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(nearbyPlayer, payload);
            }
        }
    }
    //?} else {
    /*public void syncToPlayer(ServerPlayerEntity player) {
        net.minecraft.network.PacketByteBuf buf = createSyncBuf(state.id);
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player,
                net.cozystudios.soundscape.SoundscapeId.of("boombox_state_sync"), buf);
    }

    public void broadcastToNearby() {
        if (!(this.world instanceof ServerWorld serverWorld)) return;
        int syncRange = this.range + 16;
        int syncRangeSquared = syncRange * syncRange;
        for (ServerPlayerEntity nearbyPlayer : serverWorld.getPlayers()) {
            if (nearbyPlayer.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) < syncRangeSquared) {
                net.minecraft.network.PacketByteBuf buf = createSyncBuf(state.id);
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(nearbyPlayer,
                        net.cozystudios.soundscape.SoundscapeId.of("boombox_state_sync"), buf);
            }
        }
    }

    private void broadcastToNearbyWithOverrideState(int overrideState) {
        if (!(this.world instanceof ServerWorld serverWorld)) return;
        int syncRange = this.range + 16;
        int syncRangeSquared = syncRange * syncRange;
        for (ServerPlayerEntity nearbyPlayer : serverWorld.getPlayers()) {
            if (nearbyPlayer.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) < syncRangeSquared) {
                net.minecraft.network.PacketByteBuf buf = createSyncBuf(overrideState);
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(nearbyPlayer,
                        net.cozystudios.soundscape.SoundscapeId.of("boombox_state_sync"), buf);
            }
        }
    }

    private net.minecraft.network.PacketByteBuf createSyncBuf(int stateId) {
        net.minecraft.network.PacketByteBuf buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeString(url);
        buf.writeInt(stateId);
        buf.writeFloat(volume);
        buf.writeBoolean(loop);
        buf.writeString(errorMessage);
        buf.writeLong(trackPositionMs);
        buf.writeInt(trackIndex);
        buf.writeInt(range);
        return buf;
    }
    *///?}

    public void broadcastStop() {
        this.state = BoomboxState.STOPPED;
        this.trackPositionMs = 0;
        markDirty();
        broadcastToNearby();
    }

    //? if >=1.20.5 && <1.21.6 {
    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }
    //?} elif <1.20.5 {
    /*@Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
    *///?}

    //? if >=1.21.6 {
    /*@Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.putString("Url", url);
        view.putFloat("Volume", volume);
        view.putBoolean("Loop", loop);
        view.putInt("Range", range);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        this.url = view.getString("Url", "");
        this.volume = view.getFloat("Volume", 0.5f);
        this.loop = view.getBoolean("Loop", false);
        this.range = view.getInt("Range", 48);
    }
    *///?} elif >=1.20.5 {
    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putString("Url", url);
        nbt.putFloat("Volume", volume);
        nbt.putBoolean("Loop", loop);
        nbt.putInt("Range", range);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.url = nbt.getString("Url");
        this.volume = nbt.contains("Volume") ? nbt.getFloat("Volume") : 0.5f;
        this.loop = nbt.getBoolean("Loop");
        this.range = nbt.contains("Range") ? nbt.getInt("Range") : 48;
    }
    //?} else {
    /*@Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putString("Url", url);
        nbt.putFloat("Volume", volume);
        nbt.putBoolean("Loop", loop);
        nbt.putInt("Range", range);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.url = nbt.getString("Url");
        this.volume = nbt.contains("Volume") ? nbt.getFloat("Volume") : 0.5f;
        this.loop = nbt.getBoolean("Loop");
        this.range = nbt.contains("Range") ? nbt.getInt("Range") : 48;
    }
    *///?}
}
