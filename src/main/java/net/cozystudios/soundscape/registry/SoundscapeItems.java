package net.cozystudios.soundscape.registry;

import net.cozystudios.soundscape.SoundscapeId;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

//? if <1.21 {
/*import net.minecraft.sound.SoundEvent;
import net.minecraft.item.MusicDiscItem;
*///?}

//? if >=1.21.2 {
/*import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
*///?} elif >=1.21 {
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.JukeboxPlayableComponent;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryPair;
//?}

public class SoundscapeItems {

    private static final String NATURE = "soundscape.category.nature";
    private static final String WATER = "soundscape.category.water";
    private static final String WEATHER = "soundscape.category.weather";
    private static final String FIRE = "soundscape.category.fire";
    private static final String CITYSCAPE = "soundscape.category.cityscape";
    private static final String HORROR = "soundscape.category.horror";
    private static final String INDUSTRIAL = "soundscape.category.industrial";
    private static final String INTERIOR = "soundscape.category.interior";
    private static final String OUTDOOR = "soundscape.category.outdoor";

    public static final Item BOOMBOX = registerItem("boombox",
            new BlockItem(SoundscapeBlocks.BOOMBOX, createSettings("boombox")));

    public static final Item AMBIENCE_DISC_SHARD = registerItem("ambience_disc_shard",
            new Item(createSettings("ambience_disc_shard")));

    public static final Item AMBIENCE_DISC = registerItem("ambience_disc",
            new Item(createSettings("ambience_disc").maxCount(1)));

