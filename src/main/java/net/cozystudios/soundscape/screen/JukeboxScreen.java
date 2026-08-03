package net.cozystudios.soundscape.screen;

import net.cozystudios.soundscape.SoundscapeId;
import net.cozystudios.soundscape.jukebox.PlaybackMode;
import net.cozystudios.soundscape.network.SoundscapeNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class JukeboxScreen extends AbstractContainerScreen<JukeboxScreenHandler> {

    private static final Identifier TEXTURE = SoundscapeId.of("textures/gui/jukebox.png");

    private static final Map<BlockPos, RotationState> ROTATION_CACHE = new HashMap<>();

    private static class RotationState {
        float rotation;
        long timestamp;
        float pitchMultiplier;
        boolean wasPlaying;

        RotationState(float rotation, long timestamp, float pitchMultiplier, boolean wasPlaying) {
            this.rotation = rotation;
            this.timestamp = timestamp;
            this.pitchMultiplier = pitchMultiplier;
            this.wasPlaying = wasPlaying;
        }
    }

    private static final int GUI_WIDTH = 230;
    private static final int GUI_HEIGHT = 276;

    private static final int DISC_WELL_X = 77;
    private static final int DISC_WELL_Y = 27;
    private static final int DISC_WELL_SIZE = 76;

    private static final int CONTROL_Y = 92;

    private static final int SLIDER_X = 50;
    private static final int SLIDER_WIDTH = 130;
    private static final int SLIDER_HEIGHT = 14;
    private static final int SLIDER_ROW_SPACING = 18;

    private static final int LEFT_BTN_X = 30;
    private static final int RIGHT_BTN_X = 184;
    private static final int BUTTON_SIZE = 16;
    private static final int BTN_ROW_1_Y = 94;
    private static final int BTN_ROW_2_Y = 112;
    private static final int BTN_ROW_3_Y = 130;

    private static final int PLAYER_INV_Y = 194;

    private JukeboxSlider volumeSlider;
    private JukeboxSlider pitchSlider;
    private JukeboxSlider rangeSlider;

    private Button pauseButton;
    private Button playButton;
    private Button particleButton;
    private Button singleButton;
    private Button loopButton;
    private Button shuffleButton;

    private boolean initialSyncDone = false;
    private int syncAttempts = 0;
    private int currentMode = -1;
    private boolean currentParticles = true;
    private float discRotation = 0;
    private long lastTickTime = 0;

    private boolean elapsedRotationApplied = false;

    public JukeboxScreen(JukeboxScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, GUI_WIDTH, GUI_HEIGHT);

        BlockPos pos = handler.getPos();
        if (pos != null && ROTATION_CACHE.containsKey(pos)) {
            RotationState state = ROTATION_CACHE.get(pos);
            this.discRotation = state.rotation;
        }
    }

    @Override
    protected void init() {
        super.init();

        this.titleLabelY = 5;
        this.inventoryLabelY = PLAYER_INV_Y - 11;

        int guiLeft = this.leftPos;
        int guiTop = this.topPos;

        this.volumeSlider = new JukeboxSlider(
                guiLeft + SLIDER_X,
                guiTop + CONTROL_Y + 4,
                SLIDER_WIDTH, SLIDER_HEIGHT,
                100, 0, 200,
                "Volume", "%",
                v -> sendCurrentSettings()
        );
        this.addRenderableWidget(volumeSlider);

        this.pitchSlider = new JukeboxSlider(
                guiLeft + SLIDER_X,
                guiTop + CONTROL_Y + 4 + SLIDER_ROW_SPACING,
                SLIDER_WIDTH, SLIDER_HEIGHT,
                100, 50, 200,
                "Pitch", "x",
                v -> sendCurrentSettings()
        );
        this.addRenderableWidget(pitchSlider);

        this.rangeSlider = new JukeboxSlider(
                guiLeft + SLIDER_X,
                guiTop + CONTROL_Y + 4 + SLIDER_ROW_SPACING * 2,
                SLIDER_WIDTH, SLIDER_HEIGHT,
                64, 0, 128,
                "Range", "",
                v -> sendCurrentSettings()
        );
        this.addRenderableWidget(rangeSlider);

        this.pauseButton = Button.builder(
                Component.literal("⏸"),
                button -> sendPlaybackCommand("pause")
        ).bounds(guiLeft + LEFT_BTN_X, guiTop + BTN_ROW_1_Y, BUTTON_SIZE, BUTTON_SIZE).build();
        this.addRenderableWidget(pauseButton);

        this.playButton = Button.builder(
                Component.literal("▶"),
                button -> sendPlaybackCommand("play")
        ).bounds(guiLeft + LEFT_BTN_X, guiTop + BTN_ROW_2_Y, BUTTON_SIZE, BUTTON_SIZE).build();
        this.addRenderableWidget(playButton);

        this.particleButton = Button.builder(
                Component.literal("✦"),
                button -> {
                    currentParticles = !currentParticles;
                    sendParticlesSetting(currentParticles);
                    updateButtonStyles();
                }
        ).bounds(guiLeft + LEFT_BTN_X, guiTop + BTN_ROW_3_Y, BUTTON_SIZE, BUTTON_SIZE).build();
        this.addRenderableWidget(particleButton);

        this.singleButton = Button.builder(
                Component.literal("1"),
                button -> {
                    currentMode = PlaybackMode.SINGLE.ordinal();
                    updateButtonStyles();
                    sendCurrentSettings();
                }
        ).bounds(guiLeft + RIGHT_BTN_X, guiTop + BTN_ROW_1_Y, BUTTON_SIZE, BUTTON_SIZE).build();
        this.addRenderableWidget(singleButton);

        this.loopButton = Button.builder(
                Component.literal("🔁"),
                button -> {
                    currentMode = PlaybackMode.LOOP.ordinal();
                    updateButtonStyles();
                    sendCurrentSettings();
                }
        ).bounds(guiLeft + RIGHT_BTN_X, guiTop + BTN_ROW_2_Y, BUTTON_SIZE, BUTTON_SIZE).build();
        this.addRenderableWidget(loopButton);

        this.shuffleButton = Button.builder(
                Component.literal("🔀"),
                button -> {
                    currentMode = PlaybackMode.SHUFFLE.ordinal();
                    updateButtonStyles();
                    sendCurrentSettings();
                }
        ).bounds(guiLeft + RIGHT_BTN_X, guiTop + BTN_ROW_3_Y, BUTTON_SIZE, BUTTON_SIZE).build();
        this.addRenderableWidget(shuffleButton);

        menu.setClientShuffleModeOverride(true);

        updateButtonStyles();
    }

    private void updateButtonStyles() {
        int mode = currentMode >= 0 ? currentMode : 0;
        boolean isPlaying = menu.isPlaying();

        if (pauseButton != null) pauseButton.active = isPlaying;
        if (playButton != null) playButton.active = !isPlaying;

        if (singleButton != null) singleButton.active = mode != PlaybackMode.SINGLE.ordinal();
        if (loopButton != null) loopButton.active = mode != PlaybackMode.LOOP.ordinal();
        if (shuffleButton != null) shuffleButton.active = mode != PlaybackMode.SHUFFLE.ordinal();

        if (particleButton != null) {
            particleButton.setMessage(Component.literal(currentParticles ? "✦" : "✧"));
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        if (!initialSyncDone) {
            int serverVolume = menu.getVolume();
            int serverPitch = menu.getPitch();
            int serverRange = menu.getRange();
            int serverMode = menu.getPlaybackMode().ordinal();
            boolean serverParticles = menu.getShowParticles();
            syncAttempts++;

            if (serverVolume > 0 || serverPitch > 0 || syncAttempts > 20) {
                if (serverVolume > 0) volumeSlider.setValueFromServer(serverVolume);
                if (serverPitch > 0) pitchSlider.setValueFromServer(serverPitch);
                rangeSlider.setValueFromServer(serverRange);
                currentMode = serverMode;
                currentParticles = serverParticles;
                updateButtonStyles();
                initialSyncDone = true;

                if (!elapsedRotationApplied) {
                    elapsedRotationApplied = true;
                    BlockPos pos = menu.getPos();
                    if (pos != null && ROTATION_CACHE.containsKey(pos)) {
                        RotationState state = ROTATION_CACHE.get(pos);
                        if (state.wasPlaying && menu.isPlaying()) {
                            long now = System.currentTimeMillis();
                            float elapsedSeconds = (now - state.timestamp) / 1000.0f;
                            float additionalRotation = elapsedSeconds * 30.0f * state.pitchMultiplier;
                            this.discRotation = (state.rotation + additionalRotation) % 360.0f;
                        }
                    }
                }
            }
        }

        if (currentMode == -1) {
            currentMode = menu.getPlaybackMode().ordinal();
            currentParticles = menu.getShowParticles();
            updateButtonStyles();
        }

        updateButtonStyles();

        if (menu.isPlaying()) {
            long currentTime = System.currentTimeMillis();
            if (lastTickTime > 0) {
                float deltaTime = (currentTime - lastTickTime) / 1000.0f;
                float pitchMultiplier = pitchSlider.getIntValue() / 100.0f;
                discRotation += deltaTime * 30.0f * pitchMultiplier;
                discRotation = discRotation % 360.0f;
            }
            lastTickTime = currentTime;
        } else {
            lastTickTime = 0;
        }

        super.extractRenderState(context, mouseX, mouseY, delta);

        drawButtonTooltips(context, mouseX, mouseY);
    }

    private void drawButtonTooltips(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        if (pauseButton != null && isMouseOverButton(pauseButton, mouseX, mouseY)) {
            context.setTooltipForNextFrame(this.font, Component.literal("Pause"), mouseX, mouseY);
        } else if (playButton != null && isMouseOverButton(playButton, mouseX, mouseY)) {
            context.setTooltipForNextFrame(this.font, Component.literal("Play"), mouseX, mouseY);
        } else if (particleButton != null && isMouseOverButton(particleButton, mouseX, mouseY)) {
            String state = currentParticles ? "ON" : "OFF";
            context.setTooltipForNextFrame(this.font, Component.literal("Particles: " + state), mouseX, mouseY);
        } else if (singleButton != null && isMouseOverButton(singleButton, mouseX, mouseY)) {
            context.setTooltipForNextFrame(this.font, Component.literal("Single Mode"), mouseX, mouseY);
        } else if (loopButton != null && isMouseOverButton(loopButton, mouseX, mouseY)) {
            context.setTooltipForNextFrame(this.font, Component.literal("Loop Mode"), mouseX, mouseY);
        } else if (shuffleButton != null && isMouseOverButton(shuffleButton, mouseX, mouseY)) {
            context.setTooltipForNextFrame(this.font, Component.literal("Shuffle Mode"), mouseX, mouseY);
        }
    }

    private boolean isMouseOverButton(Button button, int mouseX, int mouseY) {
        return mouseX >= button.getX() && mouseX < button.getX() + button.getWidth()
                && mouseY >= button.getY() && mouseY < button.getY() + button.getHeight();
    }

    @Override
    public void onClose() {
        BlockPos pos = menu.getPos();
        if (pos != null) {
            float pitchMult = pitchSlider != null ? pitchSlider.getIntValue() / 100.0f : 1.0f;
            boolean playing = menu.isPlaying();
            ROTATION_CACHE.put(pos, new RotationState(discRotation, System.currentTimeMillis(), pitchMult, playing));
        }

        sendCurrentSettings();
        super.onClose();
    }

    @Override
    public void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTick) {
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0.0f, 0.0f, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);

        drawSpinningDisc(context);

        super.extractContents(context, mouseX, mouseY, partialTick);
    }

    private void drawSpinningDisc(GuiGraphicsExtractor context) {
        ItemStack discStack = menu.getDiscStack();
        if (discStack.isEmpty()) {
            return;
        }

        float scale = 2.5f;

        float centerX = this.leftPos + DISC_WELL_X + DISC_WELL_SIZE / 2.0f - 1.0f;
        float centerY = this.topPos + DISC_WELL_Y + DISC_WELL_SIZE / 2.0f + 1.0f;

        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(centerX, centerY);
        matrices.rotate((float) Math.toRadians(discRotation));
        matrices.scale(scale, scale);
        matrices.translate(0.5f, 0.5f);
        context.item(discStack, -8, -8);
        matrices.popMatrix();
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor context, int mouseX, int mouseY) {
    }

    private void sendCurrentSettings() {
        int modeToSend = currentMode >= 0 ? currentMode : menu.getPlaybackMode().ordinal();
        sendSettings(volumeSlider.getIntValue(), pitchSlider.getIntValue(), rangeSlider.getIntValue(), modeToSend);
    }

    private void sendSettings(int volume, int pitch, int range, int mode) {
        ClientPlayNetworking.send(new SoundscapeNetworking.JukeboxSettingsPayload(
                menu.getPos(), volume, pitch, range, mode
        ));
    }

    private void sendPlaybackCommand(String command) {
        ClientPlayNetworking.send(new SoundscapeNetworking.JukeboxPlaybackPayload(menu.getPos(), command));
    }

    private void sendParticlesSetting(boolean showParticles) {
        ClientPlayNetworking.send(new SoundscapeNetworking.JukeboxParticlesPayload(menu.getPos(), showParticles ? 1 : 0));
    }
}
