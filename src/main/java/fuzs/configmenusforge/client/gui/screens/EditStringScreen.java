package fuzs.configmenusforge.client.gui.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import fuzs.configmenusforge.client.gui.util.ScreenUtil;
import fuzs.configmenusforge.client.gui.widget.ConfigEditBox;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Consumer;
import java.util.function.Predicate;

@SuppressWarnings("ConstantConditions")
public class EditStringScreen extends Screen {
    private final Screen lastScreen;
    private final ResourceLocation background;
    private String value;
    private final Predicate<String> validator;
    private final Consumer<String> onSave;
    private ConfigEditBox textField;

    public EditStringScreen(Screen lastScreen, ITextComponent title, ResourceLocation background, String value, Predicate<String> validator, Consumer<String> onSave) {
        super(title);
        this.lastScreen = lastScreen;
        this.background = background;
        this.value = value;
        this.validator = validator;
        this.onSave = onSave;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    protected void init() {
        final Button doneButton = this.addButton(new Button(this.width / 2 - 154, this.height / 2 + 3, 150, 20, DialogTexts.GUI_DONE, button -> {
            this.onSave.accept(this.textField.getValue());
            this.minecraft.setScreen(this.lastScreen);
        }));
        this.addButton(new Button(this.width / 2 + 4, this.height / 2 + 3, 150, 20, DialogTexts.GUI_CANCEL, button -> {
            this.minecraft.setScreen(this.lastScreen);
        }));
        this.textField = new ConfigEditBox(this.font, this.width / 2 - 153, this.height / 2 - 25, 306, 20);
        this.textField.setMaxLength(32500);
        this.textField.setCanLoseFocus(false);
        this.textField.setResponder(input -> {
            // save this as init is re-run on screen resizing
            this.value = input;
            if (this.validator.test(input)) {
                this.textField.markInvalid(false);
                doneButton.active = true;
            } else {
                this.textField.markInvalid(true);
                doneButton.active = false;
            }
        });
        this.textField.setValue(this.value);
        this.addButton(this.textField);
        this.setInitialFocus(this.textField);
    }

    @Override
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float partialTicks) {
        ScreenUtil.renderCustomBackground(this, this.background, 0);
        this.textField.render(poseStack, mouseX, mouseY, partialTicks);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }
}
