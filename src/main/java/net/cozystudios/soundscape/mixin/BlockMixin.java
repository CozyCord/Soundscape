package net.cozystudios.soundscape.mixin;

import net.cozystudios.soundscape.jukebox.JukeboxSettingsProvider;
import net.cozystudios.soundscape.jukebox.ShuffleQueue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//? if >=1.21.5 {
/*@Mixin(Block.class)
public class BlockMixin {

    @Inject(method = "onBreak", at = @At("HEAD"))
    private void soundscape$dropJukeboxQueueItems(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfoReturnable<BlockState> cir) {
        if (!((Object)this instanceof JukeboxBlock)) {
            return;
        }

        if (world.isClient()) {
            return;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof JukeboxBlockEntity jukebox) {
            if (jukebox instanceof JukeboxSettingsProvider provider) {
                ShuffleQueue queue = provider.soundscape$getShuffleQueue();
                if (queue != null) {
                    for (int i = 0; i < queue.size(); i++) {
                        ItemStack stack = queue.getStack(i);
                        if (!stack.isEmpty()) {
                            Block.dropStack(world, pos, stack);
                            queue.setStack(i, ItemStack.EMPTY);
                        }
                    }
                }
            }
        }
    }
}
*///?} else {
import net.minecraft.client.MinecraftClient;

@Mixin(MinecraftClient.class)
public class BlockMixin {
}
//?}
