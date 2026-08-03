package net.cozystudios.soundscape;

import net.minecraft.resources.Identifier;

public class SoundscapeId {
    public static Identifier of(String path) {
        return Identifier.fromNamespaceAndPath(Soundscape.MOD_ID, path);
    }
}
