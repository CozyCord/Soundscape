package net.cozystudios.soundscape.boombox.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class BoomboxVolumeSlider extends SliderWidget {

    private final Consumer<Double> onValueChanged;

    public BoomboxVolumeSlider(int x, int y, int width, int height, double value, Consumer<Double> onValueChanged) {
        super(x, y, width, height, Text.literal("Volume: " + (int)(value * 100) + "%"), value);
        this.onValueChanged = onValueChanged;
    }

    @Override
    protected void updateMessage() {
        setMessage(Text.literal("Volume: " + (int)(this.value * 100) + "%"));
    }

    @Override
    protected void applyValue() {
        onValueChanged.accept(this.value);
    }

    /**
     * Updates slider position without triggering the callback.
     * Named differently from setValue to avoid overriding SliderWidget's internal setValue.
     */
    public void setValueFromServer(double value) {
        this.value = Math.max(0.0, Math.min(1.0, value));
        updateMessage();
    }
}
