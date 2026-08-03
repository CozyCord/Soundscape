package net.cozystudios.soundscape.screen;

import net.cozystudios.soundscape.jukebox.JukeboxSettings;
import net.cozystudios.soundscape.jukebox.JukeboxSettingsProvider;
import net.cozystudios.soundscape.jukebox.PlaybackMode;
import net.cozystudios.soundscape.jukebox.ShuffleQueue;
import net.cozystudios.soundscape.registry.SoundscapeScreenHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;

public class JukeboxScreenHandler extends AbstractContainerMenu {

    public static final int DISC_SLOT = 0;
    public static final int QUEUE_SLOT_START = 1;
    public static final int QUEUE_SLOT_COUNT = ShuffleQueue.QUEUE_SIZE;
    public static final int PLAYER_INV_START = QUEUE_SLOT_START + QUEUE_SLOT_COUNT;
    public static final int PLAYER_INV_COUNT = 27;
    public static final int HOTBAR_START = PLAYER_INV_START + PLAYER_INV_COUNT;
    public static final int HOTBAR_COUNT = 9;

    private final BlockPos pos;
    private final JukeboxBlockEntity jukebox;
    private final ContainerData propertyDelegate;
    private final Container queueInventory;
    private final Container discInventory;

    private Boolean clientShuffleModeOverride = null;

    public JukeboxScreenHandler(int syncId, Inventory playerInv, BlockPos pos, JukeboxBlockEntity jukebox) {
        super(SoundscapeScreenHandlers.JUKEBOX_SCREEN_HANDLER, syncId);
        this.pos = pos;
        this.jukebox = jukebox;
        this.propertyDelegate = new JukeboxPropertyDelegate(jukebox);
        this.addDataSlots(propertyDelegate);

        this.discInventory = jukebox;

        if (jukebox instanceof JukeboxSettingsProvider provider) {
            this.queueInventory = provider.soundscape$getShuffleQueue();
        } else {
            this.queueInventory = new SimpleContainer(ShuffleQueue.QUEUE_SIZE);
        }

        addDiscSlot();
        addQueueSlots();
        addPlayerInventorySlots(playerInv);
    }

    public JukeboxScreenHandler(int syncId, Inventory playerInv, BlockPos pos) {
        super(SoundscapeScreenHandlers.JUKEBOX_SCREEN_HANDLER, syncId);
        this.pos = pos;
        this.jukebox = null;
        this.propertyDelegate = new SimpleContainerData(7);
        this.addDataSlots(propertyDelegate);
        this.discInventory = new SimpleContainer(1);
        this.queueInventory = new SimpleContainer(ShuffleQueue.QUEUE_SIZE);

        addDiscSlot();
        addQueueSlots();
        addPlayerInventorySlots(playerInv);
    }

