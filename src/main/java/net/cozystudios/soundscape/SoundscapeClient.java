package net.cozystudios.soundscape;

import net.cozystudios.soundscape.boombox.client.BoomboxAudioManager;
import net.cozystudios.soundscape.network.SoundscapeClientNetworking;
import net.cozystudios.soundscape.registry.SoundscapeBlocks;
import net.cozystudios.soundscape.registry.SoundscapeScreenHandlers;
import net.cozystudios.soundscape.screen.JukeboxScreen;
import net.cozystudios.soundscape.sound.BoomboxSoundCategory;
import net.cozystudios.soundscape.sound.JukeboxSoundManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.gui.screens.MenuScreens;

import java.util.concurrent.atomic.AtomicInteger;

public class SoundscapeClient implements ClientModInitializer {

    private static final int RESUME_CHECK_DELAY_TICKS = 40;
    private static final AtomicInteger resumeCheckCountdown = new AtomicInteger(-1);

    @Override
    public void onInitializeClient() {
        BoomboxSoundCategory.register();
        net.cozystudios.soundscape.compat.AmendmentsCompat.tryDisableJukeboxAssets();
        SoundscapeClientNetworking.registerClient();
        MenuScreens.register(SoundscapeScreenHandlers.JUKEBOX_SCREEN_HANDLER, JukeboxScreen::new);


        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            JukeboxSoundManager.tick();

            if (client.player != null) {
                BoomboxAudioManager.getInstance().tick(client.player);
            }

            int countdown = resumeCheckCountdown.get();
            if (countdown > 0) {
                resumeCheckCountdown.decrementAndGet();
            } else if (countdown == 0) {
                resumeCheckCountdown.set(-1);
                JukeboxSoundManager.checkForResumableJukeboxes();
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            resumeCheckCountdown.set(RESUME_CHECK_DELAY_TICKS);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            JukeboxSoundManager.stopAll();
            BoomboxAudioManager.getInstance().stopAll();
            resumeCheckCountdown.set(-1);
        });
    }
}
