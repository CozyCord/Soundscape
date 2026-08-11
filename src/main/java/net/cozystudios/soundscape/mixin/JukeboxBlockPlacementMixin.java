package net.cozystudios.soundscape.mixin;

import net.cozystudios.soundscape.Soundscape;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.Properties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class JukeboxBlockPlacementMixin {

    @Inject(method = "getPlacementState", at = @At("HEAD"), cancellable = true)
    private void soundscape$jukeboxPlacementState(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> cir) {
        Block self = (Block) (Object) this;
        if (!(self instanceof JukeboxBlock)) return;
        BlockState state = self.getDefaultState()
                .with(Properties.HORIZONTAL_FACING, ctx.getHorizontalPlayerFacing().getOpposite())
                .with(Soundscape.JUKEBOX_PLAYING, false);
        cir.setReturnValue(state);
    }
}
