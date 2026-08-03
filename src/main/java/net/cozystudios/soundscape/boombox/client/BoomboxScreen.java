package net.cozystudios.soundscape.boombox.client;

import net.cozystudios.soundscape.SoundscapeId;
import net.cozystudios.soundscape.boombox.BoomboxBlockEntity;
import net.cozystudios.soundscape.network.SoundscapeClientNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class BoomboxScreen extends Screen {

    private static final Identifier TEXTURE = SoundscapeId.of("textures/gui/boombox.png");
    private static final int GUI_WIDTH = 360;
    private static final int GUI_HEIGHT = 260;

    private final BlockPos boomboxPos;
    private EditBox urlField;
    private Button playButton;
    private Button pauseButton;
    private Button stopButton;
    private Button loopButton;
    private Button prevButton;
    private Button nextButton;
    private Button skipBwdButton;
    private Button skipFwdButton;
    private BoomboxVolumeSlider volumeSlider;
    private BoomboxRangeSlider rangeSlider;

    private int guiX;
    private int guiY;

    private String currentUrl = "";
    private float currentVolume = 0.5f;
    private boolean currentLoop = false;
    private int currentState = 0;
    private int currentRange = 48;
    private String statusText = "Idle";
    private String errorText = "";

    private String lastTrackInfo = "";
    private long scrollStartTime = 0;

    public BoomboxScreen(BlockPos pos) {
        super(Component.literal("Boombox"));
        this.boomboxPos = pos;
    }

    public BlockPos getBoomboxPos() {
        return boomboxPos;
    }

    @Override
    protected void init() {
        super.init();

        guiX = (this.width - GUI_WIDTH) / 2;
        guiY = (this.height - GUI_HEIGHT) / 2;

        int wx = guiX + 80;
        int wy = guiY + 55;

        urlField = new EditBox(this.font, wx, wy, 200, 20, Component.literal("URL"));
        urlField.setMaxLength(256);
        urlField.setValue(currentUrl);
        urlField.setResponder(this::onUrlChanged);
        if (currentUrl.isEmpty()) urlField.setSuggestion("Enter YouTube link...");
        addRenderableWidget(urlField);

        playButton = Button.builder(Component.literal("Play"), button -> sendPlay())
                .bounds(wx, wy + 30, 63, 20)
                .build();
        addRenderableWidget(playButton);

        pauseButton = Button.builder(
                Component.literal(currentState == 4 ? "Resume" : "Pause"),
                button -> {
                    if (currentState == 4) {
                        sendResume();
                    } else {
                        sendPause();
                    }
                }
        ).bounds(wx + 68, wy + 30, 63, 20).build();
        addRenderableWidget(pauseButton);

        stopButton = Button.builder(Component.literal("Stop"), button -> sendStop())
                .bounds(wx + 136, wy + 30, 64, 20)
                .build();
        addRenderableWidget(stopButton);

        volumeSlider = new BoomboxVolumeSlider(
                wx, wy + 58, 200, 20,
                currentVolume, this::onVolumeChanged
        );
        addRenderableWidget(volumeSlider);

        rangeSlider = new BoomboxRangeSlider(
                wx, wy + 82, 200, 20,
                currentRange, this::onRangeChanged
        );
        addRenderableWidget(rangeSlider);

        loopButton = Button.builder(
                Component.literal(currentLoop ? "Loop: ON" : "Loop: OFF"),
                button -> sendToggleLoop()
        ).bounds(wx, wy + 110, 80, 20).build();
        addRenderableWidget(loopButton);

        prevButton = Button.builder(Component.literal("<<"), button -> sendPrev())
                .bounds(wx + 84, wy + 110, 25, 20)
                .build();
        addRenderableWidget(prevButton);

        skipBwdButton = Button.builder(Component.literal("-15s"), button -> sendSkipBwd())
                .bounds(wx + 112, wy + 110, 30, 20)
                .build();
        addRenderableWidget(skipBwdButton);

        skipFwdButton = Button.builder(Component.literal("+15s"), button -> sendSkipFwd())
                .bounds(wx + 145, wy + 110, 30, 20)
                .build();
        addRenderableWidget(skipFwdButton);

        nextButton = Button.builder(Component.literal(">>"), button -> sendNext())
                .bounds(wx + 178, wy + 110, 22, 20)
                .build();
        addRenderableWidget(nextButton);

        SoundscapeClientNetworking.requestBoomboxState(boomboxPos);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        if (this.minecraft != null && this.minecraft.level != null) {
            context.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        }

        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, guiX, guiY, 0.0f, 0.0f, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);

        int centerX = this.width / 2;

        context.centeredText(this.font, Component.literal("Boombox"), centerX, guiY + 42, 0xFFFFFFFF);

        String trackInfo = BoomboxAudioManager.getInstance().getTrackInfo(boomboxPos);
        if (!trackInfo.isEmpty()) {
            int panelLeft = guiX + 72;
            int panelRight = guiX + 288;
            int panelWidth = panelRight - panelLeft;
            int textWidth = this.font.width(trackInfo);
            int textY = guiY + 207;

            if (!trackInfo.equals(lastTrackInfo)) {
                lastTrackInfo = trackInfo;
                scrollStartTime = System.currentTimeMillis();
            }

            if (textWidth <= panelWidth) {
                context.centeredText(this.font, Component.literal(trackInfo), centerX, textY, 0xFFFFFF55);
            } else {
                int overflow = textWidth - panelWidth;
                long elapsed = System.currentTimeMillis() - scrollStartTime;

                long pauseMs = 2000;
                long scrollMs = (long) (overflow / 30.0 * 1000);
                if (scrollMs < 1000) scrollMs = 1000;
                long totalCycle = pauseMs + scrollMs + pauseMs;
                long cyclePos = elapsed % totalCycle;

                int scrollOffset;
                if (cyclePos < pauseMs) {
                    scrollOffset = 0;
                } else if (cyclePos < pauseMs + scrollMs) {
                    float progress = (float) (cyclePos - pauseMs) / scrollMs;
                    scrollOffset = (int) (progress * overflow);
                } else {
                    scrollOffset = overflow;
                }

                context.enableScissor(panelLeft, textY - 1, panelRight, textY + 10);
                context.text(this.font, Component.literal(trackInfo), panelLeft - scrollOffset, textY, 0xFFFFFF55, true);
                context.disableScissor();
            }
        }

        int statusColor = switch (currentState) {
            case 1 -> 0xFF55FF55;
            case 2 -> 0xFFAAAAAA;
            case 3 -> 0xFFFF5555;
            case 4 -> 0xFFFFFF55;
            default -> 0xFFFFFFFF;
        };
        context.centeredText(this.font, Component.literal("Status: " + statusText), centerX, guiY + 222, statusColor);

        if (!errorText.isEmpty()) {
            context.centeredText(this.font, Component.literal(errorText), centerX, guiY + 237, 0xFFFF5555);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void applyCachedState(BoomboxAudioManager.CachedState cached) {
        this.currentUrl = cached.url();
        this.currentVolume = cached.volume();
        this.currentLoop = cached.loop();
        this.errorText = cached.errorMessage();
        this.currentRange = cached.range();
        if (cached.stateId() < 10) {
            this.currentState = cached.stateId();
            this.statusText = switch (cached.stateId()) {
                case 1 -> "Playing";
                case 2 -> "Stopped";
                case 3 -> "Error";
                case 4 -> "Paused";
                default -> "Idle";
            };
        }
    }

    public void updateState(String url, int state, float volume, boolean loop, String error, int range) {
        this.currentUrl = url;
        this.currentVolume = volume;
        this.currentLoop = loop;
        this.errorText = error;
        this.currentRange = range;

        if (state < 10) {
            this.currentState = state;
            this.statusText = switch (state) {
                case 1 -> "Playing";
                case 2 -> "Stopped";
                case 3 -> "Error";
                case 4 -> "Paused";
                default -> "Idle";
            };
        }

        if (urlField != null && !urlField.isFocused()) {
            urlField.setValue(url);
        }
        if (volumeSlider != null) {
            volumeSlider.setValueFromServer(volume);
        }
        if (rangeSlider != null) {
            rangeSlider.setRange(range);
        }
        if (loopButton != null) {
            loopButton.setMessage(Component.literal(loop ? "Loop: ON" : "Loop: OFF"));
        }
        if (pauseButton != null) {
            pauseButton.setMessage(Component.literal(this.currentState == 4 ? "Resume" : "Pause"));
        }
    }

    private void onUrlChanged(String newUrl) {
        urlField.setSuggestion(newUrl.isEmpty() ? "Enter YouTube link..." : null);
    }

    private void onVolumeChanged(double value) {
        sendAction(BoomboxBlockEntity.ACTION_SET_VOLUME, "", (float) value, 0L);
    }

    private void onRangeChanged(int range) {
        sendAction(BoomboxBlockEntity.ACTION_SET_RANGE, "", (float) range, 0L);
    }

    private void sendPlay() {
        String url = urlField.getValue().trim();
        if (!url.isEmpty()) {
            sendAction(BoomboxBlockEntity.ACTION_SET_URL, url, 0f, 0L);
        }
        sendAction(BoomboxBlockEntity.ACTION_PLAY, "", 0f, 0L);
    }

    private void sendResume() {
        sendAction(BoomboxBlockEntity.ACTION_PLAY, "", 0f, 0L);
    }

    private void sendPause() {
        long posMs = BoomboxAudioManager.getInstance().getTrackPositionMs(boomboxPos);
        sendAction(BoomboxBlockEntity.ACTION_PAUSE, "", 0f, posMs);
    }

    private void sendStop() {
        sendAction(BoomboxBlockEntity.ACTION_STOP, "", 0f, 0L);
    }

    private void sendToggleLoop() {
        sendAction(BoomboxBlockEntity.ACTION_TOGGLE_LOOP, "", 0f, 0L);
    }

    private void sendNext() {
        sendAction(BoomboxBlockEntity.ACTION_NEXT_TRACK, "", 0f, 0L);
    }

    private void sendPrev() {
        sendAction(BoomboxBlockEntity.ACTION_PREV_TRACK, "", 0f, 0L);
    }

    private void sendSkipFwd() {
        long posMs = BoomboxAudioManager.getInstance().getTrackPositionMs(boomboxPos);
        BoomboxAudioManager.getInstance().seek(boomboxPos, posMs + 15000);
        sendAction(BoomboxBlockEntity.ACTION_SKIP_FWD, "", 0f, posMs);
    }

    private void sendSkipBwd() {
        long posMs = BoomboxAudioManager.getInstance().getTrackPositionMs(boomboxPos);
        BoomboxAudioManager.getInstance().seek(boomboxPos, Math.max(0, posMs - 15000));
        sendAction(BoomboxBlockEntity.ACTION_SKIP_BWD, "", 0f, posMs);
    }

    private void sendAction(int action, String url, float volume, long positionMs) {
        SoundscapeClientNetworking.sendBoomboxAction(boomboxPos, action, url, volume, positionMs);
    }
}
