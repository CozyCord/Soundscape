package net.cozystudios.soundscape.boombox.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.cozystudios.soundscape.Soundscape;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

// Stored in config/soundscape-client.json
@Environment(EnvType.CLIENT)
public final class BoomboxClientConfig {

    public enum AudioMode {
        DIRECTIONAL("3D"),
        FLAT("Flat");

        private final String label;

        AudioMode(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public AudioMode next() {
            AudioMode[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "soundscape-client.json";
    private static final String KEY_AUDIO_MODE = "boomboxAudioMode";

    private static AudioMode audioMode = AudioMode.DIRECTIONAL;
    private static boolean loaded = false;

    private BoomboxClientConfig() {
    }

    public static AudioMode getAudioMode() {
        ensureLoaded();
        return audioMode;
    }

    public static void setAudioMode(AudioMode mode) {
        ensureLoaded();
        if (mode == null || mode == audioMode) return;
        audioMode = mode;
        save();
    }

    private static Path getPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;

        Path path = getPath();
        if (!Files.exists(path)) {
            save();
            return;
        }

        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (root.has(KEY_AUDIO_MODE)) {
                try {
                    audioMode = AudioMode.valueOf(root.get(KEY_AUDIO_MODE).getAsString().toUpperCase());
                } catch (IllegalArgumentException badValue) {
                    Soundscape.LOGGER.warn("Unknown {} value in {}, using {}", KEY_AUDIO_MODE, FILE_NAME, audioMode);
                }
            }
        } catch (IOException | RuntimeException e) {
            Soundscape.LOGGER.warn("Failed to read {}, using defaults", FILE_NAME, e);
        }
    }

    private static void save() {
        JsonObject root = new JsonObject();
        root.addProperty(KEY_AUDIO_MODE, audioMode.name());
        try {
            Path path = getPath();
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Soundscape.LOGGER.warn("Failed to write {}", FILE_NAME, e);
        }
    }
}
