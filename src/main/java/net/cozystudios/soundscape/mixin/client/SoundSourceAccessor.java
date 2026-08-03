package net.cozystudios.soundscape.mixin.client;

import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SoundSource.class)
public interface SoundSourceAccessor {

    @Invoker("<init>")
    static SoundSource soundscape$init(String enumName, int ordinal, String name) {
        throw new AssertionError();
    }
}
