package fuzs.forgeconfigscreens.client.gui.screens;

import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.forgeconfigscreens.ForgeConfigScreens;
import fuzs.forgeconfigscreens.ForgeConfigScreensForge;
import fuzs.forgeconfigscreens.client.gui.components.ConfigSelectionList;
import fuzs.forgeconfigscreens.client.gui.data.IEntryData;
import fuzs.forgeconfigscreens.client.gui.util.ScreenUtil;
import fuzs.forgeconfigscreens.client.gui.widget.AnimatedIconButton;
import fuzs.forgeconfigscreens.client.util.ServerConfigUploader;
import fuzs.forgeconfigscreens.lib.core.ModLoaderEnvironment;
import fuzs.forgeconfigscreens.network.client.message.C2SAskPermissionsMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("ConstantConditions")
public class SelectConfigScreen extends Screen {
	private final Screen lastScreen;
	private final ResourceLocation background;
	private final Component displayName;
	private final Map<ModConfig, Map<Object, IEntryData>> configs;
	private List<FormattedCharSequence> activeTooltip;
	private EditBox searchBox;
	private ConfigSelectionList list;
	private Button openButton;
	private Button selectButton;
	private Button restoreButton;
	private Button copyButton;
	private Button fileButton;
	private AnimatedIconButton tinyJumperButton;
	private boolean serverPermissions;
	private final List<LevelSummary> levelList;

	public SelectConfigScreen(Screen lastScreen, Component displayName, ResourceLocation optionsBackground, Set<ModConfig> configs) {
		super(Component.translatable("configmenusforge.gui.select.title", displayName));
		this.lastScreen = lastScreen;
		this.displayName = displayName;
		this.background = optionsBackground;
		this.configs = configs.stream().collect(Collectors.collectingAndThen(Collectors.toMap(Function.identity(), IEntryData::makeValueToDataMap), ImmutableMap::copyOf));
		// this.minecraft hasn't been set yet
		Minecraft minecraft = Minecraft.getInstance();
		this.levelList = this.getLevelList(minecraft);
		this.initServerPermissions(minecraft);
	}

	private List<LevelSummary> getLevelList(Minecraft minecraft) {
		LevelStorageSource levelstoragesource = minecraft.getLevelSource();
		List<LevelSummary> list = null;
		// will trigger OverlappingFileLockException when called and a world is loaded
		if (minecraft.getConnection() == null) {
			try {
				list = levelstoragesource.getLevelList();
				Collections.sort(list);
			} catch (LevelStorageException levelstorageexception) {
				ForgeConfigScreens.LOGGER.error("Couldn't load level list", levelstorageexception);
			}
		}
		return list != null ? ImmutableList.copyOf(list) : ImmutableList.of();
	}

	@Override
	public void tick() {
		this.searchBox.tick();
		// makes tiny person wave when hovered
		this.tinyJumperButton.tick();
	}

