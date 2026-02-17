package net.cozystudios.soundscape.screen;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;

import java.util.function.BooleanSupplier;

public class ShuffleModeSlot extends Slot {

    public ShuffleModeSlot(Inventory inventory, int index, int x, int y, BooleanSupplier shuffleModeChecker) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
