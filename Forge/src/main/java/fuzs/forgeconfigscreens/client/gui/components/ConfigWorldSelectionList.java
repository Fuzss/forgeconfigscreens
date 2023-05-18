package fuzs.forgeconfigscreens.client.gui.components;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.forgeconfigscreens.ForgeConfigScreens;
import fuzs.forgeconfigscreens.client.gui.data.IEntryData;
import fuzs.forgeconfigscreens.client.gui.screens.ConfigScreen;
import fuzs.forgeconfigscreens.client.gui.screens.SelectConfigWorldScreen;
import fuzs.forgeconfigscreens.client.util.ModConfigSync;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConfigWorldSelectionList extends ObjectSelectionList<ConfigWorldSelectionList.ConfigWorldListEntry> {
   private static final DateFormat DATE_FORMAT = new SimpleDateFormat();
   private static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
   private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/world_selection.png");
   private static final Component FROM_NEWER_TOOLTIP_1 = Component.translatable("selectWorld.tooltip.fromNewerVersion1").withStyle(ChatFormatting.RED);
   private static final Component FROM_NEWER_TOOLTIP_2 = Component.translatable("selectWorld.tooltip.fromNewerVersion2").withStyle(ChatFormatting.RED);
   private static final Component SNAPSHOT_TOOLTIP_1 = Component.translatable("selectWorld.tooltip.snapshot1").withStyle(ChatFormatting.GOLD);
   private static final Component SNAPSHOT_TOOLTIP_2 = Component.translatable("selectWorld.tooltip.snapshot2").withStyle(ChatFormatting.GOLD);
   private static final Component WORLD_LOCKED_TOOLTIP = Component.translatable("selectWorld.locked").withStyle(ChatFormatting.RED);
   private static final Component WORLD_REQUIRES_CONVERSION = Component.translatable("selectWorld.conversion.tooltip").withStyle(ChatFormatting.RED);
   private static final String SERVER_CONFIG_NAME = "serverconfig";
   private static final Marker CONFIG = MarkerFactory.getMarker("CONFIG");

   private final SelectConfigWorldScreen screen;
   private final List<LevelSummary> levelList;
   private final Component createConfigTooltip;

   public ConfigWorldSelectionList(SelectConfigWorldScreen selectConfigWorldScreen, Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight, String query, List<LevelSummary> levelList) {
      super(minecraft, width, height, y0, y1, itemHeight);
      this.screen = selectConfigWorldScreen;
      this.levelList = levelList;
      this.createConfigTooltip = Component.translatable("configmenusforge.gui.select.create_config", selectConfigWorldScreen.getDisplayName()).withStyle(ChatFormatting.GOLD);
      this.refreshList(query);
   }

   public void refreshList(String query) {
      this.clearEntries();
      String s = query.toLowerCase(Locale.ROOT);
      for (LevelSummary levelsummary : this.levelList) {
         if (levelsummary.getLevelName().toLowerCase(Locale.ROOT).contains(s) || levelsummary.getLevelId().toLowerCase(Locale.ROOT).contains(s)) {
            this.addEntry(new ConfigWorldListEntry(this.screen, this.minecraft, levelsummary));
         }
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
   public void setSelected(@Nullable ConfigWorldListEntry configWorldListEntry) {
      super.setSelected(configWorldListEntry);
      this.screen.updateButtonStatus(configWorldListEntry != null && !configWorldListEntry.summary.isDisabled());
   }

   public final class ConfigWorldListEntry extends Entry<ConfigWorldListEntry> implements AutoCloseable {
      private final SelectConfigWorldScreen screen;
      private final Minecraft minecraft;
      final LevelSummary summary;
      private final ResourceLocation iconLocation;
      private File iconFile;
      @Nullable
      private final DynamicTexture icon;
      private long lastClickTime;

      public ConfigWorldListEntry(SelectConfigWorldScreen selectConfigWorldScreen, Minecraft minecraft, LevelSummary levelSummary) {
         this.screen = selectConfigWorldScreen;
         this.summary = levelSummary;
         this.minecraft = minecraft;
         String s = levelSummary.getLevelId();
         this.iconLocation = new ResourceLocation("minecraft", "worlds/" + Util.sanitizeName(s, ResourceLocation::validPathChar) + "/" + Hashing.sha1().hashUnencodedChars(s) + "/icon");
         this.iconFile = levelSummary.getIcon().toFile();
         if (!this.iconFile.isFile()) {
            this.iconFile = null;
         }
         this.icon = this.loadServerIcon();
      }

      @Override
      public Component getNarration() {
         Component translatablecomponent = Component.translatable("narrator.select.world", this.summary.getLevelName(), new Date(this.summary.getLastPlayed()), this.summary.isHardcore() ? Component.translatable("gameMode.hardcore") : Component.translatable("gameMode." + this.summary.getGameMode().getName()), this.summary.hasCheats() ? Component.translatable("selectWorld.cheats") : Component.empty(), this.summary.getWorldVersionName());
         Component component;
         if (this.summary.isLocked()) {
            component = CommonComponents.joinForNarration(translatablecomponent, ConfigWorldSelectionList.WORLD_LOCKED_TOOLTIP);
         } else {
            component = translatablecomponent;
         }
         return Component.translatable("narrator.select", component);
      }

      @Override
      public void render(PoseStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean hovered, float pPartialTicks) {
         String s = this.summary.getLevelName();
         String s1 = this.summary.getLevelId() + " (" + ConfigWorldSelectionList.DATE_FORMAT.format(new Date(this.summary.getLastPlayed())) + ")";
         if (StringUtils.isEmpty(s)) {
            s = I18n.get("selectWorld.world") + " " + (pIndex + 1);
         }
         Component component = this.summary.getInfo();
         this.minecraft.font.draw(pMatrixStack, s, (float)(pLeft + 32 + 3), (float)(pTop + 1), 16777215);
         this.minecraft.font.draw(pMatrixStack, s1, (float)(pLeft + 32 + 3), (float)(pTop + 9 + 3), 8421504);
         this.minecraft.font.draw(pMatrixStack, component, (float)(pLeft + 32 + 3), (float)(pTop + 9 + 9 + 3), 8421504);
         RenderSystem.setShader(GameRenderer::getPositionTexShader);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.setShaderTexture(0, this.icon != null ? this.iconLocation : ConfigWorldSelectionList.ICON_MISSING);
         RenderSystem.enableBlend();
         GuiComponent.blit(pMatrixStack, pLeft, pTop, 0.0F, 0.0F, 32, 32, 32, 32);
         RenderSystem.disableBlend();
         if (this.minecraft.options.touchscreen().get() || hovered) {
            RenderSystem.setShaderTexture(0, ConfigWorldSelectionList.ICON_OVERLAY_LOCATION);
            GuiComponent.fill(pMatrixStack, pLeft, pTop, pLeft + 32, pTop + 32, -1601138544);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            boolean leftHovered = pMouseX - pLeft < 32;
            int textureY = leftHovered ? 32 : 0;
            if (this.summary.isLocked()) {
               GuiComponent.blit(pMatrixStack, pLeft, pTop, 96.0F, (float)textureY, 32, 32, 256, 256);
               if (leftHovered) {
                  this.screen.setActiveTooltip(this.minecraft.font.split(ConfigWorldSelectionList.WORLD_LOCKED_TOOLTIP, 175));
               }
            } else if (this.summary.requiresManualConversion()) {
               GuiComponent.blit(pMatrixStack, pLeft, pTop, 96.0F, 32.0F, 32, 32, 256, 256);
               if (leftHovered) {
                  this.screen.setActiveTooltip(this.minecraft.font.split(ConfigWorldSelectionList.WORLD_REQUIRES_CONVERSION, 175));
               }
            } else if (this.summary.markVersionInList()) {
               GuiComponent.blit(pMatrixStack, pLeft, pTop, 32.0F, (float)textureY, 32, 32, 256, 256);
               if (this.summary.askToOpenWorld()) {
                  GuiComponent.blit(pMatrixStack, pLeft, pTop, 96.0F, (float)textureY, 32, 32, 256, 256);
                  if (leftHovered) {
                     this.screen.setActiveTooltip(ImmutableList.of(ConfigWorldSelectionList.FROM_NEWER_TOOLTIP_1.getVisualOrderText(), ConfigWorldSelectionList.FROM_NEWER_TOOLTIP_2.getVisualOrderText()));
                  }
               } else if (!SharedConstants.getCurrentVersion().isStable()) {
                  GuiComponent.blit(pMatrixStack, pLeft, pTop, 64.0F, (float)textureY, 32, 32, 256, 256);
                  if (leftHovered) {
                     this.screen.setActiveTooltip(ImmutableList.of(ConfigWorldSelectionList.SNAPSHOT_TOOLTIP_1.getVisualOrderText(), ConfigWorldSelectionList.SNAPSHOT_TOOLTIP_2.getVisualOrderText()));
                  }
               }
            } else if (!this.fileExists()) {
               GuiComponent.blit(pMatrixStack, pLeft, pTop, 32.0F, (float)textureY, 32, 32, 256, 256);
               GuiComponent.blit(pMatrixStack, pLeft, pTop, 64.0F, (float)textureY, 32, 32, 256, 256);
               if (leftHovered) {
                  this.screen.setActiveTooltip(this.minecraft.font.split(ConfigWorldSelectionList.this.createConfigTooltip, 200));
               }
            } else {
               GuiComponent.blit(pMatrixStack, pLeft, pTop, 0.0F, (float)textureY, 32, 32, 256, 256);
            }
         }
      }

      @Override
      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         if (this.summary.isDisabled()) {
            return true;
         } else {
            ConfigWorldSelectionList.this.setSelected(this);
            this.screen.updateButtonStatus(ConfigWorldSelectionList.this.getSelected() != null);
            if (mouseX - (double) ConfigWorldSelectionList.this.getRowLeft() <= 32.0D) {
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
         this.loadModConfig(this.screen.getConfig());
         final ConfigScreen configScreen = ConfigScreen.create(this.screen, this.screen.getDisplayName(), this.screen.getConfig(), IEntryData.makeValueToDataMap(this.screen.getConfig()));
         this.minecraft.setScreen(configScreen);
      }

      public void loadModConfig(ModConfig config) {
         loadModConfig(config, this.getConfigBasePath());
      }

      private static void loadModConfig(ModConfig config, Path configBasePath) {
         // just like ConfigTracker::openConfig
         ForgeConfigScreens.LOGGER.trace(CONFIG, "Loading config file type {} at {} for {}", config.getType(), config.getFileName(), config.getModId());
         FileUtils.getOrCreateDirectory(configBasePath, SERVER_CONFIG_NAME);
         final CommentedFileConfig configData = config.getHandler().reader(configBasePath).apply(config);
         // setting mod server config data without a loaded level shouldn't be a problem, Forge itself doesn't even clean up when a level is unloaded
         ModConfigSync.setConfigData(config, configData);
         // don't fire config loading event here as Forge does, mods might expect a loaded world to be present
         config.save();
      }

      private Path getConfigBasePath() {
         return this.minecraft.getLevelSource().getBaseDir().resolve(this.summary.getLevelId()).resolve(SERVER_CONFIG_NAME);
      }

      public boolean fileExists() {
         return Files.exists(this.getConfigBasePath().resolve(this.screen.getConfig().getFileName()));
      }

      @Nullable
      private DynamicTexture loadServerIcon() {
         boolean flag = this.iconFile != null && this.iconFile.isFile();
         if (flag) {
            try {
               InputStream inputstream = new FileInputStream(this.iconFile);
               DynamicTexture dynamictexture1;
               try {
                  NativeImage nativeimage = NativeImage.read(inputstream);
                  Validate.validState(nativeimage.getWidth() == 64, "Must be 64 pixels wide");
                  Validate.validState(nativeimage.getHeight() == 64, "Must be 64 pixels high");
                  DynamicTexture dynamictexture = new DynamicTexture(nativeimage);
                  this.minecraft.getTextureManager().register(this.iconLocation, dynamictexture);
                  dynamictexture1 = dynamictexture;
               } catch (Throwable throwable1) {
                  try {
                     inputstream.close();
                  } catch (Throwable throwable) {
                     throwable1.addSuppressed(throwable);
                  }
                  throw throwable1;
               }
               inputstream.close();
               return dynamictexture1;
            } catch (Throwable throwable2) {
               ForgeConfigScreens.LOGGER.error("Invalid icon for world {}", this.summary.getLevelId(), throwable2);
               this.iconFile = null;
               return null;
            }
         } else {
            this.minecraft.getTextureManager().release(this.iconLocation);
            return null;
         }
      }

      @Override
      public void close() {
         if (this.icon != null) {
            this.icon.close();
         }
      }
   }
}