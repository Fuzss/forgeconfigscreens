package fuzs.forgeconfigscreens.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.forgeconfigscreens.ForgeConfigScreens;
import fuzs.forgeconfigscreens.client.gui.data.IEntryData;
import fuzs.forgeconfigscreens.client.gui.helper.ScreenTextHelper;
import fuzs.forgeconfigscreens.client.gui.screens.ConfigScreen;
import fuzs.forgeconfigscreens.client.gui.screens.SelectConfigScreen;
import fuzs.forgeconfigscreens.client.gui.screens.SelectConfigWorldScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class ConfigSelectionList extends ObjectSelectionList<ConfigSelectionList.Entry> {
    private static final ResourceLocation ICON_LOCATION = ForgeConfigScreens.id("textures/misc/config.png");
    private static final ResourceLocation ICON_DISABLED_LOCATION = ForgeConfigScreens.id("textures/misc/disabled_config.png");
    private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/world_selection.png");
    private static final Component SELECT_WORLD_TOOLTIP = Component.translatable("configmenusforge.gui.select.select_world").withStyle(ChatFormatting.GOLD);
    private static final Component NO_DATA_TOOLTIP = Component.translatable("configmenusforge.gui.select.no_data").withStyle(ChatFormatting.RED);
    private static final Component NO_PERMISSIONS_TOOLTIP = Component.translatable("configmenusforge.gui.select.no_permissions").withStyle(ChatFormatting.GOLD);
    private static final Component MULTIPLAYER_SERVER_TOOLTIP = Component.translatable("configmenusforge.gui.select.multiplayer_server").withStyle(ChatFormatting.GOLD);

    private final SelectConfigScreen screen;
    private final ConfigSelectionList.Entry loadingHeader;
    private final CompletableFuture<List<LevelSummary>> pendingLevels;
    private List<LevelSummary> levelList;
    private String filter;

    public ConfigSelectionList(SelectConfigScreen selectConfigScreen, Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight, String filter, @Nullable ConfigSelectionList list) {
        super(minecraft, width, height, y0, y1, itemHeight);
        this.screen = selectConfigScreen;
        this.filter = filter;
        this.loadingHeader = new ConfigSelectionList.LoadingHeader(this.minecraft);
        if (list != null) {
            this.pendingLevels = list.pendingLevels;
        } else {
            this.pendingLevels = this.loadLevels();
        }

        this.handleNewLevels(this.pollLevelsIgnoreErrors());
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
        this.setRenderSelection(false);
    }

    private static boolean matchesConfigSearch(ModConfig config, String query) {
        if (config.getFileName().toLowerCase(Locale.ROOT).contains(query)) {
            return true;
        } else {
            return config.getType().extension().contains(query);
        }
    }

    public void updateFilter(String filter) {
        if (this.levelList != null && !filter.equals(this.filter)) {
            this.fillLevels(filter);
        }

        this.filter = filter;
    }

    private CompletableFuture<List<LevelSummary>> loadLevels() {
        LevelStorageSource.LevelCandidates levelCandidates;
        if (this.minecraft.getConnection() == null) {
            try {
                levelCandidates = this.minecraft.getLevelSource().findLevelCandidates();
            } catch (LevelStorageException var3) {
                ForgeConfigScreens.LOGGER.error("Couldn't load level list", var3);
                return CompletableFuture.completedFuture(List.of());
            }
        } else {
            return CompletableFuture.completedFuture(List.of());
        }
        if (levelCandidates.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        } else {
            return this.minecraft.getLevelSource().loadLevelSummaries(levelCandidates).exceptionally(throwable -> {
                this.minecraft.delayCrash(CrashReport.forThrowable(throwable, "Couldn't load level list"));
                return List.of();
            });
        }
    }

    @Nullable
    private List<LevelSummary> pollLevelsIgnoreErrors() {
        try {
            return this.pendingLevels.getNow(null);
        } catch (CancellationException | CompletionException e) {
            return null;
        }
    }

    private void handleNewLevels(@Nullable List<LevelSummary> list) {
        if (list == null) {
            this.fillLoadingLevels();
        } else {
            this.fillLevels(this.filter);
        }

        this.levelList = list;
    }

    private void fillLoadingLevels() {
        this.clearEntries();
        this.addEntry(this.loadingHeader);
        this.notifyListUpdated();
    }

    private void fillLevels(String filter) {

        this.clearEntries();
        filter = filter.toLowerCase(Locale.ROOT).trim();
        List<ModConfig> toSort = new ArrayList<>();
        for (ModConfig config : this.screen.getConfigs()) {
            if (matchesConfigSearch(config, filter)) {
                toSort.add(config);
            }
        }
        toSort.sort(Comparator.<ModConfig, String>comparing(config -> config.getType().extension()).thenComparing(ConfigListEntry::getName));
        for (ModConfig config : toSort) {
            this.addEntry(new ConfigListEntry(this.screen, this.minecraft, config));
        }

        this.notifyListUpdated();
    }

    private void notifyListUpdated() {
        this.screen.triggerImmediateNarration(true);
    }

    private void updateLevelList() {
        List<LevelSummary> list = this.pollLevelsIgnoreErrors();
        if (list != this.levelList) {
            this.handleNewLevels(list);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float tickDelta) {
        this.updateLevelList();
        super.render(guiGraphics, mouseX, mouseY, tickDelta);
    }

    @Override
    protected int getScrollbarPosition() {
        return this.width / 2 + 144;
    }

    @Override
    public int getRowWidth() {
        return 260;
    }

    @Override
    public boolean isFocused() {
        return this.screen.getFocused() == this;
    }

    @Override
    public void setSelected(@Nullable Entry configListEntry) {
        super.setSelected(configListEntry);
        this.screen.updateButtonStatus(configListEntry != null && configListEntry.isSelectable());
    }

    public abstract static class Entry extends ObjectSelectionList.Entry<Entry> {

        public abstract boolean isSelectable();
    }

    public static class LoadingHeader extends Entry {
        private static final Component LOADING_LABEL = Component.translatable("selectWorld.loading_list");
        private final Minecraft minecraft;

        public LoadingHeader(Minecraft minecraft) {
            this.minecraft = minecraft;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTick) {
            int i = (this.minecraft.screen.width - this.minecraft.font.width(LOADING_LABEL)) / 2;
            int j = pTop + (pHeight - 9) / 2;
            guiGraphics.drawString(this.minecraft.font, LOADING_LABEL, i, j, 16777215);
            String s = LoadingDotsText.get(Util.getMillis());
            int k = (this.minecraft.screen.width - this.minecraft.font.width(s)) / 2;
            int l = j + 9;
            guiGraphics.drawString(this.minecraft.font, s, k, l, 8421504);
        }

        public Component getNarration() {
            return LOADING_LABEL;
        }

        public boolean isSelectable() {
            return false;
        }
    }

    public class ConfigListEntry extends Entry {
        private final SelectConfigScreen screen;
        private final Minecraft minecraft;
        private final ModConfig config;
        private final boolean mayResetValue;
        private final Component nameComponent;
        private final FormattedCharSequence fileNameComponent;
        private final Component typeComponent;
        private long lastClickTime;

        public ConfigListEntry(SelectConfigScreen selectConfigScreen, Minecraft minecraft, ModConfig config) {
            this.screen = selectConfigScreen;
            this.minecraft = minecraft;
            this.config = config;
            this.mayResetValue = selectConfigScreen.getValueToDataMap(config).values().stream().anyMatch(IEntryData::mayResetValue);
            this.nameComponent = this.mayResetValue ? Component.literal(getName(config)).withStyle(ChatFormatting.ITALIC) : Component.literal(getName(config));
//            this.fileNameComponent = Component.literal(config.getFileName());
            this.fileNameComponent = Language.getInstance().getVisualOrder(ScreenTextHelper.truncateText(minecraft.font, Component.literal(config.getFileName()), 220));
            String extension = config.getType().extension();
            this.typeComponent = Component.translatable("configmenusforge.gui.type.title", StringUtils.capitalize(extension));
        }

        static String getName(ModConfig config) {
            String fullName = config.getFileName();
            int start = fullName.lastIndexOf(File.separator) + 1;
            int end = fullName.lastIndexOf(".");
            return fullName.substring(start, end < start ? fullName.length() : end);
        }

        @Override
        public Component getNarration() {
            Component component = Component.literal(getName(this.config));
            if (this.invalidData()) {
                component = CommonComponents.joinForNarration(component, ConfigSelectionList.NO_DATA_TOOLTIP);
            } else if (this.noPermissions()) {
                component = CommonComponents.joinForNarration(component, ConfigSelectionList.NO_PERMISSIONS_TOOLTIP);
            }

            return Component.translatable("narrator.select", component);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int entryTop, int entryLeft, int rowWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            Font font = this.minecraft.font;
            guiGraphics.drawString(this.minecraft.font, this.nameComponent, entryLeft + 32 + 3, entryTop + 1, 16777215);
            guiGraphics.drawString(this.minecraft.font, this.fileNameComponent, entryLeft + 32 + 3, entryTop + 9 + 3, 8421504);
            guiGraphics.drawString(this.minecraft.font, this.typeComponent, entryLeft + 32 + 3, entryTop + 9 + 9 + 3, 8421504);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            guiGraphics.blit(this.isDisabled() ? ConfigSelectionList.ICON_DISABLED_LOCATION : ConfigSelectionList.ICON_LOCATION, entryLeft, entryTop, 0.0F, 0.0F, 32, 32, 32, 32);
            RenderSystem.disableBlend();
            if (this.minecraft.options.touchscreen().get() || hovered) {
                guiGraphics.fill(entryLeft, entryTop, entryLeft + 32, entryTop + 32, -1601138544);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                boolean leftHovered = mouseX - entryLeft < 32;
                int textureY = leftHovered ? 32 : 0;
                if (this.needsWorldInstance()) {
                    guiGraphics.blit(ConfigSelectionList.ICON_OVERLAY_LOCATION, entryLeft, entryTop, 32.0F, textureY, 32, 32, 256, 256);
                    guiGraphics.blit(ConfigSelectionList.ICON_OVERLAY_LOCATION, entryLeft, entryTop, 64.0F, textureY, 32, 32, 256, 256);
                    if (leftHovered) {
                        this.screen.setActiveTooltip(this.minecraft.font.split(ConfigSelectionList.SELECT_WORLD_TOOLTIP, 200));
                    }
                } else if (this.invalidData()) {
                    guiGraphics.blit(ConfigSelectionList.ICON_OVERLAY_LOCATION, entryLeft, entryTop, 96.0F, textureY, 32, 32, 256, 256);
                    if (leftHovered) {
                        this.screen.setActiveTooltip(this.minecraft.font.split(ConfigSelectionList.NO_DATA_TOOLTIP, 200));
                    }
                } else if (this.noPermissions()) {
                    guiGraphics.blit(ConfigSelectionList.ICON_OVERLAY_LOCATION, entryLeft, entryTop, 64.0F, textureY, 32, 32, 256, 256);
                    if (leftHovered) {
                        this.screen.setActiveTooltip(this.minecraft.font.split(ConfigSelectionList.NO_PERMISSIONS_TOOLTIP, 200));
                    }
                } else if (this.onMultiplayerServer()) {
                    guiGraphics.blit(ConfigSelectionList.ICON_OVERLAY_LOCATION, entryLeft, entryTop, 32.0F, textureY, 32, 32, 256, 256);
                    guiGraphics.blit(ConfigSelectionList.ICON_OVERLAY_LOCATION, entryLeft, entryTop, 64.0F, textureY, 32, 32, 256, 256);
                    if (leftHovered) {
                        this.screen.setActiveTooltip(this.minecraft.font.split(ConfigSelectionList.MULTIPLAYER_SERVER_TOOLTIP, 200));
                    }
                } else {
                    guiGraphics.blit(ConfigSelectionList.ICON_OVERLAY_LOCATION, entryLeft, entryTop, 0.0F, textureY, 32, 32, 256, 256);
                }
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.isDisabled()) {
                return true;
            } else {
                ConfigSelectionList.this.setSelected(this);
                this.screen.updateButtonStatus(ConfigSelectionList.this.getSelected() != null);
                if (mouseX - (double) ConfigSelectionList.this.getRowLeft() <= 32.0D) {
                    this.openConfig();
                    return true;
                } else if (Util.getMillis() - this.lastClickTime < 250L) {
                    this.openConfig();
                    return true;
                } else {
                    this.lastClickTime = Util.getMillis();
                    return false;
                }
            }
        }

        public void openConfig() {
            if (this.needsWorldInstance()) {
                this.selectWorld();
            } else {
                Screen screen = ConfigScreen.create(this.screen, this.screen.getDisplayName(), this.config, this.screen.getValueToDataMap(this.config));
                this.minecraft.setScreen(screen);
            }
        }

        private void selectWorld() {
            Screen screen = new SelectConfigWorldScreen(this.screen, this.screen.getDisplayName(), this.config, ConfigSelectionList.this.levelList);
            this.minecraft.setScreen(screen);
        }

        public boolean invalidData() {
            return this.screen.getValueToDataMap(this.config).isEmpty();
        }

        public boolean needsWorldInstance() {
            // just display as invalid if there are no worlds to select server configs from
            // check if world is loaded as Forge doesn't clean up server config after leaving world
            return !ConfigSelectionList.this.levelList.isEmpty() && this.config.getType() == ModConfig.Type.SERVER && this.minecraft.getConnection() == null;
        }

        private boolean noPermissions() {
            return this.config.getType() == ModConfig.Type.SERVER && !this.screen.getServerPermissions();
        }

        public boolean onMultiplayerServer() {
            return this.config.getType() == ModConfig.Type.SERVER && !this.minecraft.isLocalServer();
        }

        boolean isDisabled() {
            return !this.needsWorldInstance() && (this.invalidData() || this.noPermissions());
        }

        public ModConfig getConfig() {
            return this.config;
        }

        public boolean mayResetValue() {
            return this.mayResetValue;
        }

        @Override
        public boolean isSelectable() {
            return !this.isDisabled();
        }
    }
}
