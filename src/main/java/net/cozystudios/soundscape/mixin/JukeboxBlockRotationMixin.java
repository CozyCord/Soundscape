package net.cozystudios.soundscape.mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public class JukeboxBlockRotationMixin {

    @Inject(method = "rotate", at = @At("HEAD"), cancellable = true)
    private void soundscape$rotateJukebox(BlockState state, BlockRotation rotation, CallbackInfoReturnable<BlockState> cir) {
        if (state.getBlock() instanceof JukeboxBlock && state.contains(Properties.HORIZONTAL_FACING)) {
            cir.setReturnValue(state.with(Properties.HORIZONTAL_FACING, rotation.rotate(state.get(Properties.HORIZONTAL_FACING))));
        }
    }

    @Inject(method = "mirror", at = @At("HEAD"), cancellable = true)
    private void soundscape$mirrorJukebox(BlockState state, BlockMirror mirror, CallbackInfoReturnable<BlockState> cir) {
        if (state.getBlock() instanceof JukeboxBlock && state.contains(Properties.HORIZONTAL_FACING)) {
            cir.setReturnValue(state.rotate(mirror.getRotation(state.get(Properties.HORIZONTAL_FACING))));
        }
    }
}
