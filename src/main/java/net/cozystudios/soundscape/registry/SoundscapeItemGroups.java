package net.cozystudios.soundscape.registry;

import net.cozystudios.soundscape.SoundscapeId;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

//? if >=1.21.2 {
/*import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
*///?}

public class SoundscapeItemGroups {

    public static final ItemGroup SOUNDSCAPE_GROUP = Registry.register(
            Registries.ITEM_GROUP,
            SoundscapeId.of("soundscape"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(SoundscapeItems.AMBIENCE_DISC))
                    .displayName(Text.translatable("itemGroup.soundscape.soundscape"))
                    .entries((context, entries) -> {
                        entries.add(SoundscapeItems.BOOMBOX);
                        entries.add(Items.JUKEBOX);
                        entries.add(SoundscapeItems.AMBIENCE_DISC_SHARD);
                        entries.add(SoundscapeItems.AMBIENCE_DISC);

                        entries.add(SoundscapeItems.MUSIC_DISC_FOREST_SUMMER_BIRDS);
                        entries.add(SoundscapeItems.MUSIC_DISC_FOREST_SWAMP_1);
                        entries.add(SoundscapeItems.MUSIC_DISC_FOREST_SWAMP_2);
                        entries.add(SoundscapeItems.MUSIC_DISC_FOREST_RAIN);
                        entries.add(SoundscapeItems.MUSIC_DISC_LEAVES_RUSTLING);
                        entries.add(SoundscapeItems.MUSIC_DISC_CRICKETS);
                        entries.add(SoundscapeItems.MUSIC_DISC_WINDCHIMES_1);
                        entries.add(SoundscapeItems.MUSIC_DISC_WINDCHIMES_2);

                        entries.add(SoundscapeItems.MUSIC_DISC_OCEAN_WAVES_1);
                        entries.add(SoundscapeItems.MUSIC_DISC_OCEAN_WAVES_2);
                        entries.add(SoundscapeItems.MUSIC_DISC_OCEAN_WAVES_3);
                        entries.add(SoundscapeItems.MUSIC_DISC_RIVER_STREAM);
                        entries.add(SoundscapeItems.MUSIC_DISC_RIVER_STREAM_BOULDERS);
                        entries.add(SoundscapeItems.MUSIC_DISC_RIVER_STREAM_FAST);
                        entries.add(SoundscapeItems.MUSIC_DISC_RAGING_RIVER);
                        entries.add(SoundscapeItems.MUSIC_DISC_WATERFALL_CRASHING_1);
                        entries.add(SoundscapeItems.MUSIC_DISC_WATERFALL_CRASHING_2);
                        entries.add(SoundscapeItems.MUSIC_DISC_WATERFALL_CRASHING_3);
                        entries.add(SoundscapeItems.MUSIC_DISC_WATERFALL_CRASHING_4);
                        entries.add(SoundscapeItems.MUSIC_DISC_WATER_BUBBLING);
                        entries.add(SoundscapeItems.MUSIC_DISC_WATER_BUBBLING_UNDERWATER);

                        entries.add(SoundscapeItems.MUSIC_DISC_THUNDERSTORM);
                        entries.add(SoundscapeItems.MUSIC_DISC_WIND_1);
                        entries.add(SoundscapeItems.MUSIC_DISC_WIND_2_STRONG);

                        entries.add(SoundscapeItems.MUSIC_DISC_FIRE_BURNING_1);
                        entries.add(SoundscapeItems.MUSIC_DISC_FIRE_BURNING_2);
                        entries.add(SoundscapeItems.MUSIC_DISC_FIRE_BURNING_3);
                        entries.add(SoundscapeItems.MUSIC_DISC_FIRE_BURNING_4);

                        entries.add(SoundscapeItems.MUSIC_DISC_HORROR_ABANDONED_CHURCH);
                        entries.add(SoundscapeItems.MUSIC_DISC_HORROR_CAVE_AMBIENCE);
                        entries.add(SoundscapeItems.MUSIC_DISC_HORROR_SCARY_STORM);
                        entries.add(SoundscapeItems.MUSIC_DISC_CAVE_AMBIENCE);

                        entries.add(SoundscapeItems.MUSIC_DISC_CITYSCAPE_AIRPORT);
                        entries.add(SoundscapeItems.MUSIC_DISC_CITYSCAPE_BROOKLYN_PLAYGROUND);
                        entries.add(SoundscapeItems.MUSIC_DISC_CITYSCAPE_NY_STREETS);
                        entries.add(SoundscapeItems.MUSIC_DISC_CITYSCAPE_NY_SUBWAY);
                        entries.add(SoundscapeItems.MUSIC_DISC_CITYSCAPE_PARTY_CLUB);
                        entries.add(SoundscapeItems.MUSIC_DISC_CITYSCAPE_SF_SUBWAY_BART);
                        entries.add(SoundscapeItems.MUSIC_DISC_CITYSCAPE_SKATEPARK);

                        entries.add(SoundscapeItems.MUSIC_DISC_FACTORY_BASEMENT);
                        entries.add(SoundscapeItems.MUSIC_DISC_FACTORY_ELECTRONIC_ROOM);
                        entries.add(SoundscapeItems.MUSIC_DISC_FACTORY_HUM);
                        entries.add(SoundscapeItems.MUSIC_DISC_FACTORY_MACHINE_ROOM);
                        entries.add(SoundscapeItems.MUSIC_DISC_COGS_TURNING);
                        entries.add(SoundscapeItems.MUSIC_DISC_ROCKS_GRINDING);

                        entries.add(SoundscapeItems.MUSIC_DISC_BLOCKS_BREAKING);
                        entries.add(SoundscapeItems.MUSIC_DISC_WOOD_CREAKING);
                        entries.add(SoundscapeItems.MUSIC_DISC_FAIRGROUND);
                        entries.add(SoundscapeItems.MUSIC_DISC_TOWNSFOLK_CHATTER);
                        entries.add(SoundscapeItems.MUSIC_DISC_SEWER);

                        entries.add(SoundscapeItems.MUSIC_DISC_CAFE_AMBIENCE);
                        entries.add(SoundscapeItems.MUSIC_DISC_JAPANESE_RESTAURANT);
                        entries.add(SoundscapeItems.MUSIC_DISC_LARGE_ROOM_HUM);
                    })
                    .build()
    );

    public static void register() {
    }
}
