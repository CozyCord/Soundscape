package net.cozystudios.soundscape.villager;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.cozystudios.soundscape.SoundscapeId;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PoiHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.level.block.Blocks;

public final class SoundscapeVillagers {

    public static final ResourceKey<PoiType> JUKEBOX_POI_KEY =
            ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, SoundscapeId.of("jukebox_poi"));

    public static final ResourceKey<VillagerProfession> SOUND_ENGINEER_KEY =
            ResourceKey.create(Registries.VILLAGER_PROFESSION, SoundscapeId.of("sound_engineer"));

    public static final PoiType JUKEBOX_POI =
            PoiHelper.register(SoundscapeId.of("jukebox_poi"), 1, 12, Blocks.JUKEBOX);

    public static final VillagerProfession SOUND_ENGINEER = Registry.register(
            BuiltInRegistries.VILLAGER_PROFESSION,
            SoundscapeId.of("sound_engineer"),
            new VillagerProfession(
                    Component.translatable("entity.minecraft.villager.soundscape.sound_engineer"),
                    entry -> entry.is(JUKEBOX_POI_KEY),
                    entry -> entry.is(JUKEBOX_POI_KEY),
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    SoundEvents.VILLAGER_WORK_LIBRARIAN,
                    buildTradeSets()
            )
    );

    private static Int2ObjectMap<ResourceKey<TradeSet>> buildTradeSets() {
        Int2ObjectOpenHashMap<ResourceKey<TradeSet>> map = new Int2ObjectOpenHashMap<>();
        for (int level = 1; level <= 5; level++) {
            map.put(level, ResourceKey.create(Registries.TRADE_SET,
                    SoundscapeId.of("sound_engineer/level_" + level)));
        }
        return map;
    }

    private SoundscapeVillagers() {
    }

    public static void register() {
    }
}
