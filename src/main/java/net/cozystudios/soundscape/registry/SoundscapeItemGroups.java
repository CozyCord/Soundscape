package net.cozystudios.soundscape.registry;

import net.cozystudios.soundscape.SoundscapeId;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SoundscapeItemGroups {

    public static final CreativeModeTab SOUNDSCAPE_GROUP = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            SoundscapeId.of("soundscape"),
            FabricCreativeModeTab.builder()
                    .icon(() -> new ItemStack(SoundscapeItems.AMBIENCE_DISC))
                    .title(Component.translatable("itemGroup.soundscape.soundscape"))
                    .displayItems((context, entries) -> {
                        entries.accept(SoundscapeItems.BOOMBOX);
                        entries.accept(Items.JUKEBOX);
                        entries.accept(SoundscapeItems.AMBIENCE_DISC_SHARD);
                        entries.accept(SoundscapeItems.AMBIENCE_DISC);

                        entries.accept(SoundscapeItems.MUSIC_DISC_FOREST_SUMMER_BIRDS);
                        entries.accept(SoundscapeItems.MUSIC_DISC_FOREST_SWAMP_1);
                        entries.accept(SoundscapeItems.MUSIC_DISC_FOREST_SWAMP_2);
                        entries.accept(SoundscapeItems.MUSIC_DISC_FOREST_RAIN);
                        entries.accept(SoundscapeItems.MUSIC_DISC_LEAVES_RUSTLING);
                        entries.accept(SoundscapeItems.MUSIC_DISC_CRICKETS);
                        entries.accept(SoundscapeItems.MUSIC_DISC_WINDCHIMES_1);
                        entries.accept(SoundscapeItems.MUSIC_DISC_WINDCHIMES_2);

                        entries.accept(SoundscapeItems.MUSIC_DISC_OCEAN_WAVES_1);
                        entries.accept(SoundscapeItems.MUSIC_DISC_OCEAN_WAVES_2);
                        entries.accept(SoundscapeItems.MUSIC_DISC_OCEAN_WAVES_3);
                        entries.accept(SoundscapeItems.MUSIC_DISC_RIVER_STREAM);
                        entries.accept(SoundscapeItems.MUSIC_DISC_RIVER_STREAM_BOULDERS);
                        entries.accept(SoundscapeItems.MUSIC_DISC_RIVER_STREAM_FAST);
                        entries.accept(SoundscapeItems.MUSIC_DISC_RAGING_RIVER);
                        entries.accept(SoundscapeItems.MUSIC_DISC_WATERFALL_CRASHING_1);
                        entries.accept(SoundscapeItems.MUSIC_DISC_WATERFALL_CRASHING_2);
                        entries.accept(SoundscapeItems.MUSIC_DISC_WATERFALL_CRASHING_3);
                        entries.accept(SoundscapeItems.MUSIC_DISC_WATERFALL_CRASHING_4);
                        entries.accept(SoundscapeItems.MUSIC_DISC_WATER_BUBBLING);
                        entries.accept(SoundscapeItems.MUSIC_DISC_WATER_BUBBLING_UNDERWATER);

                        entries.accept(SoundscapeItems.MUSIC_DISC_THUNDERSTORM);
                        entries.accept(SoundscapeItems.MUSIC_DISC_WIND_1);
                        entries.accept(SoundscapeItems.MUSIC_DISC_WIND_2_STRONG);

                        entries.accept(SoundscapeItems.MUSIC_DISC_FIRE_BURNING_1);
                        entries.accept(SoundscapeItems.MUSIC_DISC_FIRE_BURNING_2);
                        entries.accept(SoundscapeItems.MUSIC_DISC_FIRE_BURNING_3);
                        entries.accept(SoundscapeItems.MUSIC_DISC_FIRE_BURNING_4);

                        entries.accept(SoundscapeItems.MUSIC_DISC_HORROR_ABANDONED_CHURCH);
                        entries.accept(SoundscapeItems.MUSIC_DISC_HORROR_CAVE_AMBIENCE);
                        entries.accept(SoundscapeItems.MUSIC_DISC_HORROR_SCARY_STORM);
                        entries.accept(SoundscapeItems.MUSIC_DISC_CAVE_AMBIENCE);

                        entries.accept(SoundscapeItems.MUSIC_DISC_CITYSCAPE_AIRPORT);
                        entries.accept(SoundscapeItems.MUSIC_DISC_CITYSCAPE_BROOKLYN_PLAYGROUND);
                        entries.accept(SoundscapeItems.MUSIC_DISC_CITYSCAPE_NY_STREETS);
                        entries.accept(SoundscapeItems.MUSIC_DISC_CITYSCAPE_NY_SUBWAY);
                        entries.accept(SoundscapeItems.MUSIC_DISC_CITYSCAPE_PARTY_CLUB);
                        entries.accept(SoundscapeItems.MUSIC_DISC_CITYSCAPE_SF_SUBWAY_BART);
                        entries.accept(SoundscapeItems.MUSIC_DISC_CITYSCAPE_SKATEPARK);

                        entries.accept(SoundscapeItems.MUSIC_DISC_FACTORY_BASEMENT);
                        entries.accept(SoundscapeItems.MUSIC_DISC_FACTORY_ELECTRONIC_ROOM);
                        entries.accept(SoundscapeItems.MUSIC_DISC_FACTORY_HUM);
                        entries.accept(SoundscapeItems.MUSIC_DISC_FACTORY_MACHINE_ROOM);
                        entries.accept(SoundscapeItems.MUSIC_DISC_COGS_TURNING);
                        entries.accept(SoundscapeItems.MUSIC_DISC_ROCKS_GRINDING);

                        entries.accept(SoundscapeItems.MUSIC_DISC_BLOCKS_BREAKING);
                        entries.accept(SoundscapeItems.MUSIC_DISC_WOOD_CREAKING);
                        entries.accept(SoundscapeItems.MUSIC_DISC_FAIRGROUND);
                        entries.accept(SoundscapeItems.MUSIC_DISC_TOWNSFOLK_CHATTER);
                        entries.accept(SoundscapeItems.MUSIC_DISC_SEWER);

                        entries.accept(SoundscapeItems.MUSIC_DISC_CAFE_AMBIENCE);
                        entries.accept(SoundscapeItems.MUSIC_DISC_JAPANESE_RESTAURANT);
                        entries.accept(SoundscapeItems.MUSIC_DISC_LARGE_ROOM_HUM);
                    })
                    .build()
    );

    public static void register() {
    }
}
