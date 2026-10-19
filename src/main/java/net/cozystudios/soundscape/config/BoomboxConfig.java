package net.cozystudios.soundscape.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "boombox")
public class BoomboxConfig implements ConfigData {

    public enum AudioMode {
        DIRECTIONAL,
        FLAT
    }

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public AudioMode audioMode = AudioMode.DIRECTIONAL;

    public static BoomboxConfig get() {
        return ModConfig.get().boomboxConfig;
    }
}
