package net.cozystudios.soundscape.screen;

import net.cozystudios.soundscape.SoundscapeId;
import net.cozystudios.soundscape.jukebox.PlaybackMode;
import net.cozystudios.soundscape.network.SoundscapeNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

//? if <1.21.11 {
import net.minecraft.util.math.RotationAxis;
//?}

//? if >=1.20.5 {
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
//?} else {
/*import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
*///?}

@Environment(EnvType.CLIENT)
public class JukeboxScreen extends HandledScreen<JukeboxScreenHandler> {

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

    private static final int QUEUE_Y = 154;

    private static final int PLAYER_INV_Y = 194;
    private static final int HOTBAR_Y = 254;

    private SliderData volumeSlider;
    private SliderData pitchSlider;
    private SliderData rangeSlider;

    private ButtonWidget pauseButton;
    private ButtonWidget playButton;
    private ButtonWidget particleButton;
    private ButtonWidget singleButton;
    private ButtonWidget loopButton;
    private ButtonWidget shuffleButton;

    private boolean initialSyncDone = false;
    private int syncAttempts = 0;
    private int currentMode = -1;
    private boolean currentParticles = true;
    private float discRotation = 0;
    private long lastTickTime = 0;

    private boolean elapsedRotationApplied = false;

    public JukeboxScreen(JukeboxScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = GUI_WIDTH;
        this.backgroundHeight = GUI_HEIGHT;

        BlockPos pos = handler.getPos();
        if (pos != null && ROTATION_CACHE.containsKey(pos)) {
            RotationState state = ROTATION_CACHE.get(pos);
            this.discRotation = state.rotation;
        }
    }

    @Override
    protected void init() {
        super.init();

        this.titleY = 5;
        this.playerInventoryTitleY = PLAYER_INV_Y - 11;

        int guiLeft = this.x;
        int guiTop = this.y;

        this.volumeSlider = new SliderData(
                guiLeft + SLIDER_X,
                guiTop + CONTROL_Y + 4,
                SLIDER_WIDTH,
                SLIDER_HEIGHT,
                100,
                0, 100,
                "V", "Volume", "%"
        );

        this.pitchSlider = new SliderData(
                guiLeft + SLIDER_X,
                guiTop + CONTROL_Y + 4 + SLIDER_ROW_SPACING,
                SLIDER_WIDTH,
                SLIDER_HEIGHT,
                100,
                50, 200,
                "P", "Pitch", "x"
        );

        this.rangeSlider = new SliderData(
                guiLeft + SLIDER_X,
                guiTop + CONTROL_Y + 4 + SLIDER_ROW_SPACING * 2,
                SLIDER_WIDTH,
                SLIDER_HEIGHT,
                64,
                0, 128,
                "R", "Range", ""
        );

        this.pauseButton = ButtonWidget.builder(
                Text.literal("\u23F8"),
                button -> sendPlaybackCommand("pause")
        ).dimensions(guiLeft + LEFT_BTN_X, guiTop + BTN_ROW_1_Y, BUTTON_SIZE, BUTTON_SIZE).build();
        this.addDrawableChild(pauseButton);

        this.playButton = ButtonWidget.builder(
                Text.literal("\u25B6"),
                button -> sendPlaybackCommand("play")
        ).dimensions(guiLeft + LEFT_BTN_X, guiTop + BTN_ROW_2_Y, BUTTON_SIZE, BUTTON_SIZE).build();
        this.addDrawableChild(playButton);

        this.particleButton = ButtonWidget.builder(
                Text.literal("\u2726"),
                button -> {
                    currentParticles = !currentParticles;
                    sendParticlesSetting(currentParticles);
                    updateButtonStyles();
                }
        ).dimensions(guiLeft + LEFT_BTN_X, guiTop + BTN_ROW_3_Y, BUTTON_SIZE, BUTTON_SIZE).build();
        this.addDrawableChild(particleButton);

        this.singleButton = ButtonWidget.builder(
                Text.literal("1"),
                button -> {
                    currentMode = PlaybackMode.SINGLE.ordinal();
                    updateButtonStyles();
                    sendCurrentSettings();
                }
        ).dimensions(guiLeft + RIGHT_BTN_X, guiTop + BTN_ROW_1_Y, BUTTON_SIZE, BUTTON_SIZE).build();
        this.addDrawableChild(singleButton);

        this.loopButton = ButtonWidget.builder(
                Text.literal("\uD83D\uDD01"),
                button -> {
                    currentMode = PlaybackMode.LOOP.ordinal();
                    updateButtonStyles();
                    sendCurrentSettings();
                }
        ).dimensions(guiLeft + RIGHT_BTN_X, guiTop + BTN_ROW_2_Y, BUTTON_SIZE, BUTTON_SIZE).build();
        this.addDrawableChild(loopButton);

        this.shuffleButton = ButtonWidget.builder(
                Text.literal("\uD83D\uDD00"),
                button -> {
                    currentMode = PlaybackMode.SHUFFLE.ordinal();
                    updateButtonStyles();
                    sendCurrentSettings();
                }
        ).dimensions(guiLeft + RIGHT_BTN_X, guiTop + BTN_ROW_3_Y, BUTTON_SIZE, BUTTON_SIZE).build();
        this.addDrawableChild(shuffleButton);

        handler.setClientShuffleModeOverride(true);

        updateButtonStyles();
    }

