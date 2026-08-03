package net.cozystudios.soundscape.registry;

import net.cozystudios.soundscape.SoundscapeId;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public class SoundscapeSounds {

    public static final SoundEvent AMBIENCE_CAVE = register("ambience.cave");
    public static final SoundEvent AMBIENCE_FOREST_RAIN = register("ambience.forest_rain");
    public static final SoundEvent AMBIENCE_LEAVES_RUSTLING = register("ambience.leaves_rustling");

    public static final SoundEvent AMBIENCE_RAGING_RIVER = register("ambience.raging_river");
    public static final SoundEvent AMBIENCE_RIVER_STREAM = register("ambience.river_stream");
    public static final SoundEvent AMBIENCE_WATER_BUBBLING = register("ambience.water_bubbling");
    public static final SoundEvent AMBIENCE_WATER_BUBBLING_UNDERWATER = register("ambience.water_bubbling_underwater");
    public static final SoundEvent AMBIENCE_WATERFALL_CRASHING_1 = register("ambience.waterfall_crashing_1");
    public static final SoundEvent AMBIENCE_WATERFALL_CRASHING_2 = register("ambience.waterfall_crashing_2");

    public static final SoundEvent AMBIENCE_WIND_1 = register("ambience.wind_1");
    public static final SoundEvent AMBIENCE_WIND_2_STRONG = register("ambience.wind_2_strong");

    public static final SoundEvent AMBIENCE_FIRE_BURNING_1 = register("ambience.fire_burning_1");
    public static final SoundEvent AMBIENCE_FIRE_BURNING_2 = register("ambience.fire_burning_2");
    public static final SoundEvent AMBIENCE_FIRE_BURNING_3 = register("ambience.fire_burning_3");
    public static final SoundEvent AMBIENCE_FIRE_BURNING_4 = register("ambience.fire_burning_4");

    public static final SoundEvent AMBIENCE_COGS_TURNING = register("ambience.cogs_turning");
    public static final SoundEvent AMBIENCE_LARGE_ROOM_HUM = register("ambience.large_room_hum");
    public static final SoundEvent AMBIENCE_ROCKS_GRINDING = register("ambience.rocks_grinding");

    public static final SoundEvent AMBIENCE_CITYSCAPE_AIRPORT = register("ambience.cityscape_airport");
    public static final SoundEvent AMBIENCE_CITYSCAPE_BROOKLYN_PLAYGROUND = register("ambience.cityscape_brooklyn_playground");
    public static final SoundEvent AMBIENCE_CITYSCAPE_NY_STREETS = register("ambience.cityscape_ny_streets");
    public static final SoundEvent AMBIENCE_CITYSCAPE_NY_SUBWAY = register("ambience.cityscape_ny_subway");
    public static final SoundEvent AMBIENCE_CITYSCAPE_PARTY_CLUB = register("ambience.cityscape_party_club");
    public static final SoundEvent AMBIENCE_CITYSCAPE_SF_SUBWAY_BART = register("ambience.cityscape_sf_subway_bart");
    public static final SoundEvent AMBIENCE_CITYSCAPE_SKATEPARK = register("ambience.cityscape_skatepark");

    public static final SoundEvent AMBIENCE_CRICKETS = register("ambience.crickets");

    public static final SoundEvent AMBIENCE_OCEAN_WAVES_1 = register("ambience.ocean_waves_1");
    public static final SoundEvent AMBIENCE_OCEAN_WAVES_2 = register("ambience.ocean_waves_2");
    public static final SoundEvent AMBIENCE_OCEAN_WAVES_3 = register("ambience.ocean_waves_3");

    public static final SoundEvent AMBIENCE_WINDCHIMES_1 = register("ambience.windchimes_1");
    public static final SoundEvent AMBIENCE_WINDCHIMES_2 = register("ambience.windchimes_2");

    public static final SoundEvent AMBIENCE_BLOCKS_BREAKING = register("ambience.blocks_breaking");
    public static final SoundEvent AMBIENCE_JAPANESE_RESTAURANT = register("ambience.japanese_restaurant");
    public static final SoundEvent AMBIENCE_SEWER = register("ambience.sewer");

    public static final SoundEvent AMBIENCE_HORROR_CAVE_AMBIENCE = register("ambience.horror_cave_ambience");
    public static final SoundEvent AMBIENCE_HORROR_ABANDONED_CHURCH = register("ambience.horror_abandoned_church");
    public static final SoundEvent AMBIENCE_HORROR_SCARY_STORM = register("ambience.horror_scary_storm");

    public static final SoundEvent AMBIENCE_FOREST_SWAMP_1 = register("ambience.forest_swamp_1");
    public static final SoundEvent AMBIENCE_FOREST_SWAMP_2 = register("ambience.forest_swamp_2");
    public static final SoundEvent AMBIENCE_FOREST_SUMMER_BIRDS = register("ambience.forest_summer_birds");

    public static final SoundEvent AMBIENCE_THUNDERSTORM = register("ambience.thunderstorm");
    public static final SoundEvent AMBIENCE_WOOD_CREAKING = register("ambience.wood_creaking");

    public static final SoundEvent AMBIENCE_FACTORY_MACHINE_ROOM = register("ambience.factory_machine_room");
    public static final SoundEvent AMBIENCE_FACTORY_ELECTRONIC_ROOM = register("ambience.factory_electronic_room");
    public static final SoundEvent AMBIENCE_FACTORY_HUM = register("ambience.factory_hum");
    public static final SoundEvent AMBIENCE_FACTORY_BASEMENT = register("ambience.factory_basement");

    public static final SoundEvent AMBIENCE_TOWNSFOLK_CHATTER = register("ambience.townsfolk_chatter");
    public static final SoundEvent AMBIENCE_FAIRGROUND = register("ambience.fairground");
    public static final SoundEvent AMBIENCE_CAFE_AMBIENCE = register("ambience.cafe_ambience");

    public static final SoundEvent AMBIENCE_RIVER_STREAM_FAST = register("ambience.river_stream_fast");
    public static final SoundEvent AMBIENCE_RIVER_STREAM_BOULDERS = register("ambience.river_stream_boulders");
    public static final SoundEvent AMBIENCE_WATERFALL_CRASHING_3 = register("ambience.waterfall_crashing_3");
    public static final SoundEvent AMBIENCE_WATERFALL_CRASHING_4 = register("ambience.waterfall_crashing_4");

    private static SoundEvent register(String name) {
        var id = SoundscapeId.of(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static SoundEvent getSoundForDisc(String songName) {
        return switch (songName) {
            case "cave_ambience" -> AMBIENCE_CAVE;
            case "forest_rain" -> AMBIENCE_FOREST_RAIN;
            case "leaves_rustling" -> AMBIENCE_LEAVES_RUSTLING;
            case "raging_river" -> AMBIENCE_RAGING_RIVER;
            case "river_stream" -> AMBIENCE_RIVER_STREAM;
            case "water_bubbling" -> AMBIENCE_WATER_BUBBLING;
            case "water_bubbling_underwater" -> AMBIENCE_WATER_BUBBLING_UNDERWATER;
            case "waterfall_crashing_1" -> AMBIENCE_WATERFALL_CRASHING_1;
            case "waterfall_crashing_2" -> AMBIENCE_WATERFALL_CRASHING_2;
            case "wind_1" -> AMBIENCE_WIND_1;
            case "wind_2_strong" -> AMBIENCE_WIND_2_STRONG;
            case "fire_burning_1" -> AMBIENCE_FIRE_BURNING_1;
            case "fire_burning_2" -> AMBIENCE_FIRE_BURNING_2;
            case "fire_burning_3" -> AMBIENCE_FIRE_BURNING_3;
            case "fire_burning_4" -> AMBIENCE_FIRE_BURNING_4;
            case "cogs_turning" -> AMBIENCE_COGS_TURNING;
            case "large_room_hum" -> AMBIENCE_LARGE_ROOM_HUM;
            case "rocks_grinding" -> AMBIENCE_ROCKS_GRINDING;
            case "cityscape_airport" -> AMBIENCE_CITYSCAPE_AIRPORT;
            case "cityscape_brooklyn_playground" -> AMBIENCE_CITYSCAPE_BROOKLYN_PLAYGROUND;
            case "cityscape_ny_streets" -> AMBIENCE_CITYSCAPE_NY_STREETS;
            case "cityscape_ny_subway" -> AMBIENCE_CITYSCAPE_NY_SUBWAY;
            case "cityscape_party_club" -> AMBIENCE_CITYSCAPE_PARTY_CLUB;
            case "cityscape_sf_subway_bart" -> AMBIENCE_CITYSCAPE_SF_SUBWAY_BART;
            case "cityscape_skatepark" -> AMBIENCE_CITYSCAPE_SKATEPARK;
            case "crickets" -> AMBIENCE_CRICKETS;
            case "ocean_waves_1" -> AMBIENCE_OCEAN_WAVES_1;
            case "ocean_waves_2" -> AMBIENCE_OCEAN_WAVES_2;
            case "ocean_waves_3" -> AMBIENCE_OCEAN_WAVES_3;
            case "windchimes_1" -> AMBIENCE_WINDCHIMES_1;
            case "windchimes_2" -> AMBIENCE_WINDCHIMES_2;
            case "blocks_breaking" -> AMBIENCE_BLOCKS_BREAKING;
            case "japanese_restaurant" -> AMBIENCE_JAPANESE_RESTAURANT;
            case "sewer" -> AMBIENCE_SEWER;
            case "horror_cave_ambience" -> AMBIENCE_HORROR_CAVE_AMBIENCE;
            case "horror_abandoned_church" -> AMBIENCE_HORROR_ABANDONED_CHURCH;
            case "horror_scary_storm" -> AMBIENCE_HORROR_SCARY_STORM;
            case "forest_swamp_1" -> AMBIENCE_FOREST_SWAMP_1;
            case "forest_swamp_2" -> AMBIENCE_FOREST_SWAMP_2;
            case "forest_summer_birds" -> AMBIENCE_FOREST_SUMMER_BIRDS;
            case "thunderstorm" -> AMBIENCE_THUNDERSTORM;
            case "wood_creaking" -> AMBIENCE_WOOD_CREAKING;
            case "factory_machine_room" -> AMBIENCE_FACTORY_MACHINE_ROOM;
            case "factory_electronic_room" -> AMBIENCE_FACTORY_ELECTRONIC_ROOM;
            case "factory_hum" -> AMBIENCE_FACTORY_HUM;
            case "factory_basement" -> AMBIENCE_FACTORY_BASEMENT;
            case "townsfolk_chatter" -> AMBIENCE_TOWNSFOLK_CHATTER;
            case "fairground" -> AMBIENCE_FAIRGROUND;
            case "cafe_ambience" -> AMBIENCE_CAFE_AMBIENCE;
            case "river_stream_fast" -> AMBIENCE_RIVER_STREAM_FAST;
            case "river_stream_boulders" -> AMBIENCE_RIVER_STREAM_BOULDERS;
            case "waterfall_crashing_3" -> AMBIENCE_WATERFALL_CRASHING_3;
            case "waterfall_crashing_4" -> AMBIENCE_WATERFALL_CRASHING_4;
            default -> null;
        };
    }

    public static void register() {
    }
}
