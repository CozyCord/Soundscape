package net.cozystudios.soundscape.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;

@Config(name = "soundscape")
public class ModConfig extends PartitioningSerializer.GlobalData {
    @ConfigEntry.Category("boombox")
    @ConfigEntry.Gui.TransitiveObject
    BoomboxConfig boomboxConfig = new BoomboxConfig();

    public static void register() {
        AutoConfig.register(ModConfig.class, PartitioningSerializer.wrap(GsonConfigSerializer::new));
    }

    public static void reload() {
        AutoConfig.getConfigHolder(ModConfig.class).load();
    }

    public static ModConfig get() {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }
}
