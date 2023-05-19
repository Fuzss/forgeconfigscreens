package fuzs.forgeconfigscreens.client.gui.helper;

import joptsimple.internal.Strings;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.StringUtils;

public class ScreenTextHelper {

    public static Component toFormattedComponent(String input) {
        return Component.literal(toFormattedString(input));
    }

    /**
     * Tries to create a readable label from the given input. This input should be
     * the raw config value name. For example "shouldShowParticles" will be converted
     * to "Should Show Particles".
     *
     * @param input the config value name
     * @return a readable label string
     */
    public static String toFormattedString(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        // Try split by camel case
        String[] words = input.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
        for (int i = 0; i < words.length; i++) words[i] = StringUtils.capitalize(words[i]);
        input = Strings.join(words, " ");
        // Try split by underscores
        words = input.split("_");
        for (int i = 0; i < words.length; i++) words[i] = StringUtils.capitalize(words[i]);
        // Finally join words. Some mods have inputs like "Foo_Bar" and this causes a double space.
        // To fix this any whitespace is replaced with a single space
        return Strings.join(words, " ").replaceAll("\\s++", " ");
    }

    public static FormattedText truncateText(Font font, Component component, int maxWidth, Style style) {
        // trim component when too long
        if (font.width(component) > maxWidth) {
            return FormattedText.composite(font.getSplitter().headByWidth(component, maxWidth - font.width(CommonComponents.ELLIPSIS), style), CommonComponents.ELLIPSIS);
        } else {
            return component;
        }
    }
}
