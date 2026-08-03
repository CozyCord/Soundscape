package net.cozystudios.soundscape.screen;

import net.cozystudios.soundscape.jukebox.ShuffleQueue;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.BooleanSupplier;

public class QueueSlot extends Slot {

    public QueueSlot(Container inventory, int index, int x, int y, BooleanSupplier shuffleModeChecker) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return ShuffleQueue.isDisc(stack);
    }

    @Override
    public boolean mayPickup(Player playerEntity) {
        return true;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean isActive() {
        return true;
    }
}
