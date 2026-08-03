package net.cozystudios.soundscape.boombox.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

@Environment(EnvType.CLIENT)
public class BoomboxScreenOpener {

    public static void open(BlockPos pos) {
        BoomboxScreen screen = new BoomboxScreen(pos);
        BoomboxAudioManager.CachedState cached = BoomboxAudioManager.getInstance().getCachedState(pos);
        if (cached != null) {
            screen.applyCachedState(cached);
        }
        Minecraft.getInstance().setScreen(screen);
    }
}
