package net.cozystudios.soundscape.mixin;

import net.cozystudios.soundscape.jukebox.JukeboxSettingsProvider;
import net.cozystudios.soundscape.jukebox.ShuffleQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin {

    @Inject(method = "playerWillDestroy", at = @At("HEAD"))
    private void soundscape$dropJukeboxQueueItems(Level world, BlockPos pos, BlockState state, Player player, CallbackInfoReturnable<BlockState> cir) {
        if (!((Object)this instanceof JukeboxBlock)) {
            return;
        }

        if (world.isClientSide()) {
            return;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof JukeboxBlockEntity jukebox) {
            if (jukebox instanceof JukeboxSettingsProvider provider) {
                ShuffleQueue queue = provider.soundscape$getShuffleQueue();
                if (queue != null) {
                    for (int i = 0; i < queue.getContainerSize(); i++) {
                        ItemStack stack = queue.getItem(i);
                        if (!stack.isEmpty()) {
                            Block.popResource(world, pos, stack);
                            queue.setItem(i, ItemStack.EMPTY);
                        }
                    }
                }
            }
        }
    }
}
