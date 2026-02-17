package net.cozystudios.soundscape;

import net.minecraft.util.Identifier;

public class SoundscapeId {
    public static Identifier of(String path) {
        //? if >=1.21 {
        return Identifier.of(Soundscape.MOD_ID, path);
        //?} else {
        /*return new Identifier(Soundscape.MOD_ID, path);
        *///?}
    }
}
