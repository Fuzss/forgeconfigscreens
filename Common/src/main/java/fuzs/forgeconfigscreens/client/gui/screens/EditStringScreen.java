package fuzs.forgeconfigscreens.client.gui.screens;

import fuzs.forgeconfigscreens.client.gui.helper.PanoramaBackgroundHelper;
import fuzs.forgeconfigscreens.client.gui.widget.ConfigEditBox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Predicate;

@SuppressWarnings("ConstantConditions")
public class EditStringScreen extends Screen {
    private final Screen lastScreen;
    private String value;
    private final Predicate<String> validator;
    private final Consumer<String> onSave;
    private ConfigEditBox textField;

    public EditStringScreen(Screen lastScreen, Component title, String value, Predicate<String> validator, Consumer<String> onSave) {
        super(title);
        this.lastScreen = lastScreen;
        this.value = value;
        this.validator = validator;
        this.onSave = onSave;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void tick() {
        this.textField.tick();
    }

    @Override
    protected void init() {
        final Button doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            this.onSave.accept(this.textField.getValue());
            this.minecraft.setScreen(this.lastScreen);
        }).bounds(this.width / 2 - 154, this.height / 2 + 3, 150, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> {
            this.minecraft.setScreen(this.lastScreen);
        }).bounds(this.width / 2 + 4, this.height / 2 + 3, 150, 20).build());
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
        this.addRenderableWidget(this.textField);
        this.setInitialFocus(this.textField);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        this.textField.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderDirtBackground(GuiGraphics guiGraphics) {
        PanoramaBackgroundHelper.renderDirtBackground(guiGraphics, this.width, this.height);
    }
}
