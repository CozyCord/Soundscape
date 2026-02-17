package net.cozystudios.soundscape.boombox.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class BoomboxRangeSlider extends SliderWidget {

    private static final int MIN_RANGE = 8;
    private static final int MAX_RANGE = 128;

    private final Consumer<Integer> onValueChanged;

    public BoomboxRangeSlider(int x, int y, int width, int height, int range, Consumer<Integer> onValueChanged) {
        super(x, y, width, height,
                Text.literal("Range: " + range + " blocks"),
                (double)(range - MIN_RANGE) / (MAX_RANGE - MIN_RANGE));
        this.onValueChanged = onValueChanged;
    }

    @Override
    protected void updateMessage() {
        setMessage(Text.literal("Range: " + getRangeValue() + " blocks"));
    }

    @Override
    protected void applyValue() {
        onValueChanged.accept(getRangeValue());
    }

    private int getRangeValue() {
        return MIN_RANGE + (int)(this.value * (MAX_RANGE - MIN_RANGE));
    }

    public void setRange(int range) {
        this.value = (double)(range - MIN_RANGE) / (MAX_RANGE - MIN_RANGE);
        updateMessage();
    }
}
