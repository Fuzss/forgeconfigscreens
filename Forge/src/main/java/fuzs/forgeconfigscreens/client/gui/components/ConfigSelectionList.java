package fuzs.forgeconfigscreens.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.forgeconfigscreens.ForgeConfigScreens;
import fuzs.forgeconfigscreens.client.gui.data.IEntryData;
import fuzs.forgeconfigscreens.client.gui.screens.ConfigScreen;
import fuzs.forgeconfigscreens.client.gui.screens.SelectConfigScreen;
import fuzs.forgeconfigscreens.client.gui.screens.SelectConfigWorldScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Comparator;
import java.util.Locale;

public class ConfigSelectionList extends ObjectSelectionList<ConfigSelectionList.ConfigListEntry> {
	private static final ResourceLocation ICON_LOCATION = ForgeConfigScreens.id("textures/misc/config.png");
	private static final ResourceLocation ICON_DISABLED_LOCATION = ForgeConfigScreens.id("textures/misc/disabled_config.png");
	private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/world_selection.png");
	private static final Component SELECT_WORLD_TOOLTIP = Component.translatable("configmenusforge.gui.select.select_world").withStyle(ChatFormatting.GOLD);
	private static final Component NO_DATA_TOOLTIP = Component.translatable("configmenusforge.gui.select.no_data").withStyle(ChatFormatting.RED);
	private static final Component NO_PERMISSIONS_TOOLTIP = Component.translatable("configmenusforge.gui.select.no_permissions").withStyle(ChatFormatting.GOLD);
	private static final Component MULTIPLAYER_SERVER_TOOLTIP = Component.translatable("configmenusforge.gui.select.multiplayer_server").withStyle(ChatFormatting.GOLD);

	private final SelectConfigScreen screen;

	public ConfigSelectionList(SelectConfigScreen selectConfigScreen, Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight, String query) {
		super(minecraft, width, height, y0, y1, itemHeight);
		this.screen = selectConfigScreen;
		this.refreshList(query);
	}

	public void refreshList(String query) {
		this.clearEntries();
		this.setSelected(null);
		final String lowerCaseQuery = query.toLowerCase(Locale.ROOT).trim();
		this.screen.getConfigs().stream()
				.filter(config -> matchesConfigSearch(config, lowerCaseQuery))
				.sorted(Comparator.<ModConfig, String>comparing(config -> config.getType().extension()).thenComparing(ConfigListEntry::getName))
				.forEach(config -> this.addEntry(new ConfigListEntry(this.screen, this.minecraft, config)));
	}

