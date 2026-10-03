package net.cozystudios.soundscape.mixin;

import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public class JukeboxBlockRotationMixin {

    @Inject(method = "rotate", at = @At("HEAD"), cancellable = true)
    private void soundscape$rotateJukebox(BlockState state, Rotation rotation, CallbackInfoReturnable<BlockState> cir) {
        if (state.getBlock() instanceof JukeboxBlock && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            cir.setReturnValue(state.setValue(BlockStateProperties.HORIZONTAL_FACING, rotation.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING))));
        }
    }

    @Inject(method = "mirror", at = @At("HEAD"), cancellable = true)
    private void soundscape$mirrorJukebox(BlockState state, Mirror mirror, CallbackInfoReturnable<BlockState> cir) {
        if (state.getBlock() instanceof JukeboxBlock && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            cir.setReturnValue(state.rotate(mirror.getRotation(state.getValue(BlockStateProperties.HORIZONTAL_FACING))));
        }
    }
}
