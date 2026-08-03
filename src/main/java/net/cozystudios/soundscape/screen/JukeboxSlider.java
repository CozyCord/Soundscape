package net.cozystudios.soundscape.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;

@Environment(EnvType.CLIENT)
public class JukeboxSlider extends AbstractSliderButton {

    private final int minValue;
    private final int maxValue;
    private final String fullName;
    private final String unit;
    private final IntConsumer onValueChanged;

    public JukeboxSlider(int x, int y, int width, int height,
                         int initialValue, int min, int max,
                         String fullName, String unit,
                         IntConsumer onValueChanged) {
        super(x, y, width, height,
                Component.literal(fullName + ": " + formatValue(initialValue, unit)),
                (double)(initialValue - min) / (max - min));
        this.minValue = min;
        this.maxValue = max;
        this.fullName = fullName;
        this.unit = unit;
        this.onValueChanged = onValueChanged;
    }

    public int getIntValue() {
        int raw = (int) Math.round(this.value * (maxValue - minValue)) + minValue;
        if (unit.equals("x") || unit.equals("%")) {
            raw = Math.round(raw / 5.0f) * 5;
        }
        if (raw < minValue) raw = minValue;
        if (raw > maxValue) raw = maxValue;
        return raw;
    }

    public void setValueFromServer(int val) {
        this.value = (double)(val - minValue) / (maxValue - minValue);
        updateMessage();
    }

    private static String formatValue(int val, String unit) {
        if (unit.equals("x")) {
            return String.format("%.2f%s", val / 100.0, unit);
        }
        return val + unit;
    }

    public String getDisplayValue() {
        return formatValue(getIntValue(), unit);
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    protected void updateMessage() {
        setMessage(Component.literal(fullName + ": " + getDisplayValue()));
    }

    @Override
    protected void applyValue() {
        onValueChanged.accept(getIntValue());
    }
}
