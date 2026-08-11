package net.cozystudios.soundscape.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.mehvahdjukaar.amendments.client.ClientResourceGenerator", remap = false)
public class AmendmentsResourceGenMixin {

    @Inject(method = "generateJukeboxAssets", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void soundscape$skipJukeboxAssets(CallbackInfo ci) {
        ci.cancel();
    }
}
