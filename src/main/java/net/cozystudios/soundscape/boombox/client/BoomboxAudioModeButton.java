package net.cozystudios.soundscape.boombox.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

// Text-only toggle drawn as a label on the boombox housing, right-aligned inside its bounds
@Environment(EnvType.CLIENT)
public class BoomboxAudioModeButton extends ClickableWidget {

    private static final float TEXT_SCALE = 2.0f;
    private static final int COLOR_NORMAL = 0xFFDDDDDD;
    private static final int COLOR_HOVER = 0xFFFFFFFF;

    public BoomboxAudioModeButton(int x, int y, int width, int height) {
        super(x, y, width, height, label());
        setTooltip(Tooltip.of(Text.literal(
                "3D: positional audio, panned by where you look.\n"
                + "Flat: no panning, volume depends only on distance.\n"
                + "Applies to all Boomboxes on this client.")));
    }

    private static Text label() {
        return Text.literal(BoomboxClientConfig.getAudioMode().getLabel().toUpperCase());
    }

    private void toggle() {
        BoomboxClientConfig.setAudioMode(BoomboxClientConfig.getAudioMode().next());
        setMessage(label());
    }

    //? if 1.21.11 {
    /*@Override
    public void onClick(net.minecraft.client.gui.Click click, boolean doubled) {
        toggle();
    }
    *///?} else {
    @Override
    public void onClick(double mouseX, double mouseY) {
        toggle();
    }
    //?}

    //? if >=1.20.2 {
    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        drawLabel(context);
    }
    //?} else {
    /*@Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        drawLabel(context);
    }
    *///?}

    private void drawLabel(DrawContext context) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        Text text = getMessage();
        int color = isHovered() ? COLOR_HOVER : COLOR_NORMAL;

        float textWidth = textRenderer.getWidth(text) * TEXT_SCALE;
        float textHeight = textRenderer.fontHeight * TEXT_SCALE;
        float drawX = getX() + getWidth() - textWidth;
        float drawY = getY() + (getHeight() - textHeight) / 2.0f;

        var matrices = context.getMatrices();
        //? if 1.21.11 {
        /*matrices.pushMatrix();
        matrices.translate(drawX, drawY);
        matrices.scale(TEXT_SCALE, TEXT_SCALE);
        context.drawTextWithShadow(textRenderer, text, 0, 0, color);
        matrices.popMatrix();
        *///?} else {
        matrices.push();
        matrices.translate(drawX, drawY, 0);
        matrices.scale(TEXT_SCALE, TEXT_SCALE, 1.0f);
        context.drawTextWithShadow(textRenderer, text, 0, 0, color);
        matrices.pop();
        //?}
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        appendDefaultNarrations(builder);
    }
}
