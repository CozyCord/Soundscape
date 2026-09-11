package net.cozystudios.soundscape.registry;

//? if <1.21 {
/*import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
*///?} else {
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
//?}
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;

public class SoundscapeLootTables {

    public static void register() {
        //? if >=1.21 {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (key.equals(LootTables.SIMPLE_DUNGEON_CHEST)) {
                addShardPool(tableBuilder, 0.3f, 1, 2);
            }

            if (key.equals(LootTables.ANCIENT_CITY_CHEST)) {
                addShardPool(tableBuilder, 0.4f, 1, 3);
            }

            if (key.equals(LootTables.ANCIENT_CITY_ICE_BOX_CHEST)) {
                addShardPool(tableBuilder, 0.5f, 1, 2);
            }

            if (key.equals(LootTables.TRAIL_RUINS_RARE_ARCHAEOLOGY)) {
                addShardPool(tableBuilder, 0.25f, 1, 1);
            }
        });
        //?} else {
        /*LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (id.equals(LootTables.SIMPLE_DUNGEON_CHEST)) {
                addShardPool(tableBuilder, 0.3f, 1, 2);
            }

            if (id.equals(LootTables.ANCIENT_CITY_CHEST)) {
                addShardPool(tableBuilder, 0.4f, 1, 3);
            }

            if (id.equals(LootTables.ANCIENT_CITY_ICE_BOX_CHEST)) {
                addShardPool(tableBuilder, 0.5f, 1, 2);
            }

            if (id.equals(LootTables.TRAIL_RUINS_RARE_ARCHAEOLOGY)) {
                addShardPool(tableBuilder, 0.25f, 1, 1);
            }
        });
        *///?}
    }

    private static void addShardPool(LootTable.Builder tableBuilder, float chance, int min, int max) {
        LootPool.Builder pool = LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .with(ItemEntry.builder(SoundscapeItems.AMBIENCE_DISC_SHARD))
                .conditionally(RandomChanceLootCondition.builder(chance))
                .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(min, max)));

        tableBuilder.pool(pool);
    }
}
