package net.cozystudios.soundscape.registry;

import net.cozystudios.soundscape.SoundscapeId;
import net.cozystudios.soundscape.boombox.BoomboxBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class SoundscapeBlocks {

    public static final Block BOOMBOX = registerBlock("boombox",
            new BoomboxBlock(createBlockSettings("boombox")
                    .noOcclusion()
                    .strength(2.0f)));

    private static BlockBehaviour.Properties createBlockSettings(String name) {
        return BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, SoundscapeId.of(name)));
    }

    private static Block registerBlock(String name, Block block) {
        return Registry.register(BuiltInRegistries.BLOCK, SoundscapeId.of(name), block);
    }

    public static void register() {
    }
}
