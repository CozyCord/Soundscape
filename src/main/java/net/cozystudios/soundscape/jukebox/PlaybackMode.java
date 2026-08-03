package net.cozystudios.soundscape.jukebox;

public enum PlaybackMode {
    SINGLE,
    LOOP,
    SHUFFLE;

    public PlaybackMode next() {
        PlaybackMode[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}
