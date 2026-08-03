package net.cozystudios.soundscape.boombox.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class BoomboxVolumeSlider extends AbstractSliderButton {

    private final Consumer<Double> onValueChanged;

    public BoomboxVolumeSlider(int x, int y, int width, int height, double value, Consumer<Double> onValueChanged) {
        super(x, y, width, height, Component.literal("Volume: " + (int)(value * 100) + "%"), value);
        this.onValueChanged = onValueChanged;
    }

    @Override
    protected void updateMessage() {
        setMessage(Component.literal("Volume: " + (int)(this.value * 100) + "%"));
    }

    @Override
    protected void applyValue() {
        onValueChanged.accept(this.value);
    }

    public void setValueFromServer(double value) {
        this.value = Math.max(0.0, Math.min(1.0, value));
        updateMessage();
    }
}
