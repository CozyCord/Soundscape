package net.cozystudios.soundscape.registry;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.List;

//? if >=1.21.5 {
/*import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.component.type.TooltipDisplayComponent;
import java.util.function.Consumer;

public class AmbienceDiscItem extends Item {
    private final String categoryKey;

    public AmbienceDiscItem(String categoryKey, Item.Settings settings) {
        super(settings);
        this.categoryKey = categoryKey;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, tooltip, type);
        tooltip.accept(Text.translatable(categoryKey).formatted(getCategoryColor(categoryKey)));
    }

    private static Formatting getCategoryColor(String key) {
        return switch (key) {
            case "soundscape.category.nature" -> Formatting.GREEN;
            case "soundscape.category.water" -> Formatting.AQUA;
            case "soundscape.category.weather" -> Formatting.GRAY;
            case "soundscape.category.fire" -> Formatting.RED;
            case "soundscape.category.cityscape" -> Formatting.YELLOW;
            case "soundscape.category.horror" -> Formatting.DARK_PURPLE;
            case "soundscape.category.industrial" -> Formatting.DARK_GRAY;
            case "soundscape.category.interior" -> Formatting.GOLD;
            case "soundscape.category.outdoor" -> Formatting.DARK_GREEN;
            default -> Formatting.GOLD;
        };
    }
}
*///?} elif >=1.21 {
import net.minecraft.item.tooltip.TooltipType;

public class AmbienceDiscItem extends Item {
    private final String categoryKey;

    public AmbienceDiscItem(String categoryKey, Item.Settings settings) {
        super(settings);
        this.categoryKey = categoryKey;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        tooltip.add(Text.translatable(categoryKey).formatted(getCategoryColor(categoryKey)));
    }

    private static Formatting getCategoryColor(String key) {
        return switch (key) {
            case "soundscape.category.nature" -> Formatting.GREEN;
            case "soundscape.category.water" -> Formatting.AQUA;
            case "soundscape.category.weather" -> Formatting.GRAY;
            case "soundscape.category.fire" -> Formatting.RED;
            case "soundscape.category.cityscape" -> Formatting.YELLOW;
            case "soundscape.category.horror" -> Formatting.DARK_PURPLE;
            case "soundscape.category.industrial" -> Formatting.DARK_GRAY;
            case "soundscape.category.interior" -> Formatting.GOLD;
            case "soundscape.category.outdoor" -> Formatting.DARK_GREEN;
            default -> Formatting.GOLD;
        };
    }
}
//?} else {
/*import net.minecraft.item.MusicDiscItem;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import net.minecraft.client.item.TooltipContext;
import org.jetbrains.annotations.Nullable;

public class AmbienceDiscItem extends MusicDiscItem {
    private final String categoryKey;

    public AmbienceDiscItem(String categoryKey, int comparatorOutput, SoundEvent sound, Item.Settings settings, int lengthInSeconds) {
        super(comparatorOutput, sound, settings, lengthInSeconds);
        this.categoryKey = categoryKey;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable(categoryKey).formatted(getCategoryColor(categoryKey)));
    }

    private static Formatting getCategoryColor(String key) {
        return switch (key) {
            case "soundscape.category.nature" -> Formatting.GREEN;
            case "soundscape.category.water" -> Formatting.AQUA;
            case "soundscape.category.weather" -> Formatting.GRAY;
            case "soundscape.category.fire" -> Formatting.RED;
            case "soundscape.category.cityscape" -> Formatting.YELLOW;
            case "soundscape.category.horror" -> Formatting.DARK_PURPLE;
            case "soundscape.category.industrial" -> Formatting.DARK_GRAY;
            case "soundscape.category.interior" -> Formatting.GOLD;
            case "soundscape.category.outdoor" -> Formatting.DARK_GREEN;
            default -> Formatting.GOLD;
        };
    }
}
*///?}
