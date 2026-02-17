package net.cozystudios.soundscape.mixin.client;

import net.cozystudios.soundscape.sound.JukeboxSoundManager;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=1.21.4 {
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.item.ItemStack;
//?}

@Mixin(ClientWorld.class)
public class ClientWorldMixin {

    //? if >=1.21.4 {
    @Inject(method = "syncGlobalEvent", at = @At("HEAD"), cancellable = true)
    private void soundscape$interceptJukeboxSound(int eventId, BlockPos pos, int data, CallbackInfo ci) {
        if (eventId == 1010) {
            ci.cancel();

            if (data == 0) {
                if (JukeboxSoundManager.handleSongEnded(pos)) {
                    return;
                }
                JukeboxSoundManager.stopSound(pos);
                return;
            }

            ClientWorld self = (ClientWorld) (Object) this;
            BlockEntity be = self.getBlockEntity(pos);

            if (be == null) return;
            if (!(be instanceof JukeboxBlockEntity jukebox)) return;

            ItemStack disc = jukebox.getStack(0);
            if (disc.isEmpty()) return;

            var songOptional = JukeboxSong.getSongEntryFromStack(self.getRegistryManager(), disc);
            if (songOptional.isEmpty()) return;

            JukeboxSong song = songOptional.get().value();
            SoundEvent sound = song.soundEvent().value();
            int durationTicks = Math.round(song.lengthInSeconds() * 20);

            JukeboxSoundManager.playSound(pos, sound, jukebox, durationTicks);

        } else if (eventId == 1011) {
            ci.cancel();
            JukeboxSoundManager.stopSound(pos);
        }
    }
    //?}
}
