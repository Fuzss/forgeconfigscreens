package fuzs.configmenusforge.client.gui.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import joptsimple.internal.Strings;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import org.apache.commons.lang3.StringUtils;

public class ScreenUtil {

    public static ConfirmScreen makeConfirmationScreen(BooleanConsumer booleanConsumer, ITextComponent component1, ITextComponent component2, ResourceLocation background) {
        // just a confirmation screen with a custom background
        return new ConfirmScreen(booleanConsumer, component1, component2) {
            @Override
            public void renderBackground(MatrixStack poseStack, int vOffset) {
                ScreenUtil.renderCustomBackground(this, background, vOffset);
            }
        };
    }

    public static ConfirmScreen makeConfirmationScreen(BooleanConsumer booleanConsumer, ITextComponent component1, ITextComponent component2, ITextComponent component3, ITextComponent component4, ResourceLocation background) {
        // just a confirmation screen with a custom background
        return new ConfirmScreen(booleanConsumer, component1, component2, component3, component4) {
            @Override
            public void renderBackground(MatrixStack poseStack, int vOffset) {
                ScreenUtil.renderCustomBackground(this, background, vOffset);
            }
        };
    }

    public static void renderCustomBackground(Screen screen, ResourceLocation background, int vOffset) {
        Tessellator tesselator = Tessellator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        screen.getMinecraft().getTextureManager().bind(background);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        float size = 32.0F;
        builder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        builder.vertex(0.0D, screen.height, 0.0D).uv(0.0F, screen.height / size + vOffset).color(64, 64, 64, 255).endVertex();
        builder.vertex(screen.width, screen.height, 0.0D).uv(screen.width / size, screen.height / size + vOffset).color(64, 64, 64, 255).endVertex();
        builder.vertex(screen.width, 0.0D, 0.0D).uv(screen.width / size, vOffset).color(64, 64, 64, 255).endVertex();
        builder.vertex(0.0D, 0.0D, 0.0D).uv(0.0F, vOffset).color(64, 64, 64, 255).endVertex();
        tesselator.end();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(screen, new MatrixStack()));
    }

    public static ITextComponent formatLabel(String input) {
        return new StringTextComponent(formatText(input));
    }

    /**
     * Tries to create a readable label from the given input. This input should be
     * the raw config value name. For example "shouldShowParticles" will be converted
     * to "Should Show Particles".
     *
     * @param input the config value name
     * @return a readable label string
     */
    public static String formatText(String input) {
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

    public static String getTruncatedText(FontRenderer font, String component, int maxWidth) {
        // trim component when too long
        if (font.width(component) > maxWidth) {
            return font.plainSubstrByWidth(component, maxWidth - font.width(". . .")) + ". . .";
        } else {
            return component;
        }
    }

    public static ITextProperties getTruncatedText(FontRenderer font, ITextComponent component, int maxWidth, Style style) {
        // trim component when too long
        if (font.width(component) > maxWidth) {
            return ITextProperties.composite(font.getSplitter().headByWidth(component, maxWidth - font.width(". . ."), style), ITextProperties.of(". . ."));
        } else {
            return component;
        }
    }
}
