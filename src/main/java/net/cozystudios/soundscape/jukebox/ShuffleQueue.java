package net.cozystudios.soundscape.jukebox;

import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;

//? if >=1.21 {
import net.minecraft.component.DataComponentTypes;
//?} else {
/*import net.minecraft.item.MusicDiscItem;
*///?}

//? if >=1.21.6 {
/*import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
*///?} elif >=1.20.5 {
import net.minecraft.registry.RegistryWrapper;
//?}

import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;

public class ShuffleQueue extends SimpleInventory {

    public static final int QUEUE_SIZE = 16;
    private Runnable dirtyCallback;

    public ShuffleQueue() {
        super(QUEUE_SIZE);
    }

    public void setDirtyCallback(Runnable callback) {
        this.dirtyCallback = callback;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (dirtyCallback != null) {
            dirtyCallback.run();
        }
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return isDisc(stack);
    }

    @Override
    public int getMaxCountPerStack() {
        return 1;
    }

    public static boolean isDisc(ItemStack stack) {
        if (stack.isEmpty()) return false;
        //? if >=1.21 {
        return stack.contains(DataComponentTypes.JUKEBOX_PLAYABLE);
        //?} else {
        /*return stack.getItem() instanceof MusicDiscItem;
        *///?}
    }

    public boolean hasDiscs() {
        for (int i = 0; i < size(); i++) {
            if (!getStack(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public int getDiscCount() {
        int count = 0;
        for (int i = 0; i < size(); i++) {
            if (!getStack(i).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public List<Integer> getNonEmptySlots() {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            if (!getStack(i).isEmpty()) {
                slots.add(i);
            }
        }
        return slots;
    }

    public int getFirstDiscIndex() {
        for (int i = 0; i < size(); i++) {
            if (!getStack(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    public int getRandomDiscIndex(Random random) {
        List<Integer> nonEmpty = getNonEmptySlots();
        if (nonEmpty.isEmpty()) {
            return -1;
        }
        return nonEmpty.get(random.nextInt(nonEmpty.size()));
    }

    public int getRandomDiscIndexExcluding(Random random, int excludeIndex) {
        List<Integer> nonEmpty = getNonEmptySlots();
        nonEmpty.remove(Integer.valueOf(excludeIndex));
        if (nonEmpty.isEmpty()) {
            return -1;
        }
        return nonEmpty.get(random.nextInt(nonEmpty.size()));
    }

    private DefaultedList<ItemStack> toDefaultedList() {
        DefaultedList<ItemStack> list = DefaultedList.ofSize(QUEUE_SIZE, ItemStack.EMPTY);
        for (int i = 0; i < size(); i++) {
            list.set(i, getStack(i));
        }
        return list;
    }

    private void fromDefaultedList(DefaultedList<ItemStack> list) {
        for (int i = 0; i < Math.min(list.size(), QUEUE_SIZE); i++) {
            setStack(i, list.get(i));
        }
    }

    //? if >=1.21.6 {
    /*public void writeData(WriteView view) {
        DefaultedList<ItemStack> list = toDefaultedList();
        Inventories.writeData(view, list);
    }

    public void readData(ReadView view) {
        DefaultedList<ItemStack> list = DefaultedList.ofSize(QUEUE_SIZE, ItemStack.EMPTY);
        Inventories.readData(view, list);
        fromDefaultedList(list);
    }

    public static ShuffleQueue createFromView(ReadView view) {
        ShuffleQueue queue = new ShuffleQueue();
        queue.readData(view);
        return queue;
    }
    *///?} elif >=1.20.5 {
    public NbtCompound toNbt(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound nbt = new NbtCompound();
        DefaultedList<ItemStack> list = toDefaultedList();
        Inventories.writeNbt(nbt, list, registryLookup);
        return nbt;
    }

    public static ShuffleQueue fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        ShuffleQueue queue = new ShuffleQueue();
        DefaultedList<ItemStack> list = DefaultedList.ofSize(QUEUE_SIZE, ItemStack.EMPTY);
        Inventories.readNbt(nbt, list, registryLookup);
        queue.fromDefaultedList(list);
        return queue;
    }
    //?} else {
    /*public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        DefaultedList<ItemStack> list = toDefaultedList();
        Inventories.writeNbt(nbt, list);
        return nbt;
    }

    public static ShuffleQueue fromNbt(NbtCompound nbt) {
        ShuffleQueue queue = new ShuffleQueue();
        DefaultedList<ItemStack> list = DefaultedList.ofSize(QUEUE_SIZE, ItemStack.EMPTY);
        Inventories.readNbt(nbt, list);
        queue.fromDefaultedList(list);
        return queue;
    }
    *///?}
}
