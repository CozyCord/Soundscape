package net.cozystudios.soundscape.mixin.client;

import net.cozystudios.soundscape.sound.JukeboxSoundManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelEventHandler.class)
public class LevelEventHandlerMixin {

    @Inject(method = "playJukeboxSong", at = @At("HEAD"), cancellable = true)
    private void soundscape$interceptPlayJukeboxSong(Holder<JukeboxSong> songEntry, BlockPos pos, CallbackInfo ci) {
        ci.cancel();

        if (songEntry == null) return;

        JukeboxSong song = songEntry.value();
        if (song == null) return;

        var level = Minecraft.getInstance().level;
        if (level == null) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof JukeboxBlockEntity jukebox)) return;

        SoundEvent sound = song.soundEvent().value();
        int durationTicks = Math.round(song.lengthInSeconds() * 20);

        JukeboxSoundManager.playSound(pos, sound, jukebox, durationTicks);
    }

    @Inject(method = "stopJukeboxSong", at = @At("HEAD"), cancellable = true)
    private void soundscape$interceptStopJukeboxSong(BlockPos pos, CallbackInfo ci) {
        ci.cancel();

        boolean discEjected = true;
        var level = Minecraft.getInstance().level;
        if (level != null && level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
            discEjected = jukebox.getTheItem().isEmpty();
        }

        if (!discEjected && JukeboxSoundManager.handleSongEnded(pos)) {
            return;
        }

        JukeboxSoundManager.stopSound(pos);
    }
}