    private void addDiscSlot() {
        this.addSlot(new Slot(discInventory, 0, -999, -999) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
            @Override
            public boolean mayPickup(Player playerEntity) {
                return false;
            }
        });
    }

    private void addQueueSlots() {
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 8; col++) {
                int index = row * 8 + col;
                int x = 43 + col * 18;
                int y = 154 + row * 18;
                this.addSlot(new QueueSlot(queueInventory, index, x, y, this::isShuffleMode));
            }
        }
    }

    private void addPlayerInventorySlots(Inventory playerInv) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int x = 34 + col * 18;
                int y = 194 + row * 18;
                this.addSlot(new ShuffleModeSlot(playerInv, col + row * 9 + 9, x, y, this::isShuffleMode));
            }
        }

        for (int col = 0; col < 9; col++) {
            int x = 34 + col * 18;
            int y = 254;
            this.addSlot(new ShuffleModeSlot(playerInv, col, x, y, this::isShuffleMode));
        }
    }

    public Container getQueueInventory() {
        return queueInventory;
    }

    public BlockPos getPos() {
        return pos;
    }

    public int getVolume() {
        return propertyDelegate.get(0);
    }

    public int getPitch() {
        return propertyDelegate.get(1);
    }

    public int getRange() {
        return propertyDelegate.get(2);
    }

    public PlaybackMode getPlaybackMode() {
        int ordinal = propertyDelegate.get(3);
        PlaybackMode[] modes = PlaybackMode.values();
        return ordinal >= 0 && ordinal < modes.length ? modes[ordinal] : PlaybackMode.SINGLE;
    }

    public boolean isShuffleMode() {
        if (clientShuffleModeOverride != null) {
            return clientShuffleModeOverride;
        }
        return getPlaybackMode() == PlaybackMode.SHUFFLE;
    }

    public void setClientShuffleModeOverride(boolean shuffleMode) {
        this.clientShuffleModeOverride = shuffleMode;
    }

    public void clearClientShuffleModeOverride() {
        this.clientShuffleModeOverride = null;
    }

    public boolean isPlaying() {
        return propertyDelegate.get(4) == 1;
    }

    public int getProgress() {
        return propertyDelegate.get(5);
    }

    public boolean getShowParticles() {
        return propertyDelegate.get(6) == 1;
    }

    public ItemStack getDiscStack() {
        if (!slots.isEmpty()) {
            return slots.get(DISC_SLOT).getItem();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot != null && slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();

            if (slotIndex == DISC_SLOT) {
                return ItemStack.EMPTY;
            }

            if (slotIndex >= QUEUE_SLOT_START && slotIndex < QUEUE_SLOT_START + QUEUE_SLOT_COUNT) {
                if (!this.moveItemStackTo(originalStack, PLAYER_INV_START, HOTBAR_START + HOTBAR_COUNT, true)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (slotIndex >= PLAYER_INV_START) {
                if (ShuffleQueue.isDisc(originalStack)) {
                    if (!this.moveItemStackTo(originalStack, QUEUE_SLOT_START, QUEUE_SLOT_START + QUEUE_SLOT_COUNT, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (slotIndex < HOTBAR_START) {
                        if (!this.moveItemStackTo(originalStack, HOTBAR_START, HOTBAR_START + HOTBAR_COUNT, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        if (!this.moveItemStackTo(originalStack, PLAYER_INV_START, HOTBAR_START, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }

            if (originalStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return newStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    private static class JukeboxPropertyDelegate implements ContainerData {
        private final JukeboxBlockEntity jukebox;

        public JukeboxPropertyDelegate(JukeboxBlockEntity jukebox) {
            this.jukebox = jukebox;
        }

        @Override
        public int get(int index) {
            if (!(jukebox instanceof JukeboxSettingsProvider provider)) {
                return 0;
            }
            JukeboxSettings settings = provider.soundscape$getSettings();
            return switch (index) {
                case 0 -> settings.getVolume();
                case 1 -> settings.getPitch();
                case 2 -> settings.getRange();
                case 3 -> settings.getPlaybackMode().ordinal();
                case 4 -> settings.isPaused() ? 0 : 1;
                case 5 -> settings.getPlaybackProgress();
                case 6 -> settings.showParticles() ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (!(jukebox instanceof JukeboxSettingsProvider provider)) {
                return;
            }
            JukeboxSettings settings = provider.soundscape$getSettings();
            switch (index) {
                case 0 -> settings.setVolume(value);
                case 1 -> settings.setPitch(value);
                case 2 -> settings.setRange(value);
                case 3 -> {
                    PlaybackMode[] modes = PlaybackMode.values();
                    if (value >= 0 && value < modes.length) {
                        settings.setPlaybackMode(modes[value]);
                    }
                }
                case 4 -> settings.setPaused(value == 0);
                case 5 -> settings.setPlaybackProgress(value);
                case 6 -> settings.setShowParticles(value == 1);
            }
            jukebox.setChanged();
        }

        @Override
        public int getCount() {
            return 7;
        }
    }
}
