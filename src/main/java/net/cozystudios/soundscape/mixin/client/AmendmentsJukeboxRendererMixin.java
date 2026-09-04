package net.cozystudios.soundscape.mixin.client;

import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.mehvahdjukaar.amendments.client.renderers.JukeboxTileRenderer", remap = false)
public class AmendmentsJukeboxRendererMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void soundscape$suppressDiscOnTop(JukeboxBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider provider, int light, int overlay, CallbackInfo ci) {
        ci.cancel();
    }
}