    private void updateButtonStyles() {
        int mode = currentMode >= 0 ? currentMode : 0;
        boolean isPlaying = handler.isPlaying();

        if (pauseButton != null) {
            pauseButton.active = isPlaying;
        }
        if (playButton != null) {
            playButton.active = !isPlaying;
        }

        if (singleButton != null) {
            singleButton.active = mode != PlaybackMode.SINGLE.ordinal();
        }
        if (loopButton != null) {
            loopButton.active = mode != PlaybackMode.LOOP.ordinal();
        }
        if (shuffleButton != null) {
            shuffleButton.active = mode != PlaybackMode.SHUFFLE.ordinal();
        }

        if (particleButton != null) {
            particleButton.setMessage(Text.literal(currentParticles ? "\u2726" : "\u2727"));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        //? if <1.20.5 {
        /*this.renderBackground(context);
        *///?}

        if (!initialSyncDone) {
            int serverVolume = handler.getVolume();
            int serverPitch = handler.getPitch();
            int serverRange = handler.getRange();
            int serverMode = handler.getPlaybackMode().ordinal();
            boolean serverParticles = handler.getShowParticles();
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
                    BlockPos pos = handler.getPos();
                    if (pos != null && ROTATION_CACHE.containsKey(pos)) {
                        RotationState state = ROTATION_CACHE.get(pos);
                        if (state.wasPlaying && handler.isPlaying()) {
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
            currentMode = handler.getPlaybackMode().ordinal();
            currentParticles = handler.getShowParticles();
            updateButtonStyles();
        }

        updateButtonStyles();

        if (handler.isPlaying()) {
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

        handleSliderInput(mouseX, mouseY);

        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);

        drawSliderTooltips(context, mouseX, mouseY);

        drawButtonTooltips(context, mouseX, mouseY);

    }

    private void drawSliderTooltips(DrawContext context, int mouseX, int mouseY) {
        SliderData[] sliders = {volumeSlider, pitchSlider, rangeSlider};
        for (SliderData slider : sliders) {
            if (slider != null && slider.isInBounds(mouseX, mouseY)) {
                context.drawTooltip(this.textRenderer,
                        Text.literal(slider.fullName + ": " + slider.getDisplayValue()),
                        mouseX, mouseY);
                break;
            }
        }
    }

    private void drawButtonTooltips(DrawContext context, int mouseX, int mouseY) {
        if (pauseButton != null && isMouseOverButton(pauseButton, mouseX, mouseY)) {
            context.drawTooltip(this.textRenderer, Text.literal("Pause"), mouseX, mouseY);
        } else if (playButton != null && isMouseOverButton(playButton, mouseX, mouseY)) {
            context.drawTooltip(this.textRenderer, Text.literal("Play"), mouseX, mouseY);
        } else if (particleButton != null && isMouseOverButton(particleButton, mouseX, mouseY)) {
            String state = currentParticles ? "ON" : "OFF";
            context.drawTooltip(this.textRenderer, Text.literal("Particles: " + state), mouseX, mouseY);
        } else if (singleButton != null && isMouseOverButton(singleButton, mouseX, mouseY)) {
            context.drawTooltip(this.textRenderer, Text.literal("Single Mode"), mouseX, mouseY);
        } else if (loopButton != null && isMouseOverButton(loopButton, mouseX, mouseY)) {
            context.drawTooltip(this.textRenderer, Text.literal("Loop Mode"), mouseX, mouseY);
        } else if (shuffleButton != null && isMouseOverButton(shuffleButton, mouseX, mouseY)) {
            context.drawTooltip(this.textRenderer, Text.literal("Shuffle Mode"), mouseX, mouseY);
        }
    }

    private boolean isMouseOverButton(ButtonWidget button, int mouseX, int mouseY) {
        return mouseX >= button.getX() && mouseX < button.getX() + button.getWidth()
                && mouseY >= button.getY() && mouseY < button.getY() + button.getHeight();
    }

    private void handleSliderInput(int mouseX, int mouseY) {
        long windowHandle = net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle();
        boolean mouseDown = GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        SliderData[] sliders = {volumeSlider, pitchSlider, rangeSlider};

        if (mouseDown) {
            if (draggingSliderIndex == -1) {
                for (int i = 0; i < sliders.length; i++) {
                    if (sliders[i] != null && sliders[i].isInBounds(mouseX, mouseY)) {
                        draggingSliderIndex = i;
                        sliders[i].setValueFromMouse(mouseX);
                        sendCurrentSettings();
                        break;
                    }
                }
            } else if (draggingSliderIndex >= 0 && draggingSliderIndex < sliders.length) {
                sliders[draggingSliderIndex].setValueFromMouse(mouseX);
                sendCurrentSettings();
            }
            wasMouseDown = true;
        } else {
            if (wasMouseDown && draggingSliderIndex != -1) {
                draggingSliderIndex = -1;
            }
            wasMouseDown = false;
        }
    }

    @Override
    public void close() {
        BlockPos pos = handler.getPos();
        if (pos != null) {
            float pitchMult = pitchSlider != null ? pitchSlider.getIntValue() / 100.0f : 1.0f;
            boolean playing = handler.isPlaying();
            ROTATION_CACHE.put(pos, new RotationState(discRotation, System.currentTimeMillis(), pitchMult, playing));
        }

        sendCurrentSettings();
        super.close();
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        //? if 1.21.11 {
        /*context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, TEXTURE, this.x, this.y, 0.0f, 0.0f, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);
        *///?} elif >=1.21.4 {
        /*context.drawTexture(net.minecraft.client.render.RenderLayer::getGuiTextured, TEXTURE, this.x, this.y, 0.0f, 0.0f, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);
        *///?} elif >=1.21 {
        context.drawTexture(TEXTURE, this.x, this.y, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);
        //?} else {
        /*context.drawTexture(TEXTURE, this.x, this.y, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);
        *///?}

        drawSpinningDisc(context);

        if (volumeSlider != null) volumeSlider.render(context, mouseX, mouseY);
        if (pitchSlider != null) pitchSlider.render(context, mouseX, mouseY);
        if (rangeSlider != null) rangeSlider.render(context, mouseX, mouseY);
    }

    private void drawSpinningDisc(DrawContext context) {
        ItemStack discStack = handler.getDiscStack();
        if (discStack.isEmpty()) {
            return;
        }

        float scale = 2.5f;

        float centerX = this.x + DISC_WELL_X + DISC_WELL_SIZE / 2.0f - 1.0f;
        float centerY = this.y + DISC_WELL_Y + DISC_WELL_SIZE / 2.0f + 1.0f;

        var matrices = context.getMatrices();
        //? if 1.21.11 {
        /*matrices.pushMatrix();
        matrices.translate(centerX, centerY);
        matrices.rotate((float) Math.toRadians(discRotation));
        matrices.scale(scale, scale);
        matrices.translate(0.5f, 0.5f);
        context.drawItem(discStack, -8, -8);
        matrices.popMatrix();
        *///?} else {
        matrices.push();
        matrices.translate(centerX, centerY, 0);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(discRotation));
        matrices.scale(scale, scale, 1.0f);
        matrices.translate(0.5f, 0.5f, 0);
        context.drawItem(discStack, -8, -8);
        matrices.pop();
        //?}
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
    }

    private int draggingSliderIndex = -1;
    private boolean wasMouseDown = false;

    private void sendCurrentSettings() {
        int modeToSend = currentMode >= 0 ? currentMode : handler.getPlaybackMode().ordinal();
        sendSettings(volumeSlider.getIntValue(), pitchSlider.getIntValue(), rangeSlider.getIntValue(), modeToSend);
    }

    private void sendSettings(int volume, int pitch, int range, int mode) {
        //? if >=1.20.5 {
        ClientPlayNetworking.send(new SoundscapeNetworking.JukeboxSettingsPayload(
                handler.getPos(), volume, pitch, range, mode
        ));
        //?} else {
        /*PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(handler.getPos());
        buf.writeInt(volume);
        buf.writeInt(pitch);
        buf.writeInt(range);
        buf.writeInt(mode);
        ClientPlayNetworking.send(SoundscapeNetworking.JUKEBOX_SETTINGS_C2S, buf);
        *///?}
    }

    private void sendPlaybackCommand(String command) {
        //? if >=1.20.5 {
        ClientPlayNetworking.send(new SoundscapeNetworking.JukeboxPlaybackPayload(handler.getPos(), command));
        //?} else {
        /*PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(handler.getPos());
        buf.writeString(command);
        ClientPlayNetworking.send(SoundscapeNetworking.JUKEBOX_PLAYBACK_C2S, buf);
        *///?}
    }

    private void sendParticlesSetting(boolean showParticles) {
        //? if >=1.20.5 {
        ClientPlayNetworking.send(new SoundscapeNetworking.JukeboxParticlesPayload(handler.getPos(), showParticles ? 1 : 0));
        //?} else {
        /*PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(handler.getPos());
        buf.writeBoolean(showParticles);
        ClientPlayNetworking.send(SoundscapeNetworking.JUKEBOX_PARTICLES_C2S, buf);
        *///?}
    }

    private static class SliderData {
        final int x, y, width, height;
        final int minValue, maxValue;
        final String shortLabel;
        final String fullName;
        final String unit;
        double value;

        SliderData(int x, int y, int width, int height, int initialValue, int min, int max, String shortLabel, String fullName, String unit) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.minValue = min;
            this.maxValue = max;
            this.shortLabel = shortLabel;
            this.fullName = fullName;
            this.unit = unit;
            this.value = (double)(initialValue - min) / (max - min);
        }

        int getIntValue() {
            int raw = (int) (this.value * (maxValue - minValue)) + minValue;
            if (unit.equals("x") || unit.equals("%")) {
                raw = Math.round(raw / 5.0f) * 5;
            }
            return raw;
        }

        void setValueFromServer(int val) {
            this.value = (double)(val - minValue) / (maxValue - minValue);
        }

        void setValueFromMouse(double mouseX) {
            double newValue = (mouseX - this.x - 5) / (this.width - 10);
            this.value = MathHelper.clamp(newValue, 0.0, 1.0);

            if (unit.equals("x") || unit.equals("%")) {
                int raw = (int) (this.value * (maxValue - minValue)) + minValue;
                int snapped = Math.round(raw / 5.0f) * 5;
                snapped = MathHelper.clamp(snapped, minValue, maxValue);
                this.value = (double)(snapped - minValue) / (maxValue - minValue);
            }
        }

        boolean isInBounds(double mouseX, double mouseY) {
            return mouseX >= this.x && mouseX < this.x + this.width
                    && mouseY >= this.y && mouseY < this.y + this.height;
        }

        String getDisplayValue() {
            int val = getIntValue();
            if (unit.equals("x")) {
                return String.format("%.2f%s", val / 100.0, unit);
            } else {
                return val + unit;
            }
        }

        void render(DrawContext context, int mouseX, int mouseY) {
            int trackX = this.x;
            int trackY = this.y + 2;
            int trackW = this.width;
            int trackH = 10;

            int fillWidth = (int)(this.value * (trackW - 4));
            int fillColor = unit.equals("x") ? 0xFF4488CC : (unit.equals("%") ? 0xFF44AA44 : 0xFFAA8844);
            context.fill(trackX + 2, trackY + 3, trackX + 2 + fillWidth, trackY + 7, fillColor);

            int handleX = trackX + (int)(this.value * (trackW - 10));
            int handleY = this.y;
            int handleW = 10;
            int handleH = this.height;

            context.fill(handleX + 1, handleY + 1, handleX + handleW + 1, handleY + handleH + 1, 0x40000000);

            boolean hovered = isInBounds(mouseX, mouseY);
            int handleColor = hovered ? 0xFFEEEEEE : 0xFFCCCCCC;
            int handleHighlight = hovered ? 0xFFFFFFFF : 0xFFDDDDDD;
            int handleShadow = hovered ? 0xFFAAAAAA : 0xFF999999;

            context.fill(handleX, handleY, handleX + handleW, handleY + handleH, handleShadow);
            context.fill(handleX, handleY, handleX + handleW - 1, handleY + handleH - 1, handleColor);
            context.fill(handleX + 1, handleY + 1, handleX + handleW - 1, handleY + 2, handleHighlight);
            context.fill(handleX + 1, handleY + 1, handleX + 2, handleY + handleH - 2, handleHighlight);

            context.fill(handleX + 3, handleY + 3, handleX + 4, handleY + handleH - 3, handleShadow);
            context.fill(handleX + 5, handleY + 3, handleX + 6, handleY + handleH - 3, handleShadow);
            context.fill(handleX + 7, handleY + 3, handleX + 8, handleY + handleH - 3, handleShadow);
        }
    }
}
