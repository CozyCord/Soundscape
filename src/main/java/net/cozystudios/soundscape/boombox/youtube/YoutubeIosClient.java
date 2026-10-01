package net.cozystudios.soundscape.boombox.youtube;

import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.ClientConfig;
import dev.lavalink.youtube.clients.ClientOptions;
import dev.lavalink.youtube.clients.Ios;

public final class YoutubeIosClient extends Ios {

    public static final String CLIENT_VERSION = "21.32.4";

    private static final String USER_AGENT =
            "com.google.ios.youtube/" + CLIENT_VERSION
                    + " (iPhone16,2; U; CPU iOS 18_1_0 like Mac OS X;)";

    private static final ClientConfig CONFIG = new ClientConfig()
            .withUserAgent(USER_AGENT)
            .withClientName("IOS")
            .withClientField("clientVersion", CLIENT_VERSION)
            .withUserField("lockedSafetyMode", false);

    public YoutubeIosClient() {
        super(ClientOptions.DEFAULT);
    }

    @Override
    protected ClientConfig getBaseClientConfig(HttpInterface httpInterface) {
        return CONFIG.copy();
    }

    @Override
    public String getPlayerParams() {
        return WEB_PLAYER_PARAMS;
    }

    @Override
    public boolean canHandleRequest(String identifier) {
        return super.canHandleRequest(identifier)
                && !identifier.startsWith(YoutubeAudioSourceManager.SEARCH_PREFIX);
    }
}
