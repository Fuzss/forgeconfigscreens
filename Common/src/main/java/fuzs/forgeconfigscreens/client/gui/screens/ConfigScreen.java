package fuzs.forgeconfigscreens.client.gui.screens;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import fuzs.forgeconfigscreens.ForgeConfigScreens;
import fuzs.forgeconfigscreens.client.gui.data.EntryData;
import fuzs.forgeconfigscreens.client.gui.data.IEntryData;
import fuzs.forgeconfigscreens.client.gui.helper.PanoramaBackgroundHelper;
import fuzs.forgeconfigscreens.client.gui.helper.ScreenTextHelper;
import fuzs.forgeconfigscreens.client.gui.widget.ConfigEditBox;
import fuzs.forgeconfigscreens.client.gui.widget.MutableIconButton;
import fuzs.forgeconfigscreens.client.helper.ServerConfigUploader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("ConstantConditions")
public abstract class ConfigScreen extends Screen {
    public static final ResourceLocation ICONS_LOCATION = ForgeConfigScreens.id("textures/gui/icons.png");
    public static final Component SORTING_AZ_TOOLTIP = Component.translatable("configmenusforge.gui.tooltip.sorting", Component.translatable("configmenusforge.gui.tooltip.sorting.az"));
    public static final Component SORTING_ZA_TOOLTIP = Component.translatable("configmenusforge.gui.tooltip.sorting", Component.translatable("configmenusforge.gui.tooltip.sorting.za"));
    public static final Component SAVE_COMPONENT = Component.translatable("configmenusforge.gui.save");

    final Screen lastScreen;
    /**
     * entries used when searching
     * includes entries from this screen {@link #screenEntries} and from all sub screens of this
     */
    private final List<IEntryData> searchEntries;
    /**
     * default entries shown on screen when not searching
     */
    private final List<IEntryData> screenEntries;
    /**
     * all values of this mod's configs stored as our custom units
     * they are accessible by configvalue and unmodifiableconfig
     * only used when building screen specific search and display lists and for certain actions on main screen
     * same for all sub menus
     */
    final Map<Object, IEntryData> valueToData;
    private ConfigList list;
    EditBox searchTextField;
    private Button doneButton;
    private final Runnable onSave;
    private Button cancelButton;
    private Button backButton;
    private Button reverseButton;
    private Button filterButton;
    private Button searchFilterButton;
    // 0 = reverse, 1 = filter, 2 = search filter
    private final int[] buttonData;
    @Nullable
    private ConfigEditBox activeTextField;
    @Nullable
    private List<? extends FormattedCharSequence> activeTooltip;
    private int tooltipTicks;

    private ConfigScreen(Screen lastScreen, Component title, UnmodifiableConfig config, Map<Object, IEntryData> valueToData, int[] buttonData, Runnable onSave) {
        super(title);
        this.lastScreen = lastScreen;
        this.valueToData = valueToData;
        this.buttonData = buttonData;
        this.onSave = onSave;
        this.searchEntries = this.gatherEntriesRecursive(config, valueToData);
        this.screenEntries = config.valueMap().values().stream().map(valueToData::get).toList();
        this.buildSubScreens(this.screenEntries);
    }

    private List<IEntryData> gatherEntriesRecursive(UnmodifiableConfig mainConfig, Map<Object, IEntryData> allEntries) {
        List<IEntryData> entries = Lists.newArrayList();
        this.gatherEntriesRecursive(mainConfig, entries, allEntries);
        return ImmutableList.copyOf(entries);
    }

    private void gatherEntriesRecursive(UnmodifiableConfig mainConfig, List<IEntryData> entries, Map<Object, IEntryData> allEntries) {
        mainConfig.valueMap().values().forEach(value -> {
            entries.add(allEntries.get(value));
            if (value instanceof UnmodifiableConfig config) {
                this.gatherEntriesRecursive(config, entries, allEntries);
            }
        });
    }

    private void buildSubScreens(List<IEntryData> screenEntries) {
        // every screen must build their own direct sub screens so when searching we can jump between screens in their actual hierarchical order
        // having screens stored like this is ok as everything else will be reset during init when the screen is opened anyways
        for (IEntryData unit : screenEntries) {
            if (unit instanceof EntryData.CategoryEntryData categoryEntryData) {
                categoryEntryData.setScreen(new Sub(this, categoryEntryData.getTitle(), categoryEntryData.getConfig()));
            }
        }
    }

    public static ConfigScreen create(Screen lastScreen, Component title, ModConfig config, Map<Object, IEntryData> valueToData) {
        return new ConfigScreen.Main(lastScreen, title, ((ForgeConfigSpec) config.getSpec()).getValues(), valueToData, () -> ServerConfigUploader.saveAndUpload(config));
    }

    abstract Component getCancelComponent();

    abstract Screen getSaveExitScreen();

    void updateDoneButtonState() {
        if (this.doneButton != null) {
            this.doneButton.active = this.valueToData.values().stream().anyMatch(Predicate.not(IEntryData::mayDiscardChanges));
        }
    }

    @Override
    public void renderDirtBackground(GuiGraphics guiGraphics) {
        PanoramaBackgroundHelper.renderDirtBackground(guiGraphics, this.width, this.height);
    }

    private static class Main extends ConfigScreen {

