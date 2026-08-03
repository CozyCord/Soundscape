package net.cozystudios.soundscape.jukebox;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

public class ShuffleQueue extends SimpleContainer {

    public static final int QUEUE_SIZE = 16;
    private Runnable dirtyCallback;

    public ShuffleQueue() {
        super(QUEUE_SIZE);
    }

    public void setDirtyCallback(Runnable callback) {
        this.dirtyCallback = callback;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (dirtyCallback != null) {
            dirtyCallback.run();
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return isDisc(stack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    public static boolean isDisc(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.has(DataComponents.JUKEBOX_PLAYABLE);
    }

    public boolean hasDiscs() {
        for (int i = 0; i < getContainerSize(); i++) {
            if (!getItem(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public int getDiscCount() {
        int count = 0;
        for (int i = 0; i < getContainerSize(); i++) {
            if (!getItem(i).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public List<Integer> getNonEmptySlots() {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < getContainerSize(); i++) {
            if (!getItem(i).isEmpty()) {
                slots.add(i);
            }
        }
        return slots;
    }

    public int getFirstDiscIndex() {
        for (int i = 0; i < getContainerSize(); i++) {
            if (!getItem(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    public int getRandomDiscIndex(RandomSource random) {
        List<Integer> nonEmpty = getNonEmptySlots();
        if (nonEmpty.isEmpty()) {
            return -1;
        }
        return nonEmpty.get(random.nextInt(nonEmpty.size()));
    }

    public int getRandomDiscIndexExcluding(RandomSource random, int excludeIndex) {
        List<Integer> nonEmpty = getNonEmptySlots();
        nonEmpty.remove(Integer.valueOf(excludeIndex));
        if (nonEmpty.isEmpty()) {
            return -1;
        }
        return nonEmpty.get(random.nextInt(nonEmpty.size()));
    }

    private NonNullList<ItemStack> toNonNullList() {
        NonNullList<ItemStack> list = NonNullList.withSize(QUEUE_SIZE, ItemStack.EMPTY);
        for (int i = 0; i < getContainerSize(); i++) {
            list.set(i, getItem(i));
        }
        return list;
    }

    private void fromNonNullList(NonNullList<ItemStack> list) {
        for (int i = 0; i < Math.min(list.size(), QUEUE_SIZE); i++) {
            setItem(i, list.get(i));
        }
    }

    public void writeData(ValueOutput view) {
        NonNullList<ItemStack> list = toNonNullList();
        ContainerHelper.saveAllItems(view, list);
    }

    public void readData(ValueInput view) {
        NonNullList<ItemStack> list = NonNullList.withSize(QUEUE_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(view, list);
        fromNonNullList(list);
    }
}
