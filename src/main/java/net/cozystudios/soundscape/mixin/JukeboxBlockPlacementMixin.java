package net.cozystudios.soundscape.mixin;

import net.cozystudios.soundscape.Soundscape;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class JukeboxBlockPlacementMixin {

    @Inject(method = "getStateForPlacement", at = @At("HEAD"), cancellable = true)
    private void soundscape$jukeboxPlacementState(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> cir) {
        Block self = (Block) (Object) this;
        if (!(self instanceof JukeboxBlock)) return;
        BlockState state = self.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(Soundscape.JUKEBOX_PLAYING, false);
        cir.setReturnValue(state);
    }
}