        private Main(Screen lastScreen, Component title, UnmodifiableConfig config, Map<Object, IEntryData> valueToData, Runnable onSave) {
            super(lastScreen, title, config, valueToData, new int[3], onSave);
        }

        @Override
        Component getCancelComponent() {
            return CommonComponents.GUI_CANCEL;
        }

        @Override
        Screen getSaveExitScreen() {
            return this.lastScreen;
        }

        @Override
        public void onClose() {
            // exit out of search before closing screen
            if (!this.searchTextField.getValue().isEmpty()) {
                this.searchTextField.setValue("");
            } else {
                // when canceling display confirm screen if any values have been changed
                Screen confirmScreen;
                if (this.valueToData.values().stream().allMatch(IEntryData::mayDiscardChanges)) {
                    confirmScreen = this.lastScreen;
                } else {
                    confirmScreen = new ConfirmScreen(result -> {
                        if (result) {
                            this.valueToData.values().forEach(IEntryData::discardCurrentValue);
                            this.minecraft.setScreen(this.lastScreen);
                        } else {
                            this.minecraft.setScreen(this);
                        }
                    }, Component.translatable("configmenusforge.gui.message.discard"), Component.empty()) {


                        @Override
                        public void renderDirtBackground(GuiGraphics guiGraphics) {
                            PanoramaBackgroundHelper.renderDirtBackground(guiGraphics, this.width, this.height);
                        }
                    };
                }
                this.minecraft.setScreen(confirmScreen);
            }
        }
    }

    private static class Sub extends ConfigScreen {

        private Sub(ConfigScreen lastScreen, Component title, UnmodifiableConfig config) {
            super(lastScreen, title, config, lastScreen.valueToData, lastScreen.buttonData, lastScreen.onSave);
        }

        @Override
        protected void init() {
            super.init();
            this.makeNavigationButtons().forEach(this::addRenderableWidget);
            this.onSearchFieldChanged(this.searchTextField.getValue().trim().isEmpty());
        }

        @Override
        Component getCancelComponent() {
            return CommonComponents.GUI_BACK;
        }

        @Override
        Screen getSaveExitScreen() {
            List<ConfigScreen> lastScreens = this.getLastScreens();
            return lastScreens.get(lastScreens.size() - 1).lastScreen;
        }

