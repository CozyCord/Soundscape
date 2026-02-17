package net.cozystudios.soundscape.mixin;

import net.cozystudios.soundscape.Soundscape;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.JukeboxBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class JukeboxLuminanceMixin {

    @Inject(method = "getLuminance", at = @At("RETURN"), cancellable = true)
    private void soundscape$jukeboxPlayingLuminance(CallbackInfoReturnable<Integer> cir) {
        BlockState self = (BlockState) (Object) this;
        if (self.getBlock() instanceof JukeboxBlock
                && self.contains(Soundscape.JUKEBOX_PLAYING)
                && self.get(Soundscape.JUKEBOX_PLAYING)) {
            cir.setReturnValue(8);
        }
    }
}
