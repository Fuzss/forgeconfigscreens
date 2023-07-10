package fuzs.forgeconfigscreens.client.gui.helper;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.resources.ResourceLocation;

public class PanoramaBackgroundHelper {
    private static final ResourceLocation PANORAMA_OVERLAY = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
    private static final PanoramaRenderer PANORAMA_RENDERER = new PanoramaRenderer(TitleScreen.CUBE_MAP);

    public static void renderDirtBackground(GuiGraphics guiGraphics, int width, int height) {
        Minecraft minecraft = Minecraft.getInstance();
        PANORAMA_RENDERER.render(minecraft.getDeltaFrameTime(), 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(PANORAMA_OVERLAY, 0, 0, width, height, 0.0F, 0.0F, 16, 128, 16, 128);
    }
}
