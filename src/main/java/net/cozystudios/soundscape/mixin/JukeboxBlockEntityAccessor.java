package net.cozystudios.soundscape.mixin;

import net.minecraft.block.entity.JukeboxBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(JukeboxBlockEntity.class)
public interface JukeboxBlockEntityAccessor {

    //? if >=1.20.3 && <1.21 {
    /*@Accessor("isPlaying")
    boolean soundscape$isPlaying();

    @Accessor("isPlaying")
    void soundscape$setIsPlaying(boolean playing);
    *///?}
}
