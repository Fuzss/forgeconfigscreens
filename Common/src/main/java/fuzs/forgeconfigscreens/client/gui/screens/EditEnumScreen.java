package fuzs.forgeconfigscreens.client.gui.screens;

import fuzs.forgeconfigscreens.client.gui.helper.PanoramaBackgroundHelper;
import fuzs.forgeconfigscreens.client.gui.helper.ScreenTextHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

@SuppressWarnings("ConstantConditions")
public class EditEnumScreen extends Screen {
	private final Screen lastScreen;
	private Enum<?> value;
	private final Enum<?>[] allValues;
	private final Predicate<Enum<?>> validator;
	private final Consumer<Enum<?>> onSave;
	private EnumList list;

	public EditEnumScreen(Screen lastScreen, Component title, Enum<?> value, Enum<?>[] allValues, Predicate<Enum<?>> validator, Consumer<Enum<?>> onSave) {
		super(title);
		this.lastScreen = lastScreen;
		this.value = value;
		this.allValues = allValues;
		this.validator = validator;
		this.onSave = onSave;
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}

	@Override
	protected void init() {
		this.list = new EnumList();
		this.addWidget(this.list);
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
			this.onSave.accept(this.value);
			this.minecraft.setScreen(this.lastScreen);
		}).bounds(this.width / 2 - 154, this.height - 28, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> {
			this.minecraft.setScreen(this.lastScreen);
		}).bounds(this.width / 2 + 4, this.height - 28, 150, 20).build());
		this.list.setSelected(this.list.children().stream().filter((entry) -> {
			return Objects.equals(entry.value, this.value);
		}).findFirst().orElse(null));
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(guiGraphics);
		this.list.render(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 14, 16777215);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public void renderDirtBackground(GuiGraphics guiGraphics) {
		PanoramaBackgroundHelper.renderDirtBackground(guiGraphics, this.width, this.height);
	}

	private class EnumList extends ObjectSelectionList<EnumList.Entry> {

		EnumList() {
			super(EditEnumScreen.this.minecraft, EditEnumScreen.this.width, EditEnumScreen.this.height, 36, EditEnumScreen.this.height - 36, 16);
			Stream.of(EditEnumScreen.this.allValues)
					.filter(EditEnumScreen.this.validator)
					.sorted(Comparator.comparing(Enum::name))
					.map(Entry::new)
					.forEach(this::addEntry);
			this.setRenderBackground(false);
			this.setRenderTopAndBottom(false);
		}

		@Override
		public boolean isFocused() {
			return EditEnumScreen.this.getFocused() == this;
		}

		@Override
		public void setSelected(@Nullable EditEnumScreen.EnumList.Entry entry) {
			super.setSelected(entry);
			if (entry != null) {
				EditEnumScreen.this.value = entry.value;
			}
		}

		@Override
		protected int getScrollbarPosition() {
			return this.width / 2 + 144;
		}

		@Override
		public int getRowWidth() {
			return 260;
		}

		private class Entry extends ObjectSelectionList.Entry<EnumList.Entry> {
			final Enum<?> value;
			private final Component name;

			public Entry(Enum<?> value) {
				this.value = value;
				this.name = ScreenTextHelper.toFormattedComponent(value.name().toLowerCase(Locale.ROOT));
			}

			@Override
			public Component getNarration() {
				return Component.translatable("narrator.select", this.name);
			}

			@Override
			public void render(GuiGraphics guiGraphics, int index, int entryTop, int entryLeft, int rowWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float partialTicks) {
				guiGraphics.drawCenteredString(EditEnumScreen.this.font, this.name, entryLeft + rowWidth / 2, entryTop + 2, 16777215);
			}

			@Override
			public boolean mouseClicked(double mouseX, double mouseY, int button) {
				if (button == 0) {
					EnumList.this.setSelected(this);
					return true;
				} else {
					return false;
				}
			}
		}
	}
}
