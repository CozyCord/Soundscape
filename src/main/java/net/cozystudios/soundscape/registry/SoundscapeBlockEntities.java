package net.cozystudios.soundscape.registry;

import net.cozystudios.soundscape.SoundscapeId;
import net.cozystudios.soundscape.boombox.BoomboxBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class SoundscapeBlockEntities {

    public static final BlockEntityType<BoomboxBlockEntity> BOOMBOX_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            SoundscapeId.of("boombox"),
            FabricBlockEntityTypeBuilder.create(BoomboxBlockEntity::new, SoundscapeBlocks.BOOMBOX).build(null)
    );

    public static void register() {
    }
}
