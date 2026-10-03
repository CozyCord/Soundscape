package net.cozystudios.soundscape.village;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.cozystudios.soundscape.Soundscape;
import net.cozystudios.soundscape.SoundscapeId;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;
import java.util.ArrayList;
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
        Registry<StructurePool> poolRegistry = server.getRegistryManager()
                .getOptional(RegistryKeys.TEMPLATE_POOL)
                .orElse(null);
        if (poolRegistry == null) return;
        RegistryEntry<StructureProcessorList> emptyProcessors =
                RegistryEntry.of(new StructureProcessorList(List.of()));
        for (String[] entry : MODDED_POOLS) {
            String biome = entry[0];
            Identifier poolId = parseId(entry[1]);
            if (poolId == null) continue;
            StructurePool pool = poolRegistry.get(poolId);
            if (pool == null) continue;
            Identifier structureId = SoundscapeId.of("village/" + biome + "/music_house");
            StructurePoolElement element = StructurePoolElement
                    .ofProcessedLegacySingle(structureId.toString(), emptyProcessors)
                    .apply(StructurePool.Projection.RIGID);
            addElement(pool, element, WEIGHT);
        }
    }

    @SuppressWarnings("unchecked")
    private static void addElement(StructurePool pool, StructurePoolElement element, int weight) {
        try {
            Field elementsField = elementsField();
            Object existing = elementsField.get(pool);
            ObjectArrayList<StructurePoolElement> elements;
            if (existing instanceof ObjectArrayList<?>) {
                elements = (ObjectArrayList<StructurePoolElement>) existing;
                try {
                    elements.add(element);
                    elements.remove(elements.size() - 1);
                } catch (UnsupportedOperationException ex) {
                    elements = new ObjectArrayList<>((java.util.Collection<StructurePoolElement>) existing);
                    elementsField.set(pool, elements);
                }
            } else {
                elements = new ObjectArrayList<>((java.util.Collection<StructurePoolElement>) existing);
                elementsField.set(pool, elements);
            }
            for (int i = 0; i < weight; i++) elements.add(element);

            Field countsField = elementCountsField();
            List<Pair<StructurePoolElement, Integer>> counts =
                    (List<Pair<StructurePoolElement, Integer>>) countsField.get(pool);
            if (!(counts instanceof ArrayList)) {
                counts = new ArrayList<>(counts);
                countsField.set(pool, counts);
            }
            counts.add(Pair.of(element, weight));
        } catch (ReflectiveOperationException e) {
            Soundscape.LOGGER.warn("Could not inject music house into pool", e);
        }
    }

    private static Field elementsFieldCache;
    private static Field elementCountsFieldCache;

    private static Field elementsField() throws NoSuchFieldException {
        if (elementsFieldCache != null) return elementsFieldCache;
        String mapped = FabricLoader.getInstance().getMappingResolver()
                .mapFieldName("intermediary", "net.minecraft.class_3785", "field_16680", "Lit/unimi/dsi/fastutil/objects/ObjectArrayList;");
        Field f;
        try { f = StructurePool.class.getDeclaredField(mapped); }
        catch (NoSuchFieldException ex) { f = StructurePool.class.getDeclaredField("field_16680"); }
        f.setAccessible(true);
        elementsFieldCache = f;
        return f;
    }

    private static Field elementCountsField() throws NoSuchFieldException {
        if (elementCountsFieldCache != null) return elementCountsFieldCache;
        String mapped = FabricLoader.getInstance().getMappingResolver()
                .mapFieldName("intermediary", "net.minecraft.class_3785", "field_16864", "Ljava/util/List;");
        Field f;
        try { f = StructurePool.class.getDeclaredField(mapped); }
        catch (NoSuchFieldException ex) { f = StructurePool.class.getDeclaredField("field_16864"); }
        f.setAccessible(true);
        elementCountsFieldCache = f;
        return f;
    }

    private static Identifier parseId(String s) {
        try {
            //? if >=1.20.5 {
            return Identifier.tryParse(s);
            //?} else {
            /*return new Identifier(s);
            *///?}
        } catch (Throwable t) {
            return null;
        }
    }
}
