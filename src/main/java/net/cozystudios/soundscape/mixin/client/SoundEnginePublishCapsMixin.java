package net.cozystudios.soundscape.mixin.client;

import net.minecraft.client.sound.SoundEngine;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALCapabilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundEngine.class)
public class SoundEnginePublishCapsMixin {

    @Inject(method = "init", at = @At("TAIL"))
    private void soundscape$publishCapsProcessWide(CallbackInfo ci) {
        try {
            ALCapabilities caps = AL.getCapabilities();
            if (caps != null) {
                AL.setCurrentProcess(caps);
            }
        } catch (Throwable ignored) {
        }
    }
}
