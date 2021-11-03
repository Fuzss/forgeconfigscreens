package fuzs.configmenusforge.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.matrix.MatrixStack;
import fuzs.configmenusforge.client.gui.components.CustomBackgroundContainerObjectSelectionList;
import fuzs.configmenusforge.client.gui.util.ScreenUtil;
import fuzs.configmenusforge.client.gui.widget.ConfigEditBox;
import fuzs.configmenusforge.client.gui.widget.IconButton;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("ConstantConditions")
public class EditListScreen extends Screen {
    private final Screen lastScreen;
    private final ResourceLocation background;
    private final List<MutableObject<String>> values;
    private final Predicate<String> validator;
    private final Consumer<List<String>> onSave;
    private final Set<EditEntry> invalidEntries = Sets.newHashSet();
    private EditList list;
    private Button doneButton;
    @Nullable
    private ConfigEditBox activeTextField;
    @Nullable
    private List<? extends IReorderingProcessor> activeTooltip;
    private int tooltipTicks;

    public EditListScreen(Screen lastScreen, ITextComponent title, ResourceLocation background, List<String> listValue, Predicate<String> validator, Consumer<List<String>> onSave) {
        super(title);
        this.lastScreen = lastScreen;
        this.background = background;
        this.values = listValue.stream()
                .map(MutableObject::new)
                .collect(Collectors.toList());
        this.validator = validator;
        this.onSave = onSave;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    protected void init() {
        this.list = new EditList(this.values);
        this.addWidget(this.list);
        this.doneButton = this.addButton(new Button(this.width / 2 - 154, this.height - 28, 150, 20, DialogTexts.GUI_DONE, button -> {
            this.onSave.accept(this.values.stream()
                    .map(MutableObject::getValue)
                    .collect(Collectors.toList()));
            this.minecraft.setScreen(this.lastScreen);
        }));
        this.addButton(new Button(this.width / 2 + 4, this.height - 28, 150, 20, DialogTexts.GUI_CANCEL, button -> {
            this.minecraft.setScreen(this.lastScreen);
        }));
    }

    @Override
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float partialTicks) {
        List<? extends IReorderingProcessor> lastTooltip = this.activeTooltip;
        this.activeTooltip = null;
        ScreenUtil.renderCustomBackground(this, this.background, 0);
        this.list.render(poseStack, mouseX, mouseY, partialTicks);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 14, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        if (this.activeTooltip != lastTooltip) {
            this.tooltipTicks = 0;
        }
        if (this.activeTooltip != null && this.tooltipTicks >= 10) {
            this.renderTooltip(poseStack, this.activeTooltip, mouseX, mouseY);
        }
    }

    @Override
    public void tick() {
        // makes the cursor blink
        if (this.activeTextField != null) {
            this.activeTextField.tick();
        }
        // makes tooltips not appear immediately
        if (this.tooltipTicks < 10) {
            this.tooltipTicks++;
        }
    }

    private void updateDoneButton() {
        if (this.doneButton != null) {
            this.doneButton.active = this.invalidEntries.isEmpty();
        }
    }

    void markInvalid(EditEntry entry) {
        this.invalidEntries.add(entry);
        this.updateDoneButton();
    }

    void clearInvalid(EditEntry entry) {
        this.invalidEntries.remove(entry);
        this.updateDoneButton();
    }

    public class EditList extends CustomBackgroundContainerObjectSelectionList<EditListEntry> {
        public EditList(List<MutableObject<String>> values) {
            super(EditListScreen.this.minecraft, EditListScreen.this.background, EditListScreen.this.width, EditListScreen.this.height, 36, EditListScreen.this.height - 36, 24);
            values.forEach(value -> {
                this.addEntry(new EditEntry(this, value));
            });
            this.addEntry(new AddEntry(this, values));
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width / 2 + 144;
        }

        @Override
        public int getRowWidth() {
            return 260;
        }