        private List<Button> makeNavigationButtons() {
            List<ConfigScreen> lastScreens = this.getLastScreens();
            final int maxSize = 5;
            List<Button> buttons = Lists.newLinkedList();
            for (int i = 0, size = Math.min(maxSize, lastScreens.size()); i < size; i++) {
                Screen screen = lastScreens.get(size - 1 - i);
                final boolean otherScreen = screen != this;
                final Component title = i == 0 && lastScreens.size() > maxSize ? Component.literal(". . .") : screen.getTitle();
                buttons.add(new Button(0, 1, Sub.this.font.width(title) + 4, 20, title, button -> {
                    if (otherScreen) Sub.this.minecraft.setScreen(screen);
                }, Supplier::get) {

                    @Override
                    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
                        // yellow when hovered
                        int color = otherScreen && this.isHovered ? 16777045 : 16777215;
                        guiGraphics.drawCenteredString(Sub.this.font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, color);
                        if (this.isHovered && otherScreen && this.active) {
                            // move down as this is right at screen top
                            guiGraphics.renderTooltip(Sub.this.font, CommonComponents.GUI_BACK, mouseX, mouseY + 24);
                        }
                    }

                    @Override
                    public void playDownSound(SoundManager soundManager) {
                        if (otherScreen) super.playDownSound(soundManager);
                    }
                });
                if (i < size - 1) {
                    buttons.add(new Button(0, 1, Sub.this.font.width(">") + 4, 20, Component.literal(">"), button -> {
                    }, Supplier::get) {

                        @Override
                        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
                            guiGraphics.drawCenteredString(Sub.this.font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, 16777215);
                        }

                        @Override
                        public void playDownSound(SoundManager soundManager) {

                        }
                    });
                }
            }
            this.setButtonPosX(buttons);
            return buttons;
        }

        private List<ConfigScreen> getLastScreens() {
            Screen lastScreen = this;
            List<ConfigScreen> lastScreens = Lists.newLinkedList();
            while (lastScreen instanceof ConfigScreen configScreen) {
                lastScreens.add(configScreen);
                lastScreen = configScreen.lastScreen;
            }
            return lastScreens;
        }

        private void setButtonPosX(List<Button> buttons) {
            int posX = (this.width - buttons.stream().mapToInt(AbstractWidget::getWidth).sum()) / 2;
            for (Button navigationButton : buttons) {
                navigationButton.setX(posX);
                posX += navigationButton.getWidth();
            }
        }

        @Override
        void drawBaseTitle(GuiGraphics guiGraphics) {

        }

        @Override
        public void onClose() {
            // exit out of search before closing screen
            if (!this.searchTextField.getValue().isEmpty()) {
                this.searchTextField.setValue("");
            } else {
                this.minecraft.setScreen(this.lastScreen);
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        boolean focus = this.searchTextField != null && this.searchTextField.isFocused();
        this.searchTextField = new EditBox(this.font, this.width / 2 - 121, 22, 242, 20, this.searchTextField, CommonComponents.EMPTY) {
//            private static final Component SEARCH_COMPONENT = Component.translatable("configmenusforge.gui.search").withStyle(ChatFormatting.GRAY);

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                // left click clears text
                if (this.isVisible() && button == InputConstants.MOUSE_BUTTON_RIGHT) {
                    this.setValue("");
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }

//            @Override
//            public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTime) {
//                super.renderButton(poseStack, mouseX, mouseY, partialTime);
//                if (this.isVisible() && !this.isFocused() && this.getValue().isEmpty()) {
//                    ConfigScreen.this.font.draw(poseStack, SEARCH_COMPONENT, this.x + 4, this.y + 6, 16777215);
//                }
//            }
        };
        this.searchTextField.setResponder(query -> this.updateList(query, true));
        this.searchTextField.setFocused(focus);
        this.list = new ConfigList(this.getConfigListEntries(this.searchTextField.getValue()));
        this.addWidget(this.list);
        this.addWidget(this.searchTextField);
        this.reverseButton = this.addRenderableWidget(new MutableIconButton(this.width / 2 - 126 - 20, 22, 20, 20, this.buttonData[0] == 1 ? 20 : 0, 0, ICONS_LOCATION, button -> {
            this.buttonData[0] = (this.buttonData[0] + 1) % 2;
            this.updateList(true);
            ((MutableIconButton) button).setTexture(this.buttonData[0] == 1 ? 20 : 0, 0);
        }) {

            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
                if (this.active && this.isHovered) {
                    guiGraphics.renderTooltip(ConfigScreen.this.font, ConfigScreen.this.buttonData[0] == 1 ? SORTING_ZA_TOOLTIP : SORTING_AZ_TOOLTIP, mouseX, mouseY);
                }
            }
        });
        this.filterButton = this.addRenderableWidget(new MutableIconButton(this.width / 2 + 126, 22, 20, 20, ShownEntriesFilter.values()[this.buttonData[1]].getTextureX(), 0, ICONS_LOCATION, button -> {
            this.buttonData[1] = ShownEntriesFilter.cycle(this.buttonData[1], false, Screen.hasShiftDown());
            this.updateList(true);
            ((MutableIconButton) button).setTexture(ShownEntriesFilter.values()[this.buttonData[1]].getTextureX(), 0);
        }) {

            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
                if (this.active && this.isHovered) {
                    guiGraphics.renderTooltip(ConfigScreen.this.font, ShownEntriesFilter.values()[ConfigScreen.this.buttonData[1]].getMessage(), mouseX, mouseY);
                }
            }
        });
        this.searchFilterButton = this.addRenderableWidget(new MutableIconButton(this.width / 2 + 126, 22, 20, 20, ShownEntriesFilter.values()[this.buttonData[2]].getTextureX(), 0, ICONS_LOCATION, button -> {
            this.buttonData[2] = ShownEntriesFilter.cycle(this.buttonData[2], true, Screen.hasShiftDown());
            this.updateList(true);
            ((MutableIconButton) button).setTexture(ShownEntriesFilter.values()[this.buttonData[2]].getTextureX(), 0);
        }) {

            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
                if (this.active && this.isHovered) {
                    guiGraphics.renderTooltip(ConfigScreen.this.font, ShownEntriesFilter.values()[ConfigScreen.this.buttonData[2]].getMessage(), mouseX, mouseY);
                }
            }
        });
        this.doneButton = this.addRenderableWidget(Button.builder(SAVE_COMPONENT, button -> {
            // avoid unnecessary config reloading
            if (this.valueToData.values().stream().anyMatch(Predicate.not(IEntryData::mayDiscardChanges))) {
                this.valueToData.values().forEach(IEntryData::saveConfigValue);
                this.onSave.run();
            }
            this.minecraft.setScreen(this.getSaveExitScreen());
        }).bounds(this.width / 2 - 154, this.height - 28, 150, 20).build());
        this.cancelButton = this.addRenderableWidget(Button.builder(this.getCancelComponent(), button -> {
            this.onClose();
        }).bounds(this.width / 2 + 4, this.height - 28, 150, 20).build());
        // button is made visible when search field is active
        this.backButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> {
            this.searchTextField.setValue("");
        }).bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
        this.onSearchFieldChanged(this.searchTextField.getValue().trim().isEmpty());
        this.updateDoneButtonState();
    }

    public void updateList(boolean resetScroll) {
        this.updateList(this.searchTextField.getValue(), resetScroll);
    }

    private void updateList(String query, boolean resetScroll) {
        this.list.replaceEntries(this.getConfigListEntries(query), resetScroll);
        this.onSearchFieldChanged(query.trim().isEmpty());
    }

    private List<ConfigScreen.Entry> getConfigListEntries(String query) {
        query = query.toLowerCase(Locale.ROOT).trim();
        return this.getConfigListEntries(!query.isEmpty() ? this.searchEntries : this.screenEntries, query);
    }

    List<ConfigScreen.Entry> getConfigListEntries(List<IEntryData> entries, final String searchHighlight) {
        final boolean empty = searchHighlight.isEmpty();
        return entries.stream()
                .filter(data -> data.mayInclude(searchHighlight) && ShownEntriesFilter.values()[empty ? this.buttonData[1] : this.buttonData[2]].test(data, empty))
                .sorted(IEntryData.getSearchComparator(searchHighlight, this.buttonData[0] == 1))
                .map(entryData -> this.makeEntry(entryData, searchHighlight))
                // there might be an unsupported value which will return null
                .filter(Objects::nonNull)
                .toList();
    }

    final void onSearchFieldChanged(boolean empty) {
        // sets button visibilities
        this.reverseButton.visible = empty;
        this.filterButton.visible = empty;
        this.searchFilterButton.visible = !empty;
        this.doneButton.visible = empty;
        this.cancelButton.visible = empty;
        this.backButton.visible = !empty;
    }

    @Override
    public void tick() {
        // makes the cursor blink
        this.searchTextField.tick();
        if (this.activeTextField != null) {
            this.activeTextField.tick();
        }
        // makes tooltips not appear immediately
        if (this.tooltipTicks < 10) {
            this.tooltipTicks++;
        }
        for (GuiEventListener eventListener : this.children()) {
            if (eventListener instanceof Tickable tickable) tickable.tick();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        List<? extends FormattedCharSequence> lastTooltip = this.activeTooltip;
        this.activeTooltip = null;
        this.renderBackground(guiGraphics);
        this.list.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.searchTextField.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.drawBaseTitle(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        if (this.activeTooltip != lastTooltip) {
            this.tooltipTicks = 0;
        }
        if (this.activeTooltip != null && this.tooltipTicks >= 10) {
            guiGraphics.renderTooltip(this.font, this.activeTooltip, mouseX, mouseY);
        }
    }

    void drawBaseTitle(GuiGraphics guiGraphics) {
        int width = this.font.width(this.getTitle());
        int posX = (this.width - width) / 2;
        int posY = 7;
        guiGraphics.fill(posX - 2, posY - 2, posX + width + 2, posY + this.font.lineHeight + 2, 0x7F000000);
        guiGraphics.drawString(this.font, this.getTitle(), posX, posY, 0xFFFFFF, true);
    }

    @Override
    public abstract void onClose();

    void setActiveTooltip(@Nullable List<? extends FormattedCharSequence> activeTooltip) {
        this.activeTooltip = activeTooltip;
    }

    @SuppressWarnings("unchecked")
    Entry makeEntry(IEntryData entryData, String searchHighlight) {
        if (entryData instanceof EntryData.CategoryEntryData categoryData) {
            return new CategoryEntry(categoryData, searchHighlight);
        } else if (entryData instanceof EntryData.ConfigEntryData<?> configEntryData) {
            final Object currentValue = configEntryData.getCurrentValue();
            if (currentValue instanceof Boolean) {
                return new BooleanEntry((EntryData.ConfigEntryData<Boolean>) entryData, searchHighlight);
            } else if (currentValue instanceof Integer) {
                return new NumberEntry<>((EntryData.ConfigEntryData<Integer>) entryData, searchHighlight, Integer::parseInt);
            } else if (currentValue instanceof Double) {
                return new NumberEntry<>((EntryData.ConfigEntryData<Double>) entryData, searchHighlight, Double::parseDouble);
            } else if (currentValue instanceof Long) {
                return new NumberEntry<>((EntryData.ConfigEntryData<Long>) entryData, searchHighlight, Long::parseLong);
            } else if (currentValue instanceof Enum<?>) {
                return new EnumEntry((EntryData.ConfigEntryData<Enum<?>>) entryData, searchHighlight);
            } else if (currentValue instanceof String) {
                return new StringEntry((EntryData.ConfigEntryData<String>) entryData, searchHighlight);
            } else if (currentValue instanceof List<?> listValue) {
                Object value = this.getListValue(((EntryData.ConfigEntryData<List<?>>) entryData).getDefaultValue(), listValue);
                try {
                    return this.makeListEntry(entryData, searchHighlight, value);
                } catch (RuntimeException e) {
                    ForgeConfigScreens.LOGGER.warn("Unable to add list entry containing class type {}", value != null ? value.getClass().getSimpleName() : "null", e);
                }
                return null;
            }
            ForgeConfigScreens.LOGGER.warn("Unsupported config value of class type {}", currentValue.getClass().getSimpleName());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private ListEntry<?> makeListEntry(IEntryData entryData, String searchHighlight, Object value) throws RuntimeException {
        if (value instanceof Boolean) {
            return new ListEntry<>((EntryData.ConfigEntryData<List<Boolean>>) entryData, searchHighlight, "Boolean", v -> switch (v.toLowerCase(Locale.ROOT)) {
                case "true" -> true;
                case "false" -> false;
                // is caught when editing
                default -> throw new IllegalArgumentException("unable to convert boolean value");
            });
        } else if (value instanceof Integer) {
            return new ListEntry<>((EntryData.ConfigEntryData<List<Integer>>) entryData, searchHighlight, "Integer", Integer::parseInt);
        } else if (value instanceof Double) {
            return new ListEntry<>((EntryData.ConfigEntryData<List<Double>>) entryData, searchHighlight, "Double", Double::parseDouble);
        } else if (value instanceof Long) {
            return new ListEntry<>((EntryData.ConfigEntryData<List<Long>>) entryData, searchHighlight, "Long", Long::parseLong);
        } else if (value instanceof Enum<?>) {
            return new EnumListEntry((EntryData.ConfigEntryData<List<Enum<?>>>) entryData, searchHighlight, (Class<Enum<?>>) value.getClass());
        } else if (value instanceof String) {
            return new ListEntry<>((EntryData.ConfigEntryData<List<String>>) entryData, searchHighlight, "String", s -> {
                if (s.isEmpty()) {
                    throw new IllegalArgumentException("string must not be empty");
                }
                return s;
            });
        } else {
            // string list with warning screen
            return new DangerousListEntry((EntryData.ConfigEntryData<List<String>>) entryData, searchHighlight);
        }
    }

    @Nullable
    private Object getListValue(List<?> defaultValue, List<?> currentValue) {
        // desperate attempt to somehow get some generic information out of a list
        // checking default values first is important as current values might be of a different type due to how configs are read
        // example: enum are read as strings, longs as integers
        if (!defaultValue.isEmpty()) {
            return defaultValue.get(0);
        } else if (!currentValue.isEmpty()) {
            return currentValue.get(0);
        }
        return null;
    }

    public class ConfigList extends ContainerObjectSelectionList<Entry> {

        public ConfigList(List<ConfigScreen.Entry> entries) {
            super(ConfigScreen.this.minecraft, ConfigScreen.this.width, ConfigScreen.this.height, 50, ConfigScreen.this.height - 36, 24);
            entries.forEach(this::addEntry);
            this.setRenderBackground(false);
            this.setRenderTopAndBottom(false);
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width / 2 + 144;
        }

        @Override
        public int getRowWidth() {
            return 260;
        }

        protected void replaceEntries(Collection<ConfigScreen.Entry> entries, boolean resetScroll) {
            super.replaceEntries(entries);
            // important when clearing search
            if (resetScroll) {
                this.setScrollAmount(0.0);
            }
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            super.render(guiGraphics, mouseX, mouseY, partialTicks);
            if (this.isMouseOver(mouseX, mouseY) && mouseX < ConfigScreen.this.list.getRowLeft() + ConfigScreen.this.list.getRowWidth() - 67) {
                ConfigScreen.Entry entry = this.getHovered();
                if (entry != null) {
                    ConfigScreen.this.setActiveTooltip(entry.getTooltip());
                }
            }
        }
    }

    public abstract class Entry extends ContainerObjectSelectionList.Entry<ConfigScreen.Entry> {
        private final Component title;
        @Nullable
        private final List<? extends FormattedCharSequence> tooltip;

        protected Entry(IEntryData data, String searchHighlight) {
            this.title = data.getDisplayTitle(searchHighlight);
            final List<FormattedText> lines = Lists.newArrayList();
            this.addLines(ConfigScreen.this.font, data, searchHighlight, lines);
            this.tooltip = lines.isEmpty() ? null : Language.getInstance().getVisualOrder(lines);
        }

        public final Component getTitle() {
            return this.title;
        }

        abstract void addLines(Font font, IEntryData data, String searchHighlight, List<FormattedText> lines);

        @Nullable
        public final List<? extends FormattedCharSequence> getTooltip() {
            return this.tooltip;
        }

        abstract boolean isHovered(int mouseX, int mouseY);

        @Override
        public void render(GuiGraphics guiGraphics, int index, int entryTop, int entryLeft, int rowWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            if (this.isHovered(mouseX, mouseY)) {
                ConfigScreen.this.setActiveTooltip(this.tooltip);
            }
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(new NarratableEntry() {
                @Override
                public NarratableEntry.NarrationPriority narrationPriority() {
                    return NarratableEntry.NarrationPriority.HOVERED;
                }

                @Override
                public void updateNarration(NarrationElementOutput output) {
                    output.add(NarratedElementType.TITLE, ConfigScreen.Entry.this.title);
                }
            });
        }
    }

    private class CategoryEntry extends Entry {
        private final Button button;

        public CategoryEntry(EntryData.CategoryEntryData data, String searchHighlight) {
            super(data, searchHighlight);
            // should really be truncated when too long but haven't found a way to convert result back to component for using with button while preserving formatting
            this.button = Button.builder(this.getTitle(), button -> {
                // values are usually preserved, so here we force a reset
                ConfigScreen.this.searchTextField.setValue("");
                ConfigScreen.this.searchTextField.setFocused(false);
                ConfigScreen.this.minecraft.setScreen(data.getScreen());
            }).bounds(10, 5, 260, 20).build();
        }

        @Override
        void addLines(Font font, IEntryData data, String searchHighlight, List<FormattedText> lines) {
            final String comment = data.getComment();
            if (comment != null && !comment.isEmpty()) {
                lines.addAll(font.getSplitter().splitLines(comment, 200, Style.EMPTY));
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.button);
        }

        @Override
        boolean isHovered(int mouseX, int mouseY) {
            return this.button.isHovered();
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int entryTop, int entryLeft, int rowWidth, int entryHeight, int mouseX, int mouseY, boolean selected, float partialTicks) {
            this.button.setX(entryLeft - 1);
            this.button.setY(entryTop);
            this.button.render(guiGraphics, mouseX, mouseY, partialTicks);
            // only sets tooltip and hovered flag for button is updated on rendering
            super.render(guiGraphics, index, entryTop, entryLeft, rowWidth, entryHeight, mouseX, mouseY, selected, partialTicks);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(new NarratableEntry() {
                @Override
                public NarratableEntry.NarrationPriority narrationPriority() {
                    return NarratableEntry.NarrationPriority.HOVERED;
                }

                @Override
                public void updateNarration(NarrationElementOutput output) {
                    output.add(NarratedElementType.TITLE, CategoryEntry.this.getTitle());
                }
            }, CategoryEntry.this.button);
        }
    }

    private abstract class ConfigEntry<T> extends Entry {
        private static final Component RESET_TOOLTIP = Component.translatable("configmenusforge.gui.tooltip.reset");

        private final List<AbstractWidget> children = Lists.newArrayList();
        private final EntryData.ConfigEntryData<T> configEntryData;
        private final FormattedCharSequence visualTitle;
        final Button resetButton;

        public ConfigEntry(EntryData.ConfigEntryData<T> data, String searchHighlight) {
            super(data, searchHighlight);
            this.configEntryData = data;
            FormattedText truncatedTitle = ScreenTextHelper.truncateText(ConfigScreen.this.font, this.getTitle(), 260 - 70);
            this.visualTitle = Language.getInstance().getVisualOrder(truncatedTitle);
            final List<FormattedCharSequence> tooltip = ConfigScreen.this.font.split(RESET_TOOLTIP, 200);
            this.resetButton = new MutableIconButton(0, 0, 20, 20, 140, 0, ICONS_LOCATION, button -> {
                data.resetCurrentValue();
                this.onConfigValueChanged(data.getCurrentValue(), true);
                ConfigScreen.this.updateList(false);
            }) {

                @Override
                public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
                    super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
                    if (this.active && this.isHovered) ConfigScreen.this.setActiveTooltip(tooltip);
                }
            };
            this.resetButton.active = data.mayResetValue();
            this.children.add(this.resetButton);
        }

        @SuppressWarnings("unchecked")
        @Override
        void addLines(Font font, IEntryData data, String searchHighlight, List<FormattedText> lines) {
            final Component component = Component.literal(data.getPath()).withStyle(ChatFormatting.YELLOW);
            lines.addAll(font.getSplitter().splitLines(component, 200, Style.EMPTY));
            final String comment = data.getComment();
            if (comment != null && !comment.isEmpty()) {
                final List<FormattedText> splitLines = font.getSplitter().splitLines(comment, 200, Style.EMPTY);
                int rangeIndex = -1;
                // finds index of range (number types) / allowed values (enums) line
                for (int i = 0; i < splitLines.size(); i++) {
                    String text = splitLines.get(i).getString();
                    if (text.startsWith("Range: ") || text.startsWith("Allowed Values: ")) {
                        rangeIndex = i;
                        break;
                    }
                }
                // sets text color from found index to end to gray
                if (rangeIndex != -1) {
                    for (int i = rangeIndex; i < splitLines.size(); i++) {
                        splitLines.set(i, Component.literal(splitLines.get(i).getString()).withStyle(ChatFormatting.GRAY));
                    }
                }
                lines.addAll(splitLines);
            }
            EntryData.ConfigEntryData<T> configData = (EntryData.ConfigEntryData<T>) data;
            // default value
            lines.addAll(font.getSplitter().splitLines(Component.translatable("configmenusforge.gui.tooltip.default", this.valueToString(configData.getDefaultValue())).withStyle(ChatFormatting.GRAY), 200, Style.EMPTY));
            if (searchHighlight != null && !searchHighlight.isEmpty()) { // path is only added when searching as there would be no way to tell otherwise where the entry is located
                final Component pathComponent = configData.getFullPath().stream()
                        .map(ScreenTextHelper::toFormattedComponent)
                        .reduce((o1, o2) -> Component.literal("").append(o1).append(" > ").append(o2))
                        .orElse(Component.empty());
                lines.addAll(font.getSplitter().splitLines(Component.translatable("configmenusforge.gui.tooltip.path", pathComponent).withStyle(ChatFormatting.GRAY), 200, Style.EMPTY));
            }
        }

        String valueToString(T value) {
            // value converter (toString) is necessary for enum values (issue is visible when handling chatformatting values
            // which would otherwise be converted to their corresponding formatting and therefore not display)
            return value.toString();
        }

        public void onConfigValueChanged(T newValue, boolean reset) {
            this.resetButton.active = this.configEntryData.mayResetValue();
            ConfigScreen.this.updateDoneButtonState();
        }

        @Override
        public List<AbstractWidget> children() {
            return this.children;
        }

        @Override
        boolean isHovered(int mouseX, int mouseY) {
            return ConfigScreen.this.list != null && this.isMouseOver(mouseX, mouseY) && mouseX < ConfigScreen.this.list.getRowLeft() + ConfigScreen.this.list.getRowWidth() - 67;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int entryTop, int entryLeft, int rowWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            // value button start: end - 67
            // value button width: 44
            // gap: 2
            // reset button start: end - 21
            // reset button width: 20
            // yellow when hovered
            int color = this.isHovered(mouseX, mouseY) ? 16777045 : 16777215;
            guiGraphics.fill(entryLeft - 5, entryTop + 6 - 5, entryLeft + ConfigScreen.this.font.width(this.visualTitle) + 5, entryTop + 6 + ConfigScreen.this.font.lineHeight + 5, 127 << 24 & -16777216);
            guiGraphics.drawString(ConfigScreen.this.font, this.visualTitle, entryLeft, entryTop + 6, color);
            this.resetButton.setX(entryLeft + rowWidth - 21);
            this.resetButton.setY(entryTop);
            this.resetButton.render(guiGraphics, mouseX, mouseY, partialTicks);
            super.render(guiGraphics, index, entryTop, entryLeft, rowWidth, entryHeight, mouseX, mouseY, hovered, partialTicks);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            ImmutableList.Builder<NarratableEntry> builder = ImmutableList.builder();
            builder.add(new NarratableEntry() {
                @Override
                public NarratableEntry.NarrationPriority narrationPriority() {
                    return NarratableEntry.NarrationPriority.HOVERED;
                }

                @Override
                public void updateNarration(NarrationElementOutput output) {
                    String comment = ConfigEntry.this.configEntryData.getValueSpec().getComment();
                    if (comment != null) {
                        output.add(NarratedElementType.TITLE, Component.literal("").append(ConfigEntry.this.getTitle()).append(", " + comment));
                    } else {
                        output.add(NarratedElementType.TITLE, ConfigEntry.this.getTitle());
                    }
                }
            });
            builder.addAll(ConfigEntry.this.children);
            return builder.build();
        }
    }

    private class NumberEntry<T> extends ConfigEntry<T> {
        private final ConfigEditBox textField;

        public NumberEntry(EntryData.ConfigEntryData<T> configEntryData, String searchHighlight, Function<String, T> parser) {
            super(configEntryData, searchHighlight);
            this.textField = new ConfigEditBox(ConfigScreen.this.font, 0, 0, 42, 18, () -> ConfigScreen.this.activeTextField, activeTextField -> ConfigScreen.this.activeTextField = activeTextField);
            this.textField.setResponder(input -> {
                T number = null;
                try {
                    T parsed = parser.apply(input);
                    if (configEntryData.getValueSpec().test(parsed)) {
                        number = parsed;
                    }
                } catch (NumberFormatException ignored) {
                }
                if (number != null) {
                    this.textField.markInvalid(false);
                    configEntryData.setCurrentValue(number);
                    this.onConfigValueChanged(number, false);
                } else {
                    this.textField.markInvalid(true);
                    configEntryData.resetCurrentValue();
                    // provides an easy way to make text field usable again, even though default value is already set in background
                    this.resetButton.active = true;
                }
            });
            this.textField.setValue(configEntryData.getCurrentValue().toString());
            this.children().add(this.textField);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int entryTop, int entryLeft, int rowWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            super.render(guiGraphics, index, entryTop, entryLeft, rowWidth, entryHeight, mouseX, mouseY, hovered, partialTicks);
            this.textField.setX(entryLeft + rowWidth - 66);
            this.textField.setY(entryTop + 1);
            this.textField.render(guiGraphics, mouseX, mouseY, partialTicks);
        }

        @Override
        public void onConfigValueChanged(T newValue, boolean reset) {
            super.onConfigValueChanged(newValue, reset);
            if (reset) {
                this.textField.setValue(newValue.toString());
            }
        }
    }

    private class BooleanEntry extends ConfigEntry<Boolean> {
        private final Button button;

        public BooleanEntry(EntryData.ConfigEntryData<Boolean> configEntryData, String searchHighlight) {
            super(configEntryData, searchHighlight);
            this.button = Button.builder(CommonComponents.optionStatus(configEntryData.getCurrentValue()), button -> {
                final boolean newValue = !configEntryData.getCurrentValue();
                configEntryData.setCurrentValue(newValue);
                this.onConfigValueChanged(newValue, false);
            }).bounds(10, 5, 44, 20).build();
            this.children().add(this.button);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int entryTop, int entryLeft, int rowWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            super.render(guiGraphics, index, entryTop, entryLeft, rowWidth, entryHeight, mouseX, mouseY, hovered, partialTicks);
            this.button.setX(entryLeft + rowWidth - 67);
            this.button.setY(entryTop);
            this.button.render(guiGraphics, mouseX, mouseY, partialTicks);
        }

        @Override
        public void onConfigValueChanged(Boolean newValue, boolean reset) {
            super.onConfigValueChanged(newValue, reset);
            this.button.setMessage(CommonComponents.optionStatus(newValue));
        }
    }

    private abstract class EditScreenEntry<T> extends ConfigEntry<T> {
        private final Button button;

        public EditScreenEntry(EntryData.ConfigEntryData<T> configEntryData, String searchHighlight, String type) {
            this(configEntryData, searchHighlight, type, Component.translatable("configmenusforge.gui.edit"));
        }

        public EditScreenEntry(EntryData.ConfigEntryData<T> configEntryData, String searchHighlight, String type, Component message) {
            super(configEntryData, searchHighlight);
            this.button = Button.builder(message, button -> {
                // safety precaution for dealing with lists
                try {
                    ConfigScreen.this.minecraft.setScreen(this.makeEditScreen(type, configEntryData.getCurrentValue(), configEntryData.getValueSpec(), currentValue -> {
                        configEntryData.setCurrentValue(currentValue);
                        this.onConfigValueChanged(currentValue, false);
                    }));
                } catch (RuntimeException e) {
                    ForgeConfigScreens.LOGGER.warn("Unable to handle list entry containing class type {}", type, e);
                    button.active = false;
                }
            }).bounds(10, 5, 44, 20).build();
            this.children().add(this.button);
        }

        @Override
        abstract String valueToString(T value);

        abstract Screen makeEditScreen(String type, T currentValue, ForgeConfigSpec.ValueSpec valueSpec, Consumer<T> onSave);

        @Override
        public void render(GuiGraphics guiGraphics, int index, int entryTop, int entryLeft, int rowWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            super.render(guiGraphics, index, entryTop, entryLeft, rowWidth, entryHeight, mouseX, mouseY, hovered, partialTicks);
            this.button.setX(entryLeft + rowWidth - 67);
            this.button.setY(entryTop);
            this.button.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    private class EnumEntry extends EditScreenEntry<Enum<?>> {
        public EnumEntry(EntryData.ConfigEntryData<Enum<?>> configEntryData, String searchHighlight) {
            super(configEntryData, searchHighlight, "Enum", Component.translatable("configmenusforge.gui.select"));
        }

        @Override
        String valueToString(Enum<?> value) {
            return ScreenTextHelper.toFormattedString(value.name().toLowerCase(Locale.ROOT));
        }

        @Override
        Screen makeEditScreen(String type, Enum<?> currentValue, ForgeConfigSpec.ValueSpec valueSpec, Consumer<Enum<?>> onSave) {
            return new EditEnumScreen(ConfigScreen.this, Component.translatable("configmenusforge.gui.value.select", type), currentValue, currentValue.getDeclaringClass().getEnumConstants(), valueSpec::test, onSave);
        }
    }

    private class StringEntry extends EditScreenEntry<String> {
        public StringEntry(EntryData.ConfigEntryData<String> configEntryData, String searchHighlight) {
            super(configEntryData, searchHighlight, "String");
        }

        @Override
        String valueToString(String value) {
            return value;
        }

        @Override
        Screen makeEditScreen(String type, String currentValue, ForgeConfigSpec.ValueSpec valueSpec, Consumer<String> onSave) {
            return new EditStringScreen(ConfigScreen.this, Component.translatable("configmenusforge.gui.value.edit", type), currentValue, valueSpec::test, onSave);
        }
    }

    private class ListEntry<T> extends EditScreenEntry<List<T>> {
        private final Function<String, T> fromString;

        public ListEntry(EntryData.ConfigEntryData<List<T>> configEntryData, String searchHighlight, String type, Function<String, T> fromString) {
            super(configEntryData, searchHighlight, type);
            this.fromString = fromString;
        }

        @Override
        final String valueToString(List<T> value) {
            return "[" + value.stream().map(this::listValueToString).collect(Collectors.joining(", ")) + "]";
        }

        String listValueToString(Object value) {
            return value.toString();
        }

        @Override
        Screen makeEditScreen(String type, List<T> currentValue, ForgeConfigSpec.ValueSpec valueSpec, Consumer<List<T>> onSave) {
            return new EditListScreen(ConfigScreen.this, Component.translatable("configmenusforge.gui.list.edit", type), currentValue.stream()
                    .map(this::listValueToString)
                    .collect(Collectors.toList()), input -> {
                try {
                    this.fromString.apply(input);
                    return true;
                } catch (RuntimeException ignored) {
                }
                return false;
            }, list -> {
                final List<T> values = list.stream()
                        .map(this.fromString)
                        .collect(Collectors.toList());
                valueSpec.correct(valueSpec);
                onSave.accept(values);
            });
        }
    }

    private class EnumListEntry extends ListEntry<Enum<?>> {

        // mainly here to enable unchecked cast
        @SuppressWarnings("unchecked")
        public <T extends Enum<T>> EnumListEntry(EntryData.ConfigEntryData<List<Enum<?>>> configEntryData, String searchHighlight, Class<Enum<?>> clazz) {
            // enums are read as strings from file
            super(configEntryData, searchHighlight, "Enum", v -> Enum.valueOf((Class<T>) clazz, v));
        }

        @Override
        String listValueToString(Object value) {
            return value instanceof Enum<?> e ? e.name() : super.listValueToString(value);
        }
    }

    private class DangerousListEntry extends ListEntry<String> {
        public DangerousListEntry(EntryData.ConfigEntryData<List<String>> configEntryData, String searchHighlight) {
            super(configEntryData, searchHighlight, "String", s -> {
                if (s.isEmpty()) {
                    throw new IllegalArgumentException("string must not be empty");
                }
                return s;
            });
        }

        @Override
        Screen makeEditScreen(String type, List<String> currentValue, ForgeConfigSpec.ValueSpec valueSpec, Consumer<List<String>> onSave) {
            // TODO this needs to be reworked to allow the user to choose a data type as always returning a string list is not safe
            // displays a warning screen when editing a list of unknown type before allowing the edit
            Component component1 = Component.translatable("configmenusforge.gui.message.dangerous.title").withStyle(ChatFormatting.RED);
            // just a confirmation screen with a custom background
            return new ConfirmScreen(result -> {
                    if (result) {
                        ConfigScreen.this.minecraft.setScreen(super.makeEditScreen(type, currentValue, valueSpec, onSave));
                    } else {
                        ConfigScreen.this.minecraft.setScreen(ConfigScreen.this);
                    }
                }, component1, Component.translatable("configmenusforge.gui.message.dangerous.text"), CommonComponents.GUI_PROCEED, CommonComponents.GUI_BACK) {

                @Override
                public void renderDirtBackground(GuiGraphics guiGraphics) {
                    PanoramaBackgroundHelper.renderDirtBackground(guiGraphics, this.width, this.height);
                }
            };
        }
    }
}
