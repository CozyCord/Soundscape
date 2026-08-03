package net.cozystudios.soundscape.mixin;

import net.cozystudios.soundscape.jukebox.JukeboxSettings;
import net.cozystudios.soundscape.jukebox.JukeboxSettingsProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.JukeboxSongPlayer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JukeboxSongPlayer.class)
public class JukeboxSongPlayerMixin {

    @Shadow @Final private BlockPos blockPos;

    @Inject(method = "tick(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/level/block/state/BlockState;)V",
            at = @At("HEAD"), cancellable = true)
    private void soundscape$freezeWhilePaused(LevelAccessor level, BlockState state, CallbackInfo ci) {
        BlockEntity be = level.getBlockEntity(this.blockPos);
        if (be instanceof JukeboxBlockEntity && be instanceof JukeboxSettingsProvider provider) {
            JukeboxSettings settings = provider.soundscape$getSettings();
            if (settings != null && settings.isPaused()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "spawnMusicParticles", at = @At("HEAD"), cancellable = true)
    private static void soundscape$blockParticlesIfDisabled(LevelAccessor world, BlockPos pos, CallbackInfo ci) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof JukeboxBlockEntity jukebox) {
            if (jukebox instanceof JukeboxSettingsProvider provider) {
                if (!provider.soundscape$getSettings().showParticles()) {
                    ci.cancel();
                }
            }
        }
    }

    @Redirect(method = "spawnMusicParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 soundscape$shiftParticleY(Vec3 self, double x, double y, double z) {
        return self.add(x, y + 1.0, z);
    }
}
