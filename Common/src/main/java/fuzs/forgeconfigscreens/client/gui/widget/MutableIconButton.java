package fuzs.forgeconfigscreens.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class MutableIconButton extends ImageButton implements Tickable {
    protected int xTexStart;
    protected int yTexStart;
    private int hoverTime;
    private int lastHoverTime;

    public MutableIconButton(int x, int y, int width, int height, int xTexStart, int yTexStart, ResourceLocation resourceLocation, Button.OnPress onPress) {
        super(x, y, width, height, xTexStart, yTexStart, resourceLocation, onPress);
        this.xTexStart = xTexStart;
        this.yTexStart = yTexStart;
    }

    public MutableIconButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffTex, ResourceLocation resourceLocation, Button.OnPress onPress) {
        super(x, y, width, height, xTexStart, yTexStart, yDiffTex, resourceLocation, onPress);
        this.xTexStart = xTexStart;
        this.yTexStart = yTexStart;
    }

    public MutableIconButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffTex, ResourceLocation resourceLocation, int textureWidth, int textureHeight, Button.OnPress onPress) {
        super(x, y, width, height, xTexStart, yTexStart, yDiffTex, resourceLocation, textureWidth, textureHeight, onPress);
        this.xTexStart = xTexStart;
        this.yTexStart = yTexStart;
    }

    public void setTexture(int textureX, int textureY) {
        this.xTexStart = textureX;
        this.yTexStart = textureY;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        float hoverTimeProgress = Mth.lerp(partialTicks, this.lastHoverTime, this.hoverTime) / 5.0F;
//        float leftProgress = Math.min(1.0F, hoverTimeProgress * 2.0F);
//        float rightProgress = Mth.clamp(hoverTimeProgress * 2.0F - 1.0F, 0.0F, 1.0F);
//        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 127 << 24 & 0xFF000000);
//        guiGraphics.fill(this.getX(), this.getY(), (int) (this.getX() + this.getWidth() * leftProgress), this.getY() + 1, 0xFF94E4D3);
//        guiGraphics.fill(this.getX(), this.getY(), this.getX() + 1, (int) (this.getY() + this.getHeight() * leftProgress), 0xFF94E4D3);
//        guiGraphics.fill(this.getX(), this.getY() + this.getHeight() - 1, (int) (this.getX() + this.getWidth() * rightProgress), this.getY() + this.getHeight(), 0xFF94E4D3);
//        guiGraphics.fill(this.getX() + this.getWidth() - 1, this.getY(), this.getX() + this.getWidth(), (int) (this.getY() + this.getHeight() * rightProgress), 0xFF94E4D3);
        float leftProgress = Math.min(1.0F, hoverTimeProgress * 2.0F);
        float rightProgress = Mth.clamp(hoverTimeProgress * 2.0F - 1.0F, 0.0F, 1.0F);
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 127 << 24 & 0xFF000000);
        guiGraphics.fill(this.getX(), this.getY(), (int) (this.getX() + this.getWidth() * hoverTimeProgress), this.getY() + 1, 0xFF94E4D3);
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + 1, (int) (this.getY() + this.getHeight() * hoverTimeProgress), 0xFF94E4D3);
        guiGraphics.fill((int) (this.getX() + this.getWidth() * (1.0F - hoverTimeProgress)), this.getY() + this.getHeight() - 1, (int) (this.getX() + this.getWidth()), this.getY() + this.getHeight(), 0xFF94E4D3);
        guiGraphics.fill(this.getX() + this.getWidth() - 1, (int) (this.getY() + this.getHeight() * (1.0F - hoverTimeProgress)), this.getX() + this.getWidth(), (int) (this.getY() + this.getHeight()), 0xFF94E4D3);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderTexture(guiGraphics, this.resourceLocation, this.getX(), this.getY(), this.xTexStart, this.yTexStart, this.yDiffTex, this.width, this.height, this.textureWidth, this.textureHeight);
    }

    @Override
    public void renderTexture(GuiGraphics guiGraphics, ResourceLocation resourceLocation, int i, int j, int k, int l, int m, int n, int o, int p, int q) {
        int r = l + m;
        if (!this.isActive()) {
            r = l;
        } else if (this.isHoveredOrFocused()) {
            r = l + m * 2;
        }

        RenderSystem.enableDepthTest();
        guiGraphics.blit(resourceLocation, i, j, k, r, n, o, p, q);
    }

    @Override
    public void tick() {
        this.lastHoverTime = this.hoverTime;
        if (this.isHovered) {
            if (this.hoverTime < 5) this.hoverTime++;
        } else {
            if (this.hoverTime > 0) this.hoverTime--;
        }
    }
}
