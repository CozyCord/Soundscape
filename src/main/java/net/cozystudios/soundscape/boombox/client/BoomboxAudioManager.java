package net.cozystudios.soundscape.boombox.client;

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.Android;
import dev.lavalink.youtube.clients.AndroidMusic;
import dev.lavalink.youtube.clients.AndroidVr;
import dev.lavalink.youtube.clients.Ios;
import dev.lavalink.youtube.clients.MWeb;
import dev.lavalink.youtube.clients.Music;
import dev.lavalink.youtube.clients.Tv;
import dev.lavalink.youtube.clients.TvHtml5Simply;
import dev.lavalink.youtube.clients.Web;
import dev.lavalink.youtube.clients.WebEmbedded;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.cozystudios.soundscape.Soundscape;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Environment(EnvType.CLIENT)
public class BoomboxAudioManager {

    private static BoomboxAudioManager INSTANCE;

    private AudioPlayerManager lavaPlayerManager;
    private final Map<BlockPos, BoomboxAudioInstance> activeBoomboxes = new ConcurrentHashMap<>();
    private final Map<BlockPos, Integer> boomboxRanges = new ConcurrentHashMap<>();
    private final Set<BlockPos> loadingPositions = ConcurrentHashMap.newKeySet();
    private final Map<BlockPos, CachedState> cachedStates = new ConcurrentHashMap<>();
    private final Set<BlockPos> pausedByGame = ConcurrentHashMap.newKeySet();
    private boolean initialized = false;
    private boolean gamePaused = false;

    public record CachedState(String url, int stateId, float volume, boolean loop, String errorMessage, int range) {}


    private BoomboxAudioManager() {
    }

