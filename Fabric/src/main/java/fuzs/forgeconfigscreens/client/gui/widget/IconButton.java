package fuzs.forgeconfigscreens.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * a copy of {@link net.minecraft.client.gui.components.ImageButton} with mutable texture coordinates
 */
public class IconButton extends Button {
    private final ResourceLocation resourceLocation;
    protected int xTexStart;
    protected int yTexStart;
    private final int yDiffTex;
    private final int textureWidth;
    private final int textureHeight;

    public IconButton(int x, int y, int width, int height, int xTexStart, int yTexStart, ResourceLocation resourceLocation, Button.OnPress onPress) {
        this(x, y, width, height, xTexStart, yTexStart, height, resourceLocation, 256, 256, onPress);
    }

    public IconButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffTex, ResourceLocation resourceLocation, Button.OnPress onPress) {
        this(x, y, width, height, xTexStart, yTexStart, yDiffTex, resourceLocation, 256, 256, onPress);
    }

    public IconButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffTex, ResourceLocation resourceLocation, int textureWidth, int textureHeight, Button.OnPress onPress) {
        this(x, y, width, height, xTexStart, yTexStart, yDiffTex, resourceLocation, textureWidth, textureHeight, onPress, TextComponent.EMPTY);
    }

    public IconButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffTex, ResourceLocation resourceLocation, int textureWidth, int textureHeight, Button.OnPress onPress, Component component) {
        this(x, y, width, height, xTexStart, yTexStart, yDiffTex, resourceLocation, textureWidth, textureHeight, onPress, NO_TOOLTIP, component);
    }

    public IconButton(int x, int y, int width, int height, int xTexStart, int yTexStart, ResourceLocation resourceLocation, Button.OnPress onPress, Button.OnTooltip onTooltip) {
        this(x, y, width, height, xTexStart, yTexStart, height, resourceLocation, 256, 256, onPress, onTooltip, TextComponent.EMPTY);
    }

    public IconButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffTex, ResourceLocation resourceLocation, int textureWidth, int textureHeight, Button.OnPress onPress, Button.OnTooltip onTooltip, Component component) {
        super(x, y, width, height, component, onPress, onTooltip);
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.xTexStart = xTexStart;
        this.yTexStart = yTexStart;
        this.yDiffTex = yDiffTex;
        this.resourceLocation = resourceLocation;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setTexture(int textureX, int textureY) {
        this.xTexStart = textureX;
        this.yTexStart = textureY;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        int index = this.getYImage(this.isHovered());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        this.blit(poseStack, this.x, this.y, 0, 46 + index * 20, this.width / 2, this.height);
        this.blit(poseStack, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + index * 20, this.width / 2, this.height);
        this.renderBg(poseStack, minecraft, mouseX, mouseY);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.resourceLocation);
        blit(poseStack, this.x, this.y, this.xTexStart, this.yTexStart + index * this.yDiffTex, this.width, this.height, this.textureWidth, this.textureHeight);
        if (this.isHovered()) {
            this.renderToolTip(poseStack, mouseX, mouseY);
        }
    }
}
