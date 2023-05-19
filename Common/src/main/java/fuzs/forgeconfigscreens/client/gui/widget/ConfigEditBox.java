package fuzs.forgeconfigscreens.client.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConfigEditBox extends EditBox {
    private final Supplier<ConfigEditBox> getActiveTextField;
    private final Consumer<ConfigEditBox> setActiveTextField;
    private boolean bordered = true;
    private boolean invalid;

    public ConfigEditBox(Font font, int x, int y, int width, int height) {
        this(font, x, y, width, height, () -> null, activeTextField -> {});
    }

    public ConfigEditBox(Font font, int x, int y, int width, int height, Supplier<ConfigEditBox> getActiveTextField, Consumer<ConfigEditBox> setActiveTextField) {
        super(font, x, y, width, height, Component.empty());
        this.getActiveTextField = getActiveTextField;
        this.setActiveTextField = setActiveTextField;
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (focused) {
            final ConfigEditBox activeTextField = this.getActiveTextField.get();
            if (activeTextField != null && activeTextField != this) {
                activeTextField.setFocused(false);
            }
            this.setActiveTextField.accept(this);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // left click clears text
        // only works for search field as entries inside of vanilla selection lists seem to be unable to handle left clicks properly
        if (this.isVisible() && button == 1) {
            this.setValue("");
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.renderWidget(poseStack, mouseX, mouseY, partialTicks);
        if (this.invalid && this.visible && this.bordered) {
            final int color = 16711680 | 255 << 24;
            fill(poseStack, this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY(), color);
            fill(poseStack, this.getX() - 1, this.getY() - 1, this.getX(), this.getY() + this.height + 1, color);
            fill(poseStack, this.getX() + this.width, this.getY() - 1, this.getX() + this.width + 1, this.getY() + this.height + 1, color);
            fill(poseStack, this.getX() - 1, this.getY() + this.height, this.getX() + this.width + 1, this.getY() + this.height + 1, color);
        }
    }

    @Override
    public void setBordered(boolean bordered) {
        super.setBordered(bordered);
        this.bordered = bordered;
    }

    public void markInvalid(boolean invalid) {
        this.invalid = invalid;
        this.setTextColor(invalid ? 16711680 : 14737632);
    }
}
