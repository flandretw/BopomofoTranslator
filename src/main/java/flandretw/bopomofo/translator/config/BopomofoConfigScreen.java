package flandretw.bopomofo.translator.config;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BopomofoConfigScreen extends Screen {
    private final Screen parent;
    private final BopomofoConfig config;

    public BopomofoConfigScreen(Screen parent) {
        super(Text.translatable("bopomofo.config.title"));
        this.parent = parent;
        this.config = BopomofoConfig.getInstance();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int topY = 40;
        int buttonWidth = 200;
        int buttonHeight = 20;

        // 顏色切換按鈕
        this.addDrawableChild(ButtonWidget.builder(getColorText(), button -> {
            int next = config.textColor.getColorIndex() + 1;
            while (true) {
                if (next > 15)
                    next = 0;
                Formatting f = Formatting.byColorIndex(next);
                if (f != null && f.isColor()) {
                    config.textColor = f;
                    break;
                }
                next++;
            }
            button.setMessage(getColorText());
        }).dimensions(centerX - buttonWidth / 2, topY, buttonWidth, buttonHeight).build());

        // 粗體開關
        this.addDrawableChild(ButtonWidget.builder(getBoolText("bopomofo.config.bold", config.bold), button -> {
            config.bold = !config.bold;
            button.setMessage(getBoolText("bopomofo.config.bold", config.bold));
        }).dimensions(centerX - buttonWidth / 2, topY + 24, buttonWidth, buttonHeight).build());

        // 斜體開關
        this.addDrawableChild(ButtonWidget.builder(getBoolText("bopomofo.config.italic", config.italic), button -> {
            config.italic = !config.italic;
            button.setMessage(getBoolText("bopomofo.config.italic", config.italic));
        }).dimensions(centerX - buttonWidth / 2, topY + 48, buttonWidth, buttonHeight).build());

        // 底線開關
        this.addDrawableChild(
                ButtonWidget.builder(getBoolText("bopomofo.config.underline", config.underline), button -> {
                    config.underline = !config.underline;
                    button.setMessage(getBoolText("bopomofo.config.underline", config.underline));
                }).dimensions(centerX - buttonWidth / 2, topY + 72, buttonWidth, buttonHeight).build());

        // 確定按鈕
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> {
            config.save();
            this.client.setScreen(this.parent);
        }).dimensions(centerX - buttonWidth / 2, this.height - 30, buttonWidth, buttonHeight).build());
    }

    private Text getColorText() {
        String name = config.textColor.getName();
        Text colorName = Text.literal(name.substring(0, 1).toUpperCase() + name.substring(1))
                .formatted(config.textColor);
        return Text.translatable("bopomofo.config.color").append(": ").append(colorName);
    }

    private Text getBoolText(String key, boolean value) {
        return Text.translatable(key).append(": ").append(ScreenTexts.onOrOff(value));
    }

    @Override
    public void close() {
        config.save();
        this.client.setScreen(this.parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("bopomofo.config.warning"), this.width / 2, this.height - 50, 0xFFFF5555);
    }
}