	@Override
	protected void init() {
		this.searchBox = new EditBox(this.font, this.width / 2 - 121, 22, 242, 20, this.searchBox, Component.empty()) {

			@Override
			public boolean mouseClicked(double mouseX, double mouseY, int button) {
				// left click clears text
				if (this.isVisible() && button == 1) {
					this.setValue("");
				}
				return super.mouseClicked(mouseX, mouseY, button);
			}
		};
		this.searchBox.setResponder(query -> this.list.refreshList(query));
		this.addWidget(this.searchBox);
		this.addRenderableWidget(new Button(this.width / 2 + 4, this.height - 28, 150, 20, CommonComponents.GUI_DONE, button -> this.onClose()));
		this.openButton = this.addRenderableWidget(new Button(this.width / 2 - 50 - 104, this.height - 52, 100, 20, Component.translatable("configmenusforge.gui.select.edit"), button1 -> {
			final ConfigSelectionList.ConfigListEntry selected = this.list.getSelected();
			if (selected != null) {
				selected.openConfig();
			}
		}));
		this.selectButton = this.addRenderableWidget(new Button(this.width / 2 - 50 - 104, this.height - 52, 100, 20, Component.translatable("configmenusforge.gui.select.world.edit"), button1 -> {
			final ConfigSelectionList.ConfigListEntry selected = this.list.getSelected();
			if (selected != null) {
				selected.openConfig();
			}
		}));
		this.restoreButton = this.addRenderableWidget(new Button(this.width / 2 - 50, this.height - 52, 100, 20, Component.translatable("configmenusforge.gui.select.restore"), button1 -> {
			final ConfigSelectionList.ConfigListEntry selected = this.list.getSelected();
			if (selected != null) {
				Component component2 = Component.translatable("configmenusforge.gui.message.restore.warning").withStyle(ChatFormatting.RED);
				Screen confirmScreen = new ConfirmScreen(result1 -> {
					if (result1) {
						final ModConfig config = selected.getConfig();
						this.getValueToDataMap(config).values().forEach(data -> {
							data.resetCurrentValue();
							data.saveConfigValue();
						});
						ServerConfigUploader.saveAndUpload(config);
					}
					this.minecraft.setScreen(this);
				}, Component.translatable("configmenusforge.gui.message.restore.title"), component2);
				this.minecraft.setScreen(confirmScreen);
			}
		}));
		this.copyButton = this.addRenderableWidget(new Button(this.width / 2 - 50 + 104, this.height - 52, 100, 20, Component.translatable("configmenusforge.gui.select.copy"), button -> {
			final ConfigSelectionList.ConfigListEntry selected = this.list.getSelected();
			if (selected != null) {
				final ModConfig config = selected.getConfig();
				Path destination = ModLoaderEnvironment.getGameDir().resolve(FMLConfig.defaultConfigPath()).resolve(config.getFileName());
				Component component2 = Files.exists(destination) ? Component.translatable("configmenusforge.gui.message.copy.warning").withStyle(ChatFormatting.RED) : Component.translatable("configmenusforge.gui.message.copy.description");
				this.minecraft.setScreen(new ConfirmScreen(result -> {
					if (result) {
						try {
							if (!Files.exists(destination)) {
								FileUtils.getOrCreateDirectory(destination.getParent(), String.format("%s default config", config.getFileName()));
								Files.createFile(destination);
							}
							TomlFormat.instance().createWriter().write(config.getConfigData(), destination, WritingMode.REPLACE);
							ForgeConfigScreens.LOGGER.info("Successfully copied {} to default config folder", config.getFileName());
						} catch (Exception e) {
							ForgeConfigScreens.LOGGER.error("Failed to copy {} to default config folder", config.getFileName(), e);
						}
					}
					this.minecraft.setScreen(this);
				}, Component.translatable("configmenusforge.gui.message.copy.title"), component2));
			}
		}));
		this.fileButton = this.addRenderableWidget(new Button(this.width / 2 - 154, this.height - 28, 150, 20, Component.translatable("configmenusforge.gui.select.open"), button -> {
			final ConfigSelectionList.ConfigListEntry selected = this.list.getSelected();
			if (selected != null) {
				final Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, selected.getConfig().getFullPath().toAbsolutePath().toString()));
				this.handleComponentClicked(style);
			}
		}));
		this.updateButtonStatus(false);
		this.list = new ConfigSelectionList(this, this.minecraft, this.width, this.height, 50, this.height - 60, 36, this.searchBox.getValue());
		this.addWidget(this.list);
		this.tinyJumperButton = this.addRenderableWidget(ScreenUtil.makeModPageButton(this.width / 2 + 126, 22, this.font, this::handleComponentClicked, this::renderTooltip));
		this.setInitialFocus(this.searchBox);
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		return super.keyPressed(i, j, k) || this.searchBox.keyPressed(i, j, k);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}

	@Override
	public boolean charTyped(char c, int i) {
		return this.searchBox.charTyped(c, i);
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		this.activeTooltip = null;
		this.list.render(poseStack, mouseX, mouseY, partialTicks);
		this.searchBox.render(poseStack, mouseX, mouseY, partialTicks);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 7, 16777215);
		super.render(poseStack, mouseX, mouseY, partialTicks);
		if (this.activeTooltip != null) {
			this.renderTooltip(poseStack, this.activeTooltip, mouseX, mouseY);
		}
	}

	public void updateButtonStatus(boolean active) {
		if (this.list != null && active) {
			final ConfigSelectionList.ConfigListEntry selected = this.list.getSelected();
			final boolean needsWorldInstance = selected.needsWorldInstance();
			this.openButton.visible = !needsWorldInstance;
			this.selectButton.visible = needsWorldInstance;
			this.openButton.active = true;
			this.restoreButton.active = !needsWorldInstance && selected.mayResetValue();
			this.fileButton.active = !needsWorldInstance && !selected.onMultiplayerServer();
			this.copyButton.active = !needsWorldInstance;
		} else {
			this.openButton.visible = true;
			this.openButton.active = false;
			this.selectButton.visible = false;
			this.restoreButton.active = false;
			this.fileButton.active = false;
			this.copyButton.active = false;
		}
	}

	public void setActiveTooltip(List<FormattedCharSequence> list) {
		this.activeTooltip = list;
	}

	public Component getDisplayName() {
		return this.displayName;
	}

	public ResourceLocation getBackground() {
		return this.background;
	}

	public Set<ModConfig> getConfigs() {
		return this.configs.keySet();
	}

	public Map<Object, IEntryData> getValueToDataMap(ModConfig config) {
		return this.configs.get(config);
	}

	public List<LevelSummary> getLevelList() {
		return this.levelList;
	}

	private void initServerPermissions(Minecraft minecraft) {
		if (minecraft.getConnection() != null) {
			if (minecraft.isLocalServer()) {
				this.serverPermissions = true;
			} else {
				ForgeConfigScreensForge.NETWORK.sendToServer(new C2SAskPermissionsMessage());
			}
		}
	}

	public boolean getServerPermissions() {
		return this.serverPermissions;
	}

	public void setServerPermissions() {
		this.serverPermissions = true;
	}
}