    public static final Item MUSIC_DISC_BLOCKS_BREAKING = registerDisc("music_disc_blocks_breaking", "blocks_breaking", 7, 23, OUTDOOR);
    public static final Item MUSIC_DISC_CAFE_AMBIENCE = registerDisc("music_disc_cafe_ambience", "cafe_ambience", 8, 158, INTERIOR);
    public static final Item MUSIC_DISC_CAVE_AMBIENCE = registerDisc("music_disc_cave_ambience", "cave_ambience", 1, 120, HORROR);
    public static final Item MUSIC_DISC_CITYSCAPE_AIRPORT = registerDisc("music_disc_cityscape_airport", "cityscape_airport", 2, 129, CITYSCAPE);
    public static final Item MUSIC_DISC_CITYSCAPE_BROOKLYN_PLAYGROUND = registerDisc("music_disc_cityscape_brooklyn_playground", "cityscape_brooklyn_playground", 3, 46, CITYSCAPE);
    public static final Item MUSIC_DISC_CITYSCAPE_LONDON_STREETS = registerDisc("music_disc_cityscape_london_streets", "cityscape_london_streets", 4, 90, CITYSCAPE);
    public static final Item MUSIC_DISC_CITYSCAPE_NY_STREETS = registerDisc("music_disc_cityscape_ny_streets", "cityscape_ny_streets", 5, 129, CITYSCAPE);
    public static final Item MUSIC_DISC_CITYSCAPE_NY_SUBWAY = registerDisc("music_disc_cityscape_ny_subway", "cityscape_ny_subway", 6, 69, CITYSCAPE);
    public static final Item MUSIC_DISC_CITYSCAPE_PARTY_CLUB = registerDisc("music_disc_cityscape_party_club", "cityscape_party_club", 7, 77, CITYSCAPE);
    public static final Item MUSIC_DISC_CITYSCAPE_SF_SUBWAY_BART = registerDisc("music_disc_cityscape_sf_subway_bart", "cityscape_sf_subway_bart", 8, 211, CITYSCAPE);
    public static final Item MUSIC_DISC_CITYSCAPE_SKATEPARK = registerDisc("music_disc_cityscape_skatepark", "cityscape_skatepark", 9, 54, CITYSCAPE);
    public static final Item MUSIC_DISC_COGS_TURNING = registerDisc("music_disc_cogs_turning", "cogs_turning", 9, 34, INDUSTRIAL);
    public static final Item MUSIC_DISC_CRICKETS = registerDisc("music_disc_crickets", "crickets", 1, 35, NATURE);
    public static final Item MUSIC_DISC_FACTORY_BASEMENT = registerDisc("music_disc_factory_basement", "factory_basement", 5, 168, INDUSTRIAL);
    public static final Item MUSIC_DISC_FACTORY_ELECTRONIC_ROOM = registerDisc("music_disc_factory_electronic_room", "factory_electronic_room", 3, 57, INDUSTRIAL);
    public static final Item MUSIC_DISC_FACTORY_HUM = registerDisc("music_disc_factory_hum", "factory_hum", 4, 90, INDUSTRIAL);
    public static final Item MUSIC_DISC_FACTORY_MACHINE_ROOM = registerDisc("music_disc_factory_machine_room", "factory_machine_room", 2, 61, INDUSTRIAL);
    public static final Item MUSIC_DISC_FAIRGROUND = registerDisc("music_disc_fairground", "fairground", 7, 87, OUTDOOR);
    public static final Item MUSIC_DISC_FIRE_BURNING_1 = registerDisc("music_disc_fire_burning_1", "fire_burning_1", 10, 82, FIRE);
    public static final Item MUSIC_DISC_FIRE_BURNING_2 = registerDisc("music_disc_fire_burning_2", "fire_burning_2", 11, 25, FIRE);
    public static final Item MUSIC_DISC_FIRE_BURNING_3 = registerDisc("music_disc_fire_burning_3", "fire_burning_3", 12, 20, FIRE);
    public static final Item MUSIC_DISC_FIRE_BURNING_4 = registerDisc("music_disc_fire_burning_4", "fire_burning_4", 13, 32, FIRE);
    public static final Item MUSIC_DISC_FOREST_RAIN = registerDisc("music_disc_forest_rain", "forest_rain", 14, 110, NATURE);
    public static final Item MUSIC_DISC_FOREST_SUMMER_BIRDS = registerDisc("music_disc_forest_summer_birds", "forest_summer_birds", 14, 88, NATURE);
    public static final Item MUSIC_DISC_FOREST_SWAMP_1 = registerDisc("music_disc_forest_swamp_1", "forest_swamp_1", 11, 60, NATURE);
    public static final Item MUSIC_DISC_FOREST_SWAMP_2 = registerDisc("music_disc_forest_swamp_2", "forest_swamp_2", 15, 157, NATURE);
    public static final Item MUSIC_DISC_HORROR_ABANDONED_CHURCH = registerDisc("music_disc_horror_abandoned_church", "horror_abandoned_church", 12, 219, HORROR);
    public static final Item MUSIC_DISC_HORROR_CAVE_AMBIENCE = registerDisc("music_disc_horror_cave_ambience", "horror_cave_ambience", 10, 120, HORROR);
    public static final Item MUSIC_DISC_HORROR_SCARY_STORM = registerDisc("music_disc_horror_scary_storm", "horror_scary_storm", 13, 18, HORROR);
    public static final Item MUSIC_DISC_JAPANESE_RESTAURANT = registerDisc("music_disc_japanese_restaurant", "japanese_restaurant", 8, 130, INTERIOR);
    public static final Item MUSIC_DISC_LARGE_ROOM_HUM = registerDisc("music_disc_large_room_hum", "large_room_hum", 15, 107, INTERIOR);
    public static final Item MUSIC_DISC_LEAVES_RUSTLING = registerDisc("music_disc_leaves_rustling", "leaves_rustling", 1, 109, NATURE);
    public static final Item MUSIC_DISC_OCEAN_WAVES_1 = registerDisc("music_disc_ocean_waves_1", "ocean_waves_1", 2, 120, WATER);
    public static final Item MUSIC_DISC_OCEAN_WAVES_2 = registerDisc("music_disc_ocean_waves_2", "ocean_waves_2", 3, 174, WATER);
    public static final Item MUSIC_DISC_OCEAN_WAVES_3 = registerDisc("music_disc_ocean_waves_3", "ocean_waves_3", 4, 23, WATER);
    public static final Item MUSIC_DISC_RAGING_RIVER = registerDisc("music_disc_raging_river", "raging_river", 2, 117, WATER);
    public static final Item MUSIC_DISC_RIVER_STREAM = registerDisc("music_disc_river_stream", "river_stream", 3, 90, WATER);
    public static final Item MUSIC_DISC_RIVER_STREAM_BOULDERS = registerDisc("music_disc_river_stream_boulders", "river_stream_boulders", 11, 137, WATER);
    public static final Item MUSIC_DISC_RIVER_STREAM_FAST = registerDisc("music_disc_river_stream_fast", "river_stream_fast", 10, 180, WATER);
    public static final Item MUSIC_DISC_ROCKS_GRINDING = registerDisc("music_disc_rocks_grinding", "rocks_grinding", 4, 56, INDUSTRIAL);
    public static final Item MUSIC_DISC_SEWER = registerDisc("music_disc_sewer", "sewer", 9, 34, OUTDOOR);
    public static final Item MUSIC_DISC_THUNDERSTORM = registerDisc("music_disc_thunderstorm", "thunderstorm", 14, 142, WEATHER);
    public static final Item MUSIC_DISC_TOWNSFOLK_CHATTER = registerDisc("music_disc_townsfolk_chatter", "townsfolk_chatter", 6, 117, OUTDOOR);
    public static final Item MUSIC_DISC_WATER_BUBBLING = registerDisc("music_disc_water_bubbling", "water_bubbling", 5, 30, WATER);
    public static final Item MUSIC_DISC_WATER_BUBBLING_UNDERWATER = registerDisc("music_disc_water_bubbling_underwater", "water_bubbling_underwater", 6, 90, WATER);
    public static final Item MUSIC_DISC_WATERFALL_CRASHING_1 = registerDisc("music_disc_waterfall_crashing_1", "waterfall_crashing_1", 7, 101, WATER);
    public static final Item MUSIC_DISC_WATERFALL_CRASHING_2 = registerDisc("music_disc_waterfall_crashing_2", "waterfall_crashing_2", 8, 120, WATER);
    public static final Item MUSIC_DISC_WATERFALL_CRASHING_3 = registerDisc("music_disc_waterfall_crashing_3", "waterfall_crashing_3", 12, 128, WATER);
    public static final Item MUSIC_DISC_WATERFALL_CRASHING_4 = registerDisc("music_disc_waterfall_crashing_4", "waterfall_crashing_4", 13, 80, WATER);
    public static final Item MUSIC_DISC_WIND_1 = registerDisc("music_disc_wind_1", "wind_1", 9, 63, WEATHER);
    public static final Item MUSIC_DISC_WIND_2_STRONG = registerDisc("music_disc_wind_2_strong", "wind_2_strong", 10, 45, WEATHER);
    public static final Item MUSIC_DISC_WINDCHIMES_1 = registerDisc("music_disc_windchimes_1", "windchimes_1", 5, 101, NATURE);
    public static final Item MUSIC_DISC_WINDCHIMES_2 = registerDisc("music_disc_windchimes_2", "windchimes_2", 6, 174, NATURE);
    public static final Item MUSIC_DISC_WOOD_CREAKING = registerDisc("music_disc_wood_creaking", "wood_creaking", 1, 81, OUTDOOR);

