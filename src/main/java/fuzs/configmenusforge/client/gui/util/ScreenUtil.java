package fuzs.configmenusforge.client.gui.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import fuzs.configmenusforge.ConfigMenusForge;
import fuzs.configmenusforge.client.gui.widget.AnimatedIconButton;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import joptsimple.internal.Strings;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScreenUtil {
    private static final ResourceLocation TINY_JUMPER_LOCATION = new ResourceLocation(ConfigMenusForge.MOD_ID, "textures/gui/tiny_jumper.png");
    private static final Component INFO_1_TOOLTIP = new TranslatableComponent("configmenusforge.gui.info.1");
    private static final Component INFO_2_TOOLTIP = new TranslatableComponent("configmenusforge.gui.info.2", ConfigMenusForge.MOD_NAME);
    private static final Component INFO_3_TOOLTIP = new TranslatableComponent("configmenusforge.gui.info.3");

    public static ConfirmScreen makeConfirmationScreen(Component component1, Component component2, ResourceLocation background, BooleanConsumer booleanConsumer) {
        // just a confirmation screen with a custom background
        return new ConfirmScreen(booleanConsumer, component1, component2) {

            @Override
            public void renderBackground(PoseStack poseStack, int vOffset) {
                ScreenUtil.renderCustomBackground(this, background, vOffset);
            }
        };
    }

    public static ConfirmScreen makeConfirmationScreen(Component component1, Component component2, Component component3, Component component4, ResourceLocation background, BooleanConsumer booleanConsumer) {
        // just a confirmation screen with a custom background
        return new ConfirmScreen(booleanConsumer, component1, component2, component3, component4) {

            @Override
            public void renderBackground(PoseStack poseStack, int vOffset) {
                ScreenUtil.renderCustomBackground(this, background, vOffset);
            }
        };
    }

    public static AnimatedIconButton makeModPageButton(int posX, int posY, Font font, Consumer<Style> handleComponentClicked, ITooltipRenderer renderTooltip) {
        return new AnimatedIconButton(posX, posY, 20, 20, 0, 0, TINY_JUMPER_LOCATION, button -> {
            Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, ConfigMenusForge.MOD_URL));
            handleComponentClicked.accept(style);
        }, (Button button, PoseStack poseStack, int mouseX, int mouseY) -> {
            final List<FormattedCharSequence> tooltip = Stream.of(INFO_1_TOOLTIP, INFO_2_TOOLTIP, INFO_3_TOOLTIP)
                    .map(line -> font.split(line, 200))
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            renderTooltip.render(poseStack, tooltip, mouseX, mouseY);
        }) {
            @Override
            public void playDownSound(SoundManager manager) {
                manager.play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_AMBIENT, 1.0F));
            }
        }.setAnimationData(4, 3);
    }

    public static void renderCustomBackground(Screen screen, ResourceLocation background, int vOffset) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, background);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        float size = 32.0F;
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        builder.vertex(0.0D, screen.height, 0.0D).uv(0.0F, screen.height / size + vOffset).color(64, 64, 64, 255).endVertex();
        builder.vertex(screen.width, screen.height, 0.0D).uv(screen.width / size, screen.height / size + vOffset).color(64, 64, 64, 255).endVertex();
        builder.vertex(screen.width, 0.0D, 0.0D).uv(screen.width / size, vOffset).color(64, 64, 64, 255).endVertex();
        builder.vertex(0.0D, 0.0D, 0.0D).uv(0.0F, vOffset).color(64, 64, 64, 255).endVertex();
        tesselator.end();
    }

    public static Component formatLabel(String input) {
        return new TextComponent(formatText(input));
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

    public static String getTruncatedText(Font font, String component, int maxWidth) {
        // trim component when too long
        if (font.width(component) > maxWidth) {
            return font.plainSubstrByWidth(component, maxWidth - font.width("...")) + "...";
        } else {
            return component;
        }
    }

    public static FormattedText getTruncatedText(Font font, Component component, int maxWidth, Style style) {
        // trim component when too long
        if (font.width(component) > maxWidth) {
            return FormattedText.composite(font.getSplitter().headByWidth(component, maxWidth - font.width("..."), style), FormattedText.of("..."));
        } else {
            return component;
        }
    }

    @FunctionalInterface
    public interface ITooltipRenderer {

        void render(PoseStack poseStack, List<? extends FormattedCharSequence> tooltips, int mouseX, int mouseY);
    }
}
