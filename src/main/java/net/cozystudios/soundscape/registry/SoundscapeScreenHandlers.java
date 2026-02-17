package net.cozystudios.soundscape.registry;

import net.cozystudios.soundscape.SoundscapeId;
import net.cozystudios.soundscape.screen.JukeboxScreenHandler;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.math.BlockPos;

//? if >=1.20.5 {
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.RegistryByteBuf;
//?} else {
/*import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.PacketByteBuf;
*///?}

public class SoundscapeScreenHandlers {

    //? if >=1.20.5 {
    public static final ScreenHandlerType<JukeboxScreenHandler> JUKEBOX_SCREEN_HANDLER =
            new ExtendedScreenHandlerType<>(
                    (syncId, playerInventory, data) -> new JukeboxScreenHandler(syncId, playerInventory, data),
                    BlockPos.PACKET_CODEC
            );
    //?} else {
    /*public static final ScreenHandlerType<JukeboxScreenHandler> JUKEBOX_SCREEN_HANDLER =
            new ExtendedScreenHandlerType<>((syncId, playerInventory, buf) -> new JukeboxScreenHandler(syncId, playerInventory, buf));
    *///?}

    public static void register() {
        Registry.register(Registries.SCREEN_HANDLER, SoundscapeId.of("jukebox"), JUKEBOX_SCREEN_HANDLER);
    }

    public static JukeboxScreenHandler createJukeboxScreenHandler(
            int syncId,
            PlayerInventory playerInventory,
            BlockPos pos,
            JukeboxBlockEntity jukebox
    ) {
        return new JukeboxScreenHandler(syncId, playerInventory, pos, jukebox);
    }
}
