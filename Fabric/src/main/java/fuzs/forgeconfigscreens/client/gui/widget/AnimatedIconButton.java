package fuzs.forgeconfigscreens.client.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;

public class AnimatedIconButton extends IconButton {
    private final int origXTexStart;
    private int tickTime;
    private int frames;
    private int speed;

    public AnimatedIconButton(int x, int y, int width, int height, int xTexStart, int yTexStart, ResourceLocation resourceLocation, Button.OnPress onPress, Button.OnTooltip onTooltip) {
        super(x, y, width, height, xTexStart, yTexStart, resourceLocation, onPress, onTooltip);
        this.origXTexStart = xTexStart;
    }

    public AnimatedIconButton setAnimationData(int frameAmount, int speedInTicks) {
        if (frameAmount <= 0 || speedInTicks <= 0) throw new IllegalArgumentException("animation data must be greater than 0");
        this.frames = frameAmount;
        this.speed = speedInTicks;
        return this;
    }

    @Override
    public void setTexture(int textureX, int textureY) {
        throw new UnsupportedOperationException();
    }

    public void tick() {
        if (this.isHovered() || this.tickTime > 0) {
            this.tickTime++;
            this.tickTime %= this.frames * this.speed;
        }
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.xTexStart = this.origXTexStart + (int) ((this.tickTime + partialTicks) / this.speed) * this.width;
        super.renderButton(poseStack, mouseX, mouseY, partialTicks);
    }
}
