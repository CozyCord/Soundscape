package net.cozystudios.soundscape.registry;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class SoundscapeLootTables {

    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (key.equals(BuiltInLootTables.SIMPLE_DUNGEON)) {
                addShardPool(tableBuilder, 0.3f, 1, 2);
            }

            if (key.equals(BuiltInLootTables.ANCIENT_CITY)) {
                addShardPool(tableBuilder, 0.4f, 1, 3);
            }

            if (key.equals(BuiltInLootTables.ANCIENT_CITY_ICE_BOX)) {
                addShardPool(tableBuilder, 0.5f, 1, 2);
            }

            if (key.equals(BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_RARE)) {
                addShardPool(tableBuilder, 0.25f, 1, 1);
            }
        });
    }

    private static void addShardPool(LootTable.Builder tableBuilder, float chance, int min, int max) {
        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(SoundscapeItems.AMBIENCE_DISC_SHARD))
                .when(LootItemRandomChanceCondition.randomChance(chance))
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));

        tableBuilder.withPool(pool);
    }
}