        protected int addEntry(int index, EditListEntry entry) {
            this.children().add(index, entry);
            return this.children().size() - 1;
        }

        @Override
        protected boolean removeEntry(EditListEntry entry) {
            return super.removeEntry(entry);
        }
    }

    public static abstract class EditListEntry extends AbstractOptionList.Entry<EditListEntry> {

    }

    private class AddEntry extends EditListEntry {
        private final Button addButton;

        public AddEntry(EditList list, List<MutableObject<String>> values) {
            final List<IReorderingProcessor> tooltip = EditListScreen.this.minecraft.font.split(new TranslationTextComponent("configmenusforge.gui.tooltip.add"), 200);
            this.addButton = new IconButton(0, 0, 20, 20, 80, 0, ConfigScreen.ICONS_LOCATION, button -> {
                MutableObject<String> holder = new MutableObject<>("");
                values.add(holder);
                list.addEntry(list.children().size() - 1, new EditEntry(list, holder, true));
            }, (button, matrixStack, mouseX, mouseY) -> {
                if (button.active) {
                    EditListScreen.this.activeTooltip = tooltip;
                }
            });
        }

        @Override
        public void render(MatrixStack poseStack, int index, int entryTop, int entryLeft, int rowWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            this.addButton.x = entryLeft + rowWidth - 21;
            this.addButton.y = entryTop;
            this.addButton.render(poseStack, mouseX, mouseY, partialTicks);
        }

        @Override
        public List<? extends IGuiEventListener> children() {
            return ImmutableList.of(this.addButton);
        }
    }

    private class EditEntry extends EditListEntry {
        private final MutableObject<String> holder;
        private final ConfigEditBox textField;
        private final Button deleteButton;

        public EditEntry(EditList list, MutableObject<String> holder) {
            this(list, holder, false);
        }

        public EditEntry(EditList list, MutableObject<String> holder, boolean withFocus) {
            this.holder = holder;
            this.textField = new ConfigEditBox(EditListScreen.this.font, 0, 0, 260 - 24, 18, () -> EditListScreen.this.activeTextField, activeTextField -> EditListScreen.this.activeTextField = activeTextField) {

                @Override
                public void setFocus(boolean focused) {
                    super.setFocus(focused);
                    EditListScreen.this.activeTextField = focused ? this : null;
                }
            };
            this.textField.setResponder(input -> {
                if (EditListScreen.this.validator.test(input)) {
                    this.textField.markInvalid(false);
                    this.holder.setValue(input);
                    EditListScreen.this.clearInvalid(this);
                } else {
                    this.textField.markInvalid(true);
                    EditListScreen.this.markInvalid(this);
                }
            });
            this.textField.setValue(holder.getValue());
            this.textField.setFocus(withFocus);

            final List<IReorderingProcessor> tooltip = EditListScreen.this.minecraft.font.split(new TranslationTextComponent("configmenusforge.gui.tooltip.remove"), 200);
            this.deleteButton = new IconButton(0, 0, 20, 20, 100, 0, ConfigScreen.ICONS_LOCATION, button -> {
                EditListScreen.this.values.remove(holder);
                list.removeEntry(this);
                EditListScreen.this.clearInvalid(this);
            }, (button, matrixStack, mouseX, mouseY) -> {
                if (button.active) {
                    EditListScreen.this.activeTooltip = tooltip;
                }
            });
        }

        @Override
        public void render(MatrixStack poseStack, int index, int entryTop, int entryLeft, int rowWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            this.textField.x = entryLeft;
            this.textField.y = entryTop + 1;
            this.textField.render(poseStack, mouseX, mouseY, partialTicks);
            this.deleteButton.x = entryLeft + rowWidth - 21;
            this.deleteButton.y = entryTop;
            this.deleteButton.render(poseStack, mouseX, mouseY, partialTicks);
        }

        @Override
        public List<? extends IGuiEventListener> children() {
            return ImmutableList.of(this.textField, this.deleteButton);
        }
    }
}
