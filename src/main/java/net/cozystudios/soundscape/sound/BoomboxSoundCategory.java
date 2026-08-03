package net.cozystudios.soundscape.sound;

import net.cozystudios.soundscape.Soundscape;
import net.cozystudios.soundscape.mixin.client.SoundSourceAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.sounds.SoundSource;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class BoomboxSoundCategory {

    public static SoundSource BOOMBOX = SoundSource.RECORDS;
    private static boolean registered = false;
    private static boolean mapInjectionAttempted = false;

    @SuppressWarnings("deprecation")
    public static void register() {
        try {
            Field valuesField = null;
            for (Field f : SoundSource.class.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) && f.getType() == SoundSource[].class) {
                    valuesField = f;
                    break;
                }
            }
            if (valuesField == null) {
                Soundscape.LOGGER.warn("Could not locate SoundSource values array; falling back to RECORDS");
                return;
            }
            valuesField.setAccessible(true);
            SoundSource[] existing = (SoundSource[]) valuesField.get(null);

            SoundSource boombox = SoundSourceAccessor.soundscape$init(
                    "BOOMBOX", existing.length, "boombox");

            SoundSource[] combined = new SoundSource[existing.length + 1];
            System.arraycopy(existing, 0, combined, 0, existing.length);
            combined[existing.length] = boombox;

            Field theUnsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            sun.misc.Unsafe unsafe = (sun.misc.Unsafe) theUnsafeField.get(null);
            Object base = unsafe.staticFieldBase(valuesField);
            long offset = unsafe.staticFieldOffset(valuesField);
            unsafe.putObject(base, offset, combined);

            BOOMBOX = boombox;
            registered = true;
            Soundscape.LOGGER.info("Registered custom sound category: boombox");
        } catch (Throwable t) {
            Soundscape.LOGGER.warn("Failed to register Boombox sound category, falling back to RECORDS", t);
        }
    }

    public static float getEffectiveVolume() {
        if (!registered) return 1.0f;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.options == null) return 1.0f;
        ensureMapInjection(mc.options);
        try {
            return mc.options.getSoundSourceVolume(BOOMBOX);
        } catch (Throwable t) {
            return 1.0f;
        }
    }

    @SuppressWarnings("unchecked")
    private static void ensureMapInjection(Options options) {
        if (mapInjectionAttempted) return;
        mapInjectionAttempted = true;
        try {
            Map<SoundSource, Object> soundMap = null;
            for (Field f : options.getClass().getDeclaredFields()) {
                if (!Map.class.isAssignableFrom(f.getType())) continue;
                f.setAccessible(true);
                Object v = f.get(options);
                if (!(v instanceof Map<?, ?> map) || map.isEmpty()) continue;
                Object firstKey = map.keySet().iterator().next();
                if (firstKey instanceof SoundSource) {
                    soundMap = (Map<SoundSource, Object>) v;
                    break;
                }
            }
            if (soundMap == null) {
                Soundscape.LOGGER.warn("Could not locate sound volume map on Options");
                return;
            }
            if (soundMap.containsKey(BOOMBOX)) return;

            Object recordsOption = soundMap.get(SoundSource.RECORDS);
            if (recordsOption == null) return;
            Class<?> optionClass = recordsOption.getClass();

            Method createMethod = null;
            for (Method m : options.getClass().getDeclaredMethods()) {
                if (m.getParameterCount() != 2) continue;
                Class<?>[] params = m.getParameterTypes();
                if (params[0] != String.class || params[1] != SoundSource.class) continue;
                if (!optionClass.isAssignableFrom(m.getReturnType())) continue;
                createMethod = m;
                break;
            }
            if (createMethod == null) {
                Soundscape.LOGGER.warn("Could not locate sound volume option factory method; boombox slider may not work");
                return;
            }
            createMethod.setAccessible(true);
            Object boomboxOption = createMethod.invoke(options, "options.sound.category.boombox", BOOMBOX);
            soundMap.put(BOOMBOX, boomboxOption);
            Soundscape.LOGGER.info("Injected boombox sound category into vanilla options map");
        } catch (Throwable t) {
            Soundscape.LOGGER.warn("Failed to inject boombox category into options map", t);
        }
    }
}
