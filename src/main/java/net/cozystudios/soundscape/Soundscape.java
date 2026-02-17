package net.cozystudios.soundscape;

import net.cozystudios.soundscape.network.SoundscapeNetworking;
import net.cozystudios.soundscape.registry.SoundscapeBlockEntities;
import net.cozystudios.soundscape.registry.SoundscapeBlocks;
import net.cozystudios.soundscape.registry.SoundscapeItemGroups;
import net.cozystudios.soundscape.registry.SoundscapeItems;
import net.cozystudios.soundscape.registry.SoundscapeLootTables;
import net.cozystudios.soundscape.registry.SoundscapeScreenHandlers;
import net.cozystudios.soundscape.registry.SoundscapeSounds;
import net.fabricmc.api.ModInitializer;
import net.minecraft.state.property.BooleanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Soundscape implements ModInitializer {
    public static final String MOD_ID = "soundscape";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final BooleanProperty JUKEBOX_PLAYING = BooleanProperty.of("playing");

    @Override
    public void onInitialize() {
        SoundscapeSounds.register();
        SoundscapeBlocks.register();
        SoundscapeBlockEntities.register();
        SoundscapeItems.register();
        SoundscapeItemGroups.register();
        SoundscapeLootTables.register();
        SoundscapeScreenHandlers.register();
        SoundscapeNetworking.registerServer();

        LOGGER.info("Soundscape has been initialized!");
    }
}
