package net.cozystudios.soundscape.compat;

import net.cozystudios.soundscape.Soundscape;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Field;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class AmendmentsCompat {

    private static final Supplier<Boolean> ALWAYS_FALSE = () -> Boolean.FALSE;

    @SuppressWarnings("deprecation")
    public static void tryDisableJukeboxAssets() {
        if (!FabricLoader.getInstance().isModLoaded("amendments")) return;
        try {
            Class<?> configClass = Class.forName("net.mehvahdjukaar.amendments.configs.ClientConfigs");
            Field field = configClass.getDeclaredField("JUKEBOX_MODEL");
            field.setAccessible(true);

            Field theUnsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            sun.misc.Unsafe unsafe = (sun.misc.Unsafe) theUnsafeField.get(null);
            Object base = unsafe.staticFieldBase(field);
            long offset = unsafe.staticFieldOffset(field);
            unsafe.putObject(base, offset, ALWAYS_FALSE);

            Soundscape.LOGGER.info("Disabled Amendments' JUKEBOX_MODEL feature; Soundscape's jukebox blockstates/models will be used");
        } catch (Throwable t) {
            Soundscape.LOGGER.warn("Failed to disable Amendments' JUKEBOX_MODEL feature; jukebox visuals may conflict", t);
        }
    }
}