    public static BoomboxAudioManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BoomboxAudioManager();
        }
        return INSTANCE;
    }

    private void ensureInitialized() {
        if (!initialized) {
            try {
                lavaPlayerManager = new DefaultAudioPlayerManager();
                lavaPlayerManager.getConfiguration().setOutputFormat(StandardAudioDataFormats.DISCORD_PCM_S16_BE);
                lavaPlayerManager.registerSourceManager(new YoutubeAudioSourceManager(true,
                        new TvHtml5Simply(),
                        new Tv(),
                        new Ios(),
                        new AndroidVr(),
                        new AndroidMusic(),
                        new Android(),
                        new Music(),
                        new WebEmbedded(),
                        new Web(),
                        new MWeb()
                ));
                initialized = true;
                Soundscape.LOGGER.info("LavaPlayer initialized successfully for Boombox");
            } catch (Exception e) {
                Soundscape.LOGGER.error("Failed to initialize LavaPlayer", e);
            }
        }
    }

    public void setRange(BlockPos pos, int range) {
        boomboxRanges.put(pos, range);
    }

    public void play(BlockPos pos, String url, float volume, boolean loop) {
        playFromPosition(pos, url, volume, loop, 0, 0);
    }

    public void playFromPosition(BlockPos pos, String url, float volume, boolean loop, long positionMs, int trackIndex) {
        stop(pos);
        loadingPositions.add(pos);

        ensureInitialized();
        if (!initialized || lavaPlayerManager == null) {
            loadingPositions.remove(pos);
            Soundscape.LOGGER.warn("Cannot play: LavaPlayer not initialized");
            return;
        }

        lavaPlayerManager.loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                Minecraft.getInstance().execute(() -> {
                    if (!loadingPositions.remove(pos)) return;
                    AudioPlayer audioPlayer = lavaPlayerManager.createPlayer();
                    List<AudioTrack> singleTrack = new ArrayList<>();
                    singleTrack.add(track);
                    BoomboxAudioInstance instance = new BoomboxAudioInstance(audioPlayer, volume, loop, singleTrack);
                    if (positionMs > 0) {
                        track.setPosition(positionMs);
                    }
                    audioPlayer.playTrack(track);
                    instance.initialize();
                    activeBoomboxes.put(pos, instance);
                    Soundscape.LOGGER.info("Playing track at boombox {}", pos);
                });
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                List<AudioTrack> tracks = playlist.getTracks();
                if (tracks.isEmpty()) {
                    loadingPositions.remove(pos);
                    return;
                }

                Minecraft.getInstance().execute(() -> {
                    if (!loadingPositions.remove(pos)) return;
                    AudioPlayer audioPlayer = lavaPlayerManager.createPlayer();
                    BoomboxAudioInstance instance = new BoomboxAudioInstance(audioPlayer, volume, loop, tracks);

                    int startIndex = trackIndex;
                    if (startIndex <= 0) {
                        AudioTrack selectedTrack = playlist.getSelectedTrack();
                        if (selectedTrack != null) {
                            int idx = tracks.indexOf(selectedTrack);
                            if (idx >= 0) startIndex = idx;
                        }
                    }
                    if (startIndex >= tracks.size()) startIndex = 0;
                    instance.setTrackIndex(startIndex);

                    AudioTrack trackToPlay = tracks.get(startIndex).makeClone();
                    if (positionMs > 0) {
                        trackToPlay.setPosition(positionMs);
                    }
                    audioPlayer.playTrack(trackToPlay);
                    instance.initialize();
                    activeBoomboxes.put(pos, instance);
                    Soundscape.LOGGER.info("Playing playlist ({} tracks) at boombox {}", tracks.size(), pos);
                });
            }

            @Override
            public void noMatches() {
                loadingPositions.remove(pos);
                Soundscape.LOGGER.warn("No matches found for URL at boombox {}", pos);
                Minecraft.getInstance().execute(() -> updateScreenError(pos, "No matches found for URL"));
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                loadingPositions.remove(pos);
                Soundscape.LOGGER.error("Failed to load track at boombox {}: {}", pos, exception.getMessage());
                Minecraft.getInstance().execute(() -> updateScreenError(pos, "Load failed: " + exception.getMessage()));
            }
        });
    }

    public boolean isPlaying(BlockPos pos) {
        if (loadingPositions.contains(pos)) return true;
        BoomboxAudioInstance instance = activeBoomboxes.get(pos);
        return instance != null && !instance.isDestroyed();
    }

    public void stop(BlockPos pos) {
        loadingPositions.remove(pos);
        BoomboxAudioInstance instance = activeBoomboxes.remove(pos);
        if (instance != null) {
            instance.destroy();
        }
        boomboxRanges.remove(pos);
    }

    public void pause(BlockPos pos) {
        BoomboxAudioInstance instance = activeBoomboxes.get(pos);
        if (instance != null && !instance.isPaused()) {
            instance.pause();
        }
    }

    public void resume(BlockPos pos) {
        BoomboxAudioInstance instance = activeBoomboxes.get(pos);
        if (instance != null && instance.isPaused()) {
            instance.resume();
        }
    }

    public long getTrackPositionMs(BlockPos pos) {
        BoomboxAudioInstance instance = activeBoomboxes.get(pos);
        if (instance != null) {
            return instance.getTrackPositionMs();
        }
        return 0;
    }

    public void seek(BlockPos pos, long positionMs) {
        BoomboxAudioInstance instance = activeBoomboxes.get(pos);
        if (instance != null) {
            instance.seekTo(positionMs);
        }
    }

    public void setVolume(BlockPos pos, float volume) {
        BoomboxAudioInstance instance = activeBoomboxes.get(pos);
        if (instance != null) {
            instance.setVolume(volume);
        }
    }

    public void setLoop(BlockPos pos, boolean loop) {
        BoomboxAudioInstance instance = activeBoomboxes.get(pos);
        if (instance != null) {
            instance.setLoop(loop);
        }
    }

    public void nextTrack(BlockPos pos) {
        BoomboxAudioInstance instance = activeBoomboxes.get(pos);
        if (instance != null) {
            instance.nextTrack();
        }
    }

    public void prevTrack(BlockPos pos) {
        BoomboxAudioInstance instance = activeBoomboxes.get(pos);
        if (instance != null) {
            instance.prevTrack();
        }
    }

    public String getTrackInfo(BlockPos pos) {
        BoomboxAudioInstance instance = activeBoomboxes.get(pos);
        if (instance != null) {
            return instance.getTrackInfo();
        }
        return "";
    }

    public int getTrackIndex(BlockPos pos) {
        BoomboxAudioInstance instance = activeBoomboxes.get(pos);
        if (instance != null) {
            return instance.getTrackIndex();
        }
        return 0;
    }

    public void tick(Player player) {
        if (activeBoomboxes.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        boolean isGamePaused = mc.isPaused();

        if (isGamePaused && !gamePaused) {
            gamePaused = true;
            for (Map.Entry<BlockPos, BoomboxAudioInstance> entry : activeBoomboxes.entrySet()) {
                BoomboxAudioInstance instance = entry.getValue();
                if (!instance.isPaused() && !instance.isDestroyed()) {
                    instance.pause();
                    pausedByGame.add(entry.getKey());
                }
            }
            return;
        } else if (!isGamePaused && gamePaused) {
            gamePaused = false;
            for (BlockPos pos : pausedByGame) {
                BoomboxAudioInstance instance = activeBoomboxes.get(pos);
                if (instance != null && !instance.isDestroyed()) {
                    instance.resume();
                }
            }
            pausedByGame.clear();
        }

        if (gamePaused) return;

        Vec3 playerPos = new Vec3(player.getX(), player.getY(), player.getZ());
        activeBoomboxes.entrySet().removeIf(entry -> {
            BoomboxAudioInstance instance = entry.getValue();
            if (instance.isDestroyed()) return true;

            BlockPos boomboxPos = entry.getKey();

            if (instance.needsClientRetry()) {
                instance.destroy();
                Soundscape.LOGGER.error("Boombox at {} playback failed (YouTube signature error - try a different link)", boomboxPos);
                updateScreenError(boomboxPos, "YouTube playback error - try a different link");
                return true;
            }

            if (mc.level != null && !(mc.level.getBlockEntity(boomboxPos) instanceof net.cozystudios.soundscape.boombox.BoomboxBlockEntity)) {
                instance.destroy();
                boomboxRanges.remove(boomboxPos);
                return true;
            }

            int maxRange = boomboxRanges.getOrDefault(boomboxPos, 48);
            float destroyRange = maxRange * 3.0f;
            double distance = Math.sqrt(player.distanceToSqr(Vec3.atCenterOf(boomboxPos)));

            if (distance > destroyRange) {
                instance.destroy();
                return true;
            }

            instance.updatePositionAndGain(playerPos, boomboxPos, distance, maxRange);
            instance.tick();
            return false;
        });
    }

    private void updateScreenError(BlockPos pos, String error) {
        Minecraft client = Minecraft.getInstance();
        if (client.screen instanceof BoomboxScreen screen
                && screen.getBoomboxPos().equals(pos)) {
            screen.updateState("", 3, 0f, false, error, boomboxRanges.getOrDefault(pos, 48));
        }
    }

    public void cacheState(BlockPos pos, String url, int stateId, float volume, boolean loop, String errorMessage, int range) {
        cachedStates.put(pos, new CachedState(url, stateId, volume, loop, errorMessage, range));
    }

    public CachedState getCachedState(BlockPos pos) {
        return cachedStates.get(pos);
    }

    public void stopAll() {
        loadingPositions.clear();
        for (BoomboxAudioInstance instance : activeBoomboxes.values()) {
            instance.destroy();
        }
        activeBoomboxes.clear();
        boomboxRanges.clear();
        cachedStates.clear();
        pausedByGame.clear();
        gamePaused = false;
    }

    public void cleanup() {
        stopAll();
        if (lavaPlayerManager != null) {
            lavaPlayerManager.shutdown();
            lavaPlayerManager = null;
        }
        initialized = false;
    }
}
