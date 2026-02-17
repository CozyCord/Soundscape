package net.cozystudios.soundscape.registry;

import net.cozystudios.soundscape.SoundscapeId;
import net.cozystudios.soundscape.boombox.BoomboxBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

//? if >=1.21.2 {
/*import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
*///?}

public class SoundscapeBlocks {

    public static final Block BOOMBOX = registerBlock("boombox",
            new BoomboxBlock(createBlockSettings("boombox")
                    .nonOpaque()
                    .strength(2.0f)));

    //? if >=1.21.2 {
    /*private static AbstractBlock.Settings createBlockSettings(String name) {
        return AbstractBlock.Settings.create()
                .registryKey(RegistryKey.of(RegistryKeys.BLOCK, SoundscapeId.of(name)));
    }
    *///?} else {
    private static AbstractBlock.Settings createBlockSettings(String name) {
        return AbstractBlock.Settings.create();
    }
    //?}

    private static Block registerBlock(String name, Block block) {
        return Registry.register(Registries.BLOCK, SoundscapeId.of(name), block);
    }

    public static void register() {
    }
}
