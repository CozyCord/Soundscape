package net.cozystudios.soundscape.registry;

import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class AmbienceDiscItem extends Item {
    private final String categoryKey;

    public AmbienceDiscItem(String categoryKey, Item.Properties settings) {
        super(settings);
        this.categoryKey = categoryKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, displayComponent, tooltip, type);
        tooltip.accept(Component.translatable(categoryKey).withStyle(style -> style.withColor(getCategoryColor(categoryKey))));
    }

    private static int getCategoryColor(String key) {
        return switch (key) {
            case "soundscape.category.nature" -> 0xFF8db849;
            case "soundscape.category.water" -> 0xFF62a5c9;
            case "soundscape.category.weather" -> 0xFFffe57e;
            case "soundscape.category.fire" -> 0xFFd67502;
            case "soundscape.category.cityscape" -> 0xFF2c2f59;
            case "soundscape.category.horror" -> 0xFF961f1f;
            case "soundscape.category.industrial" -> 0xFFde815f;
            case "soundscape.category.interior" -> 0xFF1a3823;
            case "soundscape.category.outdoor" -> 0xFF9e6749;
            default -> 0xFFFFFFFF;
        };
    }
}
