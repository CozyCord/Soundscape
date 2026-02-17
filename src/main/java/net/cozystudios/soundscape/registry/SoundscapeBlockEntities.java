package net.cozystudios.soundscape.registry;

import net.cozystudios.soundscape.SoundscapeId;
import net.cozystudios.soundscape.boombox.BoomboxBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class SoundscapeBlockEntities {

    public static final BlockEntityType<BoomboxBlockEntity> BOOMBOX_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            SoundscapeId.of("boombox"),
            FabricBlockEntityTypeBuilder.create(BoomboxBlockEntity::new, SoundscapeBlocks.BOOMBOX).build(null)
    );

    public static void register() {
    }
}
