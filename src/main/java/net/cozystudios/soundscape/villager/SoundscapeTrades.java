package net.cozystudios.soundscape.villager;

import net.cozystudios.soundscape.registry.SoundscapeItems;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SoundscapeTrades {

    //? if >=1.20.5 {
    private static final TagKey<Item> MUSIC_DISCS_TAG =
            TagKey.of(RegistryKeys.ITEM, Identifier.of("minecraft", "music_discs"));
    //?} else {
    /*private static final TagKey<Item> MUSIC_DISCS_TAG =
            TagKey.of(RegistryKeys.ITEM, new Identifier("minecraft:music_discs"));
    *///?}

    private static final int DISC_PRICE = 8;
    private static final int DISC_MAX_USES = 4;
    private static final int DISC_XP_LVL2 = 10;
    private static final int DISC_XP_LVL3 = 20;
    private static final int DISC_XP_LVL4 = 30;
    private static final int DISC_XP_LVL5 = 30;
    private static final float DISC_PRICE_MULT = 0.05f;

    private static final int DISCS_PER_LEVEL = 2;

    private SoundscapeTrades() {
    }

    public static TradeOffer createShardsOffer(net.minecraft.util.math.random.Random random) {
        int count = 6 + random.nextInt(7);
        int price = Math.max(1, Math.round(count * 4f / 9f));
        return makeTradeOffer(Items.EMERALD, price,
                new ItemStack(SoundscapeItems.AMBIENCE_DISC_SHARD, count),
                12, 2, 0.05f);
    }

    public static void register() {
        registerShardsTrade();
        registerDiscLevel(2, DISC_XP_LVL2);
        registerDiscLevel(3, DISC_XP_LVL3);
        registerDiscLevel(4, DISC_XP_LVL4);
        registerDiscLevel(5, DISC_XP_LVL5);
    }

    private static void registerShardsTrade() {
        registerLevel(1, factories -> {
            //? if >=1.21.11 {
            /*factories.add((world, entity, random) -> {
            *///?} else {
            factories.add((entity, random) -> {
            //?}
                int count = 6 + random.nextInt(7);
                int price = Math.max(1, Math.round(count * 4f / 9f));
                return makeTradeOffer(Items.EMERALD, price,
                        new ItemStack(SoundscapeItems.AMBIENCE_DISC_SHARD, count),
                        12, 2, 0.05f);
            });
        });
    }

    private static void registerDiscLevel(int level, int xp) {
        registerLevel(level, factories -> {
            for (int i = 0; i < DISCS_PER_LEVEL; i++) {
                final int indexWithinLevel = i;
                //? if >=1.21.11 {
                /*factories.add((world, entity, random) -> {
                *///?} else {
                factories.add((entity, random) -> {
                //?}
                    List<Item> discs = resolveMusicDiscs();
                    if (discs.isEmpty()) return null;
                    int globalIndex = cumulativeDiscCountBeforeLevel(level) + indexWithinLevel;
                    Item chosen = pickStableUnique(discs, random.nextLong(), globalIndex);
                    if (chosen == null) return null;
                    return makeTradeOffer(Items.EMERALD, DISC_PRICE,
                            new ItemStack(chosen, 1),
                            DISC_MAX_USES, xp, DISC_PRICE_MULT);
                });
            }
        });
    }

    private static void registerLevel(int level, java.util.function.Consumer<java.util.List<net.minecraft.village.TradeOffers.Factory>> consumer) {
        //? if >=1.21.11 {
        /*TradeOfferHelper.registerVillagerOffers(SoundscapeVillagers.SOUND_ENGINEER_KEY, level, consumer);
        *///?} else {
        TradeOfferHelper.registerVillagerOffers(SoundscapeVillagers.SOUND_ENGINEER, level, consumer);
        //?}
    }

    private static TradeOffer makeTradeOffer(Item priceItem, int priceCount, ItemStack sell, int maxUses, int xp, float mult) {
        //? if >=1.21 {
        return new TradeOffer(
                new net.minecraft.village.TradedItem(priceItem, priceCount),
                sell, maxUses, xp, mult);
        //?} else {
        /*return new TradeOffer(
                new ItemStack(priceItem, priceCount),
                sell, maxUses, xp, mult);
        *///?}
    }

    private static int cumulativeDiscCountBeforeLevel(int level) {
        return (level - 2) * DISCS_PER_LEVEL;
    }

    private static List<Item> resolveMusicDiscs() {
        List<Item> out = new ArrayList<>();
        for (RegistryEntry<Item> entry : Registries.ITEM.iterateEntries(MUSIC_DISCS_TAG)) {
            Item item = entry.value();
            if (item != Items.AIR) out.add(item);
        }
        return out;
    }

    private static Item pickStableUnique(List<Item> items, long seed, int globalIndex) {
        int size = items.size();
        if (size == 0 || globalIndex < 0) return null;
        int wrapped = globalIndex % size;
        List<Integer> order = new ArrayList<>(size);
        for (int i = 0; i < size; i++) order.add(i);
        Collections.shuffle(order, new java.util.Random(seed));
        return items.get(order.get(wrapped));
    }
}
