package net.cozystudios.soundscape.mixin;

import net.cozystudios.soundscape.Soundscape;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class JukeboxLuminanceMixin {

    @Inject(method = "getLightEmission", at = @At("RETURN"), cancellable = true)
    private void soundscape$jukeboxPlayingLuminance(CallbackInfoReturnable<Integer> cir) {
        BlockState self = (BlockState) (Object) this;
        if (self.getBlock() instanceof JukeboxBlock
                && self.hasProperty(Soundscape.JUKEBOX_PLAYING)
                && self.getValue(Soundscape.JUKEBOX_PLAYING)) {
            cir.setReturnValue(8);
        }
    }
}
