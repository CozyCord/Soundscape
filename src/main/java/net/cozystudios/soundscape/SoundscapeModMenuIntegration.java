package net.cozystudios.soundscape;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import net.cozystudios.soundscape.config.ModConfig;
import net.minecraft.client.gui.screen.Screen;

public class SoundscapeModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (Screen parent) -> AutoConfig.getConfigScreen(ModConfig.class, parent).get();
    }
}
