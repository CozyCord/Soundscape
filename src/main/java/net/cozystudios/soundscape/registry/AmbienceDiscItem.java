package net.cozystudios.soundscape.registry;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
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
        tooltip.accept(Text.translatable(categoryKey).styled(style -> style.withColor(getCategoryColor(categoryKey))));
    }

    private static int getCategoryColor(String key) {
        return switch (key) {
            case "soundscape.category.nature" -> 0x8db849;
            case "soundscape.category.water" -> 0x62a5c9;
            case "soundscape.category.weather" -> 0xffe57e;
            case "soundscape.category.fire" -> 0xd67502;
            case "soundscape.category.cityscape" -> 0x2c2f59;
            case "soundscape.category.horror" -> 0x961f1f;
            case "soundscape.category.industrial" -> 0xde815f;
            case "soundscape.category.interior" -> 0x1a3823;
            case "soundscape.category.outdoor" -> 0x9e6749;
            default -> 0xFFFFFF;
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
        tooltip.add(Text.translatable(categoryKey).styled(style -> style.withColor(getCategoryColor(categoryKey))));
    }

    private static int getCategoryColor(String key) {
        return switch (key) {
            case "soundscape.category.nature" -> 0x8db849;
            case "soundscape.category.water" -> 0x62a5c9;
            case "soundscape.category.weather" -> 0xffe57e;
            case "soundscape.category.fire" -> 0xd67502;
            case "soundscape.category.cityscape" -> 0x2c2f59;
            case "soundscape.category.horror" -> 0x961f1f;
            case "soundscape.category.industrial" -> 0xde815f;
            case "soundscape.category.interior" -> 0x1a3823;
            case "soundscape.category.outdoor" -> 0x9e6749;
            default -> 0xFFFFFF;
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
        tooltip.add(Text.translatable(categoryKey).styled(style -> style.withColor(getCategoryColor(categoryKey))));
    }

    private static int getCategoryColor(String key) {
        return switch (key) {
            case "soundscape.category.nature" -> 0x8db849;
            case "soundscape.category.water" -> 0x62a5c9;
            case "soundscape.category.weather" -> 0xffe57e;
            case "soundscape.category.fire" -> 0xd67502;
            case "soundscape.category.cityscape" -> 0x2c2f59;
            case "soundscape.category.horror" -> 0x961f1f;
            case "soundscape.category.industrial" -> 0xde815f;
            case "soundscape.category.interior" -> 0x1a3823;
            case "soundscape.category.outdoor" -> 0x9e6749;
            default -> 0xFFFFFF;
        };
    }
}
*///?}
