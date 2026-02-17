package net.cozystudios.soundscape.mixin.client;

import net.cozystudios.soundscape.jukebox.JukeboxSettingsProvider;
import net.cozystudios.soundscape.sound.JukeboxSoundManager;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=1.21 && <1.21.4 {
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.registry.entry.RegistryEntry;
//?} elif <1.21 {
/*import net.minecraft.item.Item;
import net.minecraft.item.MusicDiscItem;
*///?}

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Shadow
    private ClientWorld world;

    //? if >=1.21 && <1.21.4 {
    @Inject(method = "playJukeboxSong", at = @At("HEAD"), cancellable = true)
    private void soundscape$interceptPlayJukeboxSong(RegistryEntry<JukeboxSong> songEntry, BlockPos pos, CallbackInfo ci) {
        ci.cancel();

        if (songEntry == null) return;
        if (world == null) return;

        JukeboxSong song = songEntry.value();
        if (song == null) return;

        BlockEntity be = world.getBlockEntity(pos);
        if (be == null) return;
        if (!(be instanceof JukeboxBlockEntity jukebox)) return;

        if (jukebox instanceof JukeboxSettingsProvider provider) {
            if (provider.soundscape$getSettings().isPaused()) {
                return;
            }
        }

        SoundEvent sound = song.soundEvent().value();
        int durationTicks = Math.round(song.lengthInSeconds() * 20);

        JukeboxSoundManager.playSound(pos, sound, jukebox, durationTicks);
    }

    @Inject(method = "stopJukeboxSongAndUpdate", at = @At("HEAD"), cancellable = true)
    private void soundscape$interceptStopJukeboxSong(BlockPos pos, CallbackInfo ci) {
        ci.cancel();

        boolean discEjected = true;
        if (world != null && world.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
            discEjected = jukebox.getStack(0).isEmpty();
        }

        if (!discEjected && JukeboxSoundManager.handleSongEnded(pos)) {
            return;
        }

        JukeboxSoundManager.stopSound(pos);
    }
    //?} elif <1.21 {
    /*@Inject(method = "processWorldEvent", at = @At("HEAD"), cancellable = true)
    private void soundscape$interceptJukeboxSound(int eventId, BlockPos pos, int data, CallbackInfo ci) {
        if (eventId == 1010) {
            ci.cancel();

            if (data == 0) {
                boolean discEjected = true;
                if (world != null && world.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
                    discEjected = jukebox.getStack().isEmpty();
                }
                if (!discEjected && JukeboxSoundManager.handleSongEnded(pos)) {
                    return;
                }
                JukeboxSoundManager.stopSound(pos);
                return;
            }

            Item item = Item.byRawId(data);
            if (!(item instanceof MusicDiscItem musicDisc)) return;

            SoundEvent sound = musicDisc.getSound();
            if (sound == null) return;
            if (world == null) return;

            BlockEntity be = world.getBlockEntity(pos);
            if (!(be instanceof JukeboxBlockEntity jukebox)) return;

            if (jukebox instanceof JukeboxSettingsProvider provider) {
                if (provider.soundscape$getSettings().isPaused()) {
                    return;
                }
            }

            JukeboxSoundManager.playSound(pos, sound, jukebox);

        } else if (eventId == 1011) {
            ci.cancel();
            JukeboxSoundManager.stopSound(pos);
        }
    }
    *///?}
}