	private static boolean matchesConfigSearch(ModConfig config, String query) {
		if (config.getFileName().toLowerCase(Locale.ROOT).contains(query)) {
			return true;
		} else {
			return config.getType().extension().contains(query);
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

	@Override
	public boolean isFocused() {
		return this.screen.getFocused() == this;
	}

	@Override
	public void setSelected(@Nullable ConfigListEntry configListEntry) {
		super.setSelected(configListEntry);
		this.screen.updateButtonStatus(configListEntry != null && !configListEntry.isDisabled());
	}

	public class ConfigListEntry extends Entry<ConfigListEntry> {
		private final SelectConfigScreen screen;
		private final Minecraft minecraft;
		private final ModConfig config;
		private final boolean mayResetValue;
		private final Component nameComponent;
		private final Component fileNameComponent;
		private final Component typeComponent;
		private long lastClickTime;

		public ConfigListEntry(SelectConfigScreen selectConfigScreen, Minecraft minecraft, ModConfig config) {
			this.screen = selectConfigScreen;
			this.minecraft = minecraft;
			this.config = config;
			this.mayResetValue = selectConfigScreen.getValueToDataMap(config).values().stream().anyMatch(IEntryData::mayResetValue);
			this.nameComponent = this.mayResetValue ? Component.literal(getName(config)).withStyle(ChatFormatting.ITALIC) : Component.literal(getName(config));
			this.fileNameComponent = Component.literal(config.getFileName());
			String extension = config.getType().extension();
			this.typeComponent = Component.translatable("configmenusforge.gui.type.title", StringUtils.capitalize(extension));
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
		public void render(PoseStack poseStack, int index, int entryTop, int entryLeft, int rowWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float partialTicks) {
			Font font = this.minecraft.font;
			font.draw(poseStack, this.nameComponent, (float)(entryLeft + 32 + 3), (float)(entryTop + 1), 16777215);
			font.draw(poseStack, this.fileNameComponent, (float)(entryLeft + 32 + 3), (float)(entryTop + 9 + 3), 8421504);
			font.draw(poseStack, this.typeComponent, (float)(entryLeft + 32 + 3), (float)(entryTop + 9 + 9 + 3), 8421504);
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShaderTexture(0, this.isDisabled() ? ConfigSelectionList.ICON_DISABLED_LOCATION : ConfigSelectionList.ICON_LOCATION);
			RenderSystem.enableBlend();
			GuiComponent.blit(poseStack, entryLeft, entryTop, 0.0F, 0.0F, 32, 32, 32, 32);
			RenderSystem.disableBlend();
			if (this.minecraft.options.touchscreen().get() || hovered) {
				RenderSystem.setShaderTexture(0, ConfigSelectionList.ICON_OVERLAY_LOCATION);
				GuiComponent.fill(poseStack, entryLeft, entryTop, entryLeft + 32, entryTop + 32, -1601138544);
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				boolean leftHovered = mouseX - entryLeft < 32;
				int textureY = leftHovered ? 32 : 0;
				if (this.needsWorldInstance()) {
					GuiComponent.blit(poseStack, entryLeft, entryTop, 32.0F, (float)textureY, 32, 32, 256, 256);
					GuiComponent.blit(poseStack, entryLeft, entryTop, 64.0F, textureY, 32, 32, 256, 256);
					if (leftHovered) {
						this.screen.setActiveTooltip(this.minecraft.font.split(ConfigSelectionList.SELECT_WORLD_TOOLTIP, 200));
					}
				} else if (this.invalidData()) {
					GuiComponent.blit(poseStack, entryLeft, entryTop, 96.0F, (float)textureY, 32, 32, 256, 256);
					if (leftHovered) {
						this.screen.setActiveTooltip(this.minecraft.font.split(ConfigSelectionList.NO_DATA_TOOLTIP, 200));
					}
				} else if (this.noPermissions()) {
					GuiComponent.blit(poseStack, entryLeft, entryTop, 64.0F, textureY, 32, 32, 256, 256);
					if (leftHovered) {
						this.screen.setActiveTooltip(this.minecraft.font.split(ConfigSelectionList.NO_PERMISSIONS_TOOLTIP, 200));
					}
				} else if (this.onMultiplayerServer()) {
					GuiComponent.blit(poseStack, entryLeft, entryTop, 32.0F, (float)textureY, 32, 32, 256, 256);
					GuiComponent.blit(poseStack, entryLeft, entryTop, 64.0F, textureY, 32, 32, 256, 256);
					if (leftHovered) {
						this.screen.setActiveTooltip(this.minecraft.font.split(ConfigSelectionList.MULTIPLAYER_SERVER_TOOLTIP, 200));
					}
				} else {
					GuiComponent.blit(poseStack, entryLeft, entryTop, 0.0F, (float)textureY, 32, 32, 256, 256);
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
			Screen screen = new SelectConfigWorldScreen(this.screen, this.screen.getDisplayName(), this.config, this.screen.getLevelList());
			this.minecraft.setScreen(screen);
		}

		static String getName(ModConfig config) {
			String fullName = config.getFileName();
			int start = fullName.lastIndexOf(File.separator) + 1;
			int end = fullName.lastIndexOf(".");
			return fullName.substring(start, end < start ? fullName.length() : end);
		}

		public boolean invalidData() {
			return this.screen.getValueToDataMap(this.config).isEmpty();
		}

		public boolean needsWorldInstance() {
			// just display as invalid if there are no worlds to select server configs from anyways
			// check if world is loaded as Forge doesn't clean up server config after leaving world
			return !this.screen.getLevelList().isEmpty() && this.config.getType() == ModConfig.Type.SERVER && this.minecraft.getConnection() == null;
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
	}
}
