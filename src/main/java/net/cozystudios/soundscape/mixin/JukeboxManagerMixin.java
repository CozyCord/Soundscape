package net.cozystudios.soundscape.mixin;

import net.cozystudios.soundscape.jukebox.JukeboxSettingsProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=1.21 {
import net.minecraft.block.jukebox.JukeboxManager;

@Mixin(JukeboxManager.class)
public class JukeboxManagerMixin {

    @Inject(method = "spawnNoteParticles", at = @At("HEAD"), cancellable = true)
    private static void soundscape$blockParticlesIfDisabled(WorldAccess world, BlockPos pos, CallbackInfo ci) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof JukeboxBlockEntity jukebox) {
            if (jukebox instanceof JukeboxSettingsProvider provider) {
                if (!provider.soundscape$getSettings().showParticles()) {
                    ci.cancel();
                }
            }
        }
    }

    @Redirect(method = "spawnNoteParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;add(DDD)Lnet/minecraft/util/math/Vec3d;"))
    private static Vec3d soundscape$shiftParticleY(Vec3d self, double x, double y, double z) {
        return self.add(x, y + 1.0, z);
    }
}
//?} else {
/*import net.minecraft.client.MinecraftClient;

@Mixin(MinecraftClient.class)
public class JukeboxManagerMixin {
}
*///?}
