package net.cozystudios.soundscape.screen;

import net.cozystudios.soundscape.jukebox.ShuffleQueue;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.util.function.BooleanSupplier;

public class QueueSlot extends Slot {

    public QueueSlot(Inventory inventory, int index, int x, int y, BooleanSupplier shuffleModeChecker) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return ShuffleQueue.isDisc(stack);
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return true;
    }

    @Override
    public int getMaxItemCount() {
        return 1;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
