package net.cozystudios.soundscape.mixin.client;

import net.minecraft.client.sound.Channel;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(SoundSystem.class)
public interface SoundSystemAccessor {

    @Accessor("sources")
    Map<SoundInstance, Channel.SourceManager> soundscape$getSources();
}
