package net.cozystudios.soundscape.registry;

import net.cozystudios.soundscape.SoundscapeId;
import net.cozystudios.soundscape.screen.JukeboxScreenHandler;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;

public class SoundscapeScreenHandlers {

    public static final MenuType<JukeboxScreenHandler> JUKEBOX_SCREEN_HANDLER =
            new ExtendedMenuType<JukeboxScreenHandler, BlockPos>(
                    (syncId, playerInventory, data) -> new JukeboxScreenHandler(syncId, playerInventory, data),
                    BlockPos.STREAM_CODEC
            );

    public static void register() {
        Registry.register(BuiltInRegistries.MENU, SoundscapeId.of("jukebox"), JUKEBOX_SCREEN_HANDLER);
    }

    public static JukeboxScreenHandler createJukeboxScreenHandler(
            int syncId,
            Inventory playerInventory,
            BlockPos pos,
            JukeboxBlockEntity jukebox
    ) {
        return new JukeboxScreenHandler(syncId, playerInventory, pos, jukebox);
    }
}
