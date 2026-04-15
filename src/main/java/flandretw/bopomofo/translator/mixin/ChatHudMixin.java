package flandretw.bopomofo.translator.mixin;

import flandretw.bopomofo.translator.BopomofoConverter;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatHud.class)
public class ChatHudMixin {

    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Text modifyChatMessage(Text originalMessage) {
        if (originalMessage == null)
            return null;
        Text modified = processText(originalMessage);
        return modified != null ? modified : originalMessage;
    }

    private Text processText(Text text) {
        boolean changed = false;

        MutableText newText = text.copyContentOnly();
        Style newStyle = text.getStyle();

        net.minecraft.text.TextContent content = text.getContent();
        if (content instanceof net.minecraft.text.PlainTextContent plain) {
            String literal = plain.string();
            if (!literal.isEmpty()) {
                BopomofoConverter.BopomofoResult result = BopomofoConverter.convert(literal);
                if (result.changed) {
                    changed = true;
                    newText = Text.empty(); // Discard the original single text body
                    for (BopomofoConverter.Segment seg : result.segments) {
                        MutableText segText = Text.literal(seg.original);
                        if (seg.translated != null) {
                            flandretw.bopomofo.translator.config.BopomofoConfig config = flandretw.bopomofo.translator.config.BopomofoConfig
                                    .getInstance();
                            MutableText translatedText = Text.literal(seg.translated).formatted(config.textColor);
                            if (config.bold)
                                translatedText.formatted(net.minecraft.util.Formatting.BOLD);
                            if (config.italic)
                                translatedText.formatted(net.minecraft.util.Formatting.ITALIC);
                            if (config.underline)
                                translatedText.formatted(net.minecraft.util.Formatting.UNDERLINE);

                            segText.setStyle(newStyle.withHoverEvent(new HoverEvent.ShowText(translatedText)));
                        } else {
                            segText.setStyle(newStyle);
                        }
                        newText.append(segText);
                    }
                    newStyle = Style.EMPTY;
                }
            }
        } else if (content instanceof net.minecraft.text.TranslatableTextContent translatable) {
            Object[] args = translatable.getArgs();
            Object[] newArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg instanceof Text argText) {
                    Text newArg = processText(argText);
                    if (newArg != argText)
                        changed = true;
                    newArgs[i] = newArg;
                } else if (arg instanceof String argStr) {
                    Text textArg = Text.literal(argStr);
                    Text newArg = processText(textArg);
                    if (newArg != textArg)
                        changed = true;
                    newArgs[i] = (newArg != textArg) ? newArg : argStr;
                } else {
                    newArgs[i] = arg;
                }
            }
            if (changed) {
                if (translatable.getFallback() != null) {
                    newText = Text.translatableWithFallback(translatable.getKey(), translatable.getFallback(), newArgs);
                } else {
                    newText = Text.translatable(translatable.getKey(), newArgs);
                }
            }
        }

        newText.setStyle(newStyle);

        for (Text sibling : text.getSiblings()) {
            Text processedSibling = processText(sibling);
            if (processedSibling != sibling) {
                changed = true;
            }
            newText.append(processedSibling);
        }

        return changed ? newText : text;
    }
}