    //? if >=1.21.2 {
    /*private static Item.Settings createSettings(String name) {
        return new Item.Settings()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, SoundscapeId.of(name)));
    }

    private static Item.Settings createDiscSettings(String name, String songName) {
        return new Item.Settings()
                .maxCount(1)
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, SoundscapeId.of(name)))
                .jukeboxPlayable(RegistryKey.of(RegistryKeys.JUKEBOX_SONG, SoundscapeId.of(songName)));
    }
    *///?} elif >=1.21 {
    private static Item.Settings createSettings(String name) {
        return new Item.Settings();
    }

    private static Item.Settings createDiscSettings(String name, String songName) {
        RegistryKey<JukeboxSong> songKey = RegistryKey.of(RegistryKeys.JUKEBOX_SONG, SoundscapeId.of(songName));
        JukeboxPlayableComponent component = new JukeboxPlayableComponent(new RegistryPair<>(songKey), true);
        return new Item.Settings()
                .maxCount(1)
                .component(DataComponentTypes.JUKEBOX_PLAYABLE, component);
    }
    //?} else {
    /*private static Item.Settings createSettings(String name) {
        return new Item.Settings();
    }

    private static Item.Settings createDiscSettings(String name, String songName) {
        return new Item.Settings().maxCount(1);
    }
    *///?}

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, SoundscapeId.of(name), item);
    }

    private static Item registerDisc(String name, String songName, int comparatorOutput, int lengthInSeconds, String category) {
        //? if >=1.21 {
        return registerItem(name, new AmbienceDiscItem(category, createDiscSettings(name, songName)));
        //?} else {
        /*SoundEvent soundEvent = SoundscapeSounds.getSoundForDisc(songName);
        if (soundEvent == null) {
            soundEvent = SoundEvent.of(SoundscapeId.of("ambience." + songName));
        }
        AmbienceDiscItem disc = new AmbienceDiscItem(category, comparatorOutput, soundEvent, createDiscSettings(name, songName), lengthInSeconds);
        return registerItem(name, disc);
        *///?}
    }

    public static void register() {
    }
}
