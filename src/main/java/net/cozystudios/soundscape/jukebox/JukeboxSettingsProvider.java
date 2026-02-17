package net.cozystudios.soundscape.jukebox;

public interface JukeboxSettingsProvider {

    JukeboxSettings soundscape$getSettings();

    void soundscape$setSettings(JukeboxSettings settings);

    ShuffleQueue soundscape$getShuffleQueue();

    void soundscape$setShuffleQueue(ShuffleQueue queue);
}
