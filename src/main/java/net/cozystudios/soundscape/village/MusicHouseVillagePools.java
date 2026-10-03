package net.cozystudios.soundscape.village;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.cozystudios.soundscape.Soundscape;
import net.cozystudios.soundscape.SoundscapeId;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class MusicHouseVillagePools {

    private static final int WEIGHT = 30;

    private static final String[][] MODDED_POOLS = {
            {"taiga",   "repurposed_structures:villages/giant_taiga/houses"},
            {"taiga",   "kaisyn:village/old_growth_taiga_polish/houses"},
            {"taiga",   "kaisyn:village/snowy_taiga_viking/houses"},
            {"jungle",  "repurposed_structures:villages/jungle/houses"},
            {"jungle",  "kaisyn:village/jungle_tribal/houses"},
            {"jungle",  "kaisyn:village/sparse_jungle_polynesian/houses"},
    };

    private MusicHouseVillagePools() {
    }

    public static void registerCallback() {
        ServerLifecycleEvents.SERVER_STARTING.register(MusicHouseVillagePools::inject);
    }

    private static void inject(MinecraftServer server) {
        Registry<StructureTemplatePool> poolRegistry = server.registryAccess()
                .lookup(Registries.TEMPLATE_POOL)
                .orElse(null);
        if (poolRegistry == null) return;
        Holder<StructureProcessorList> emptyProcessors =
                Holder.direct(new StructureProcessorList(List.of()));
        int added = 0;
        for (String[] entry : MODDED_POOLS) {
            String biome = entry[0];
            Identifier poolId = Identifier.tryParse(entry[1]);
            if (poolId == null) continue;
            StructureTemplatePool pool = poolRegistry.getValue(poolId);
            if (pool == null) continue;
            Identifier structureId = SoundscapeId.of("village/" + biome + "/music_house");
            StructurePoolElement element = StructurePoolElement
                    .legacy(structureId.toString(), emptyProcessors)
                    .apply(StructureTemplatePool.Projection.RIGID);
            addElement(pool, element, WEIGHT);
            added++;
            Soundscape.LOGGER.info("[village] injected music_house into modded pool {}", poolId);
        }
        if (added > 0) {
            Soundscape.LOGGER.info("Injected music_house into {} modded village pool(s)", added);
        }
    }

    @SuppressWarnings("unchecked")
    private static void addElement(StructureTemplatePool pool, StructurePoolElement element, int weight) {
        try {
            Field templatesField = StructureTemplatePool.class.getDeclaredField("templates");
            templatesField.setAccessible(true);
            Object existing = templatesField.get(pool);
            ObjectArrayList<StructurePoolElement> elements;
            if (existing instanceof ObjectArrayList<?>) {
                elements = (ObjectArrayList<StructurePoolElement>) existing;
                try {
                    elements.add(element);
                    elements.remove(elements.size() - 1);
                } catch (UnsupportedOperationException ex) {
                    elements = new ObjectArrayList<>((Collection<StructurePoolElement>) existing);
                    templatesField.set(pool, elements);
                }
            } else {
                elements = new ObjectArrayList<>((Collection<StructurePoolElement>) existing);
                templatesField.set(pool, elements);
            }
            for (int i = 0; i < weight; i++) elements.add(element);

            Field rawField = StructureTemplatePool.class.getDeclaredField("rawTemplates");
            rawField.setAccessible(true);
            List<Pair<StructurePoolElement, Integer>> raw =
                    (List<Pair<StructurePoolElement, Integer>>) rawField.get(pool);
            if (!(raw instanceof ArrayList)) {
                raw = new ArrayList<>(raw);
                rawField.set(pool, raw);
            }
            raw.add(Pair.of(element, weight));
        } catch (ReflectiveOperationException e) {
            Soundscape.LOGGER.warn("Could not inject music house into pool", e);
        }
    }
}
