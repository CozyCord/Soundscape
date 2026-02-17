package net.cozystudios.soundscape.mixin.client;

import net.cozystudios.soundscape.sound.JukeboxSoundManager;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=1.21.4 {
/*import net.minecraft.client.world.WorldEventHandler;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.registry.entry.RegistryEntry;

@Mixin(WorldEventHandler.class)
public class WorldEventHandlerMixin {

    @Inject(method = "playJukeboxSong", at = @At("HEAD"), cancellable = true)
    private void soundscape$interceptPlayJukeboxSong(RegistryEntry<JukeboxSong> songEntry, BlockPos pos, CallbackInfo ci) {
        ci.cancel();

        if (songEntry == null) return;

        JukeboxSong song = songEntry.value();
        if (song == null) return;

        var world = MinecraftClient.getInstance().world;
        if (world == null) return;

        BlockEntity be = world.getBlockEntity(pos);
        if (be == null) return;
        if (!(be instanceof JukeboxBlockEntity jukebox)) return;

        SoundEvent sound = song.soundEvent().value();
        int durationTicks = Math.round(song.lengthInSeconds() * 20);

        JukeboxSoundManager.playSound(pos, sound, jukebox, durationTicks);
    }

    @Inject(method = "stopJukeboxSong", at = @At("HEAD"), cancellable = true)
    private void soundscape$interceptStopJukeboxSong(BlockPos pos, CallbackInfo ci) {
        ci.cancel();

        boolean discEjected = true;
        var world = MinecraftClient.getInstance().world;
        if (world != null && world.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
            discEjected = jukebox.getStack(0).isEmpty();
        }

        if (!discEjected && JukeboxSoundManager.handleSongEnded(pos)) {
            return;
        }

        JukeboxSoundManager.stopSound(pos);
    }
}
*///?} else {

@Mixin(MinecraftClient.class)
public class WorldEventHandlerMixin {
}
//?}
