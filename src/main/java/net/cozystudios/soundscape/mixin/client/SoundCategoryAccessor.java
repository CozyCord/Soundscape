package net.cozystudios.soundscape.mixin.client;

import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SoundCategory.class)
public interface SoundCategoryAccessor {

    @Invoker("<init>")
    static SoundCategory soundscape$init(String enumName, int ordinal, String name) {
        throw new AssertionError();
    }
}
