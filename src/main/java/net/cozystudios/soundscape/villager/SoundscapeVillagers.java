package net.cozystudios.soundscape.villager;

import com.google.common.collect.ImmutableSet;
import net.cozystudios.soundscape.Soundscape;
import net.cozystudios.soundscape.SoundscapeId;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvents;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;

public final class SoundscapeVillagers {

    public static final RegistryKey<PointOfInterestType> JUKEBOX_POI_KEY =
            RegistryKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, SoundscapeId.of("jukebox_poi"));

    public static final RegistryKey<VillagerProfession> SOUND_ENGINEER_KEY =
            RegistryKey.of(RegistryKeys.VILLAGER_PROFESSION, SoundscapeId.of("sound_engineer"));

    public static final PointOfInterestType JUKEBOX_POI =
            PointOfInterestHelper.register(SoundscapeId.of("jukebox_poi"), 1, 12, Blocks.JUKEBOX);

    //? if >=1.21.11 {
    /*public static final VillagerProfession SOUND_ENGINEER = Registry.register(
            Registries.VILLAGER_PROFESSION,
            SoundscapeId.of("sound_engineer"),
            new VillagerProfession(
                    net.minecraft.text.Text.translatable("entity.minecraft.villager.sound_engineer"),
                    entry -> entry.matchesKey(JUKEBOX_POI_KEY),
                    entry -> entry.matchesKey(JUKEBOX_POI_KEY),
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    SoundEvents.ENTITY_VILLAGER_WORK_LIBRARIAN
            )
    );
    *///?} else {
    public static final VillagerProfession SOUND_ENGINEER = Registry.register(
            Registries.VILLAGER_PROFESSION,
            SoundscapeId.of("sound_engineer"),
            new VillagerProfession(
                    Soundscape.MOD_ID + ":sound_engineer",
                    entry -> entry.matchesKey(JUKEBOX_POI_KEY),
                    entry -> entry.matchesKey(JUKEBOX_POI_KEY),
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    SoundEvents.ENTITY_VILLAGER_WORK_LIBRARIAN
            )
    );
    //?}

    private SoundscapeVillagers() {
    }

    public static void register() {
    }
}
