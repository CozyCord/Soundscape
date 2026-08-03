package net.cozystudios.soundscape.screen;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

import java.util.function.BooleanSupplier;

public class ShuffleModeSlot extends Slot {

    public ShuffleModeSlot(Container inventory, int index, int x, int y, BooleanSupplier shuffleModeChecker) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean isActive() {
        return true;
    }
}
