package net.cozystudios.soundscape.boombox.youtube;

import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.ClientOptions;
import dev.lavalink.youtube.clients.Web;

public final class YoutubeSearchClient extends Web {

    private static final ClientOptions SEARCH_ONLY = makeOptions();

    public YoutubeSearchClient() {
        super(SEARCH_ONLY);
    }

    private static ClientOptions makeOptions() {
        ClientOptions opts = new ClientOptions();
        opts.setSearching(true);
        opts.setVideoLoading(false);
        opts.setPlaylistLoading(false);
        opts.setPlayback(false);
        return opts;
    }

    @Override
    public boolean canHandleRequest(String identifier) {
        return identifier.startsWith(YoutubeAudioSourceManager.SEARCH_PREFIX);
    }
}
