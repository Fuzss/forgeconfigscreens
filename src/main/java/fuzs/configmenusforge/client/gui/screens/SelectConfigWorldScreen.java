package fuzs.configmenusforge.client.gui.screens;

import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.configmenusforge.ConfigMenusForge;
import fuzs.configmenusforge.client.gui.components.ConfigWorldSelectionList;
import fuzs.configmenusforge.client.gui.util.ScreenUtil;
import fuzs.configmenusforge.client.gui.widget.AnimatedIconButton;
import fuzs.configmenusforge.lib.core.ModLoaderEnvironment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class SelectConfigWorldScreen extends Screen {
   private final Screen lastScreen;
   private final ResourceLocation background;
   private final Component displayName;
   private final ModConfig config;
   private final List<LevelSummary> levelList;
   private List<FormattedCharSequence> activeTooltip;
   private EditBox searchBox;
   private ConfigWorldSelectionList list;
   private Button openButton;
   private Button createButton;
   private Button copyButton;
   private Button fileButton;
   private AnimatedIconButton tinyJumperButton;
   
   public SelectConfigWorldScreen(Screen lastScreen, Component displayName, ResourceLocation optionsBackground, ModConfig config, List<LevelSummary> levelList) {
      super(new TranslatableComponent("configmenusforge.gui.select.world.title", displayName));
      this.lastScreen = lastScreen;
      this.displayName = displayName;
      this.background = optionsBackground;
      this.config = config;
      if (levelList.isEmpty()) throw new IllegalArgumentException("level list may not be empty, solve this beforehand");
      this.levelList = levelList;
   }

   @Override
   public void tick() {
      this.searchBox.tick();
      // makes tiny person wave when hovered
      this.tinyJumperButton.tick();
   }

   @Override
   protected void init() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
      this.searchBox = new EditBox(this.font, this.width / 2 - 121, 22, 242, 20, this.searchBox, TextComponent.EMPTY) {

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
      this.openButton = this.addRenderableWidget(new Button(this.width / 2 - 154, this.height - 52, 150, 20, new TranslatableComponent("configmenusforge.gui.select.edit"), button1 -> {
         final ConfigWorldSelectionList.ConfigWorldListEntry selected = this.list.getSelected();
         if (selected != null) {
            selected.openConfig();
         }
      }));
      this.createButton = this.addRenderableWidget(new Button(this.width / 2 - 154, this.height - 52, 150, 20, new TranslatableComponent("configmenusforge.gui.select.world.create"), button1 -> {
         final ConfigWorldSelectionList.ConfigWorldListEntry selected = this.list.getSelected();
         if (selected != null) {
            selected.openConfig();
         }
      }));
      this.copyButton = this.addRenderableWidget(new Button(this.width / 2 + 4, this.height - 52, 150, 20, new TranslatableComponent("configmenusforge.gui.select.copy"), button -> {
         final ConfigWorldSelectionList.ConfigWorldListEntry selected = this.list.getSelected();
         if (selected != null) {
            selected.loadModConfig(this.config);
            Path destination = ModLoaderEnvironment.getGameDir().resolve(FMLConfig.defaultConfigPath()).resolve(this.config.getFileName());
            this.minecraft.setScreen(ScreenUtil.makeConfirmationScreen(new TranslatableComponent("configmenusforge.gui.message.copy.title"), Files.exists(destination) ? new TranslatableComponent("configmenusforge.gui.message.copy.warning").withStyle(ChatFormatting.RED) : new TranslatableComponent("configmenusforge.gui.message.copy.description"), this.background, result -> {
               if (result) {
                  try {
                     if (!Files.exists(destination)) {
                        FileUtils.getOrCreateDirectory(destination.getParent(), String.format("%s default config", this.config.getFileName()));
                        Files.createFile(destination);
                     }
                     TomlFormat.instance().createWriter().write(this.config.getConfigData(), destination, WritingMode.REPLACE);
                     ConfigMenusForge.LOGGER.info("Successfully copied {} to default config folder", this.config.getFileName());
                  } catch (Exception e) {
                     ConfigMenusForge.LOGGER.error("Failed to copy {} to default config folder", this.config.getFileName(), e);
                  }
               }
               this.minecraft.setScreen(this);
            }));
         }
      }));
      this.fileButton = this.addRenderableWidget(new Button(this.width / 2 - 154, this.height - 28, 150, 20, new TranslatableComponent("configmenusforge.gui.select.open"), button -> {
         final ConfigWorldSelectionList.ConfigWorldListEntry selected = this.list.getSelected();
         if (selected != null) {
            selected.loadModConfig(this.config);
            final Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, this.config.getFullPath().toAbsolutePath().toString()));
            this.handleComponentClicked(style);
         }
      }));
      this.updateButtonStatus(false);
      this.list = new ConfigWorldSelectionList(this, this.minecraft, this.width, this.height, 50, this.height - 60, 36, this.searchBox.getValue(), this.levelList);
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
   public boolean charTyped(char pCodePoint, int pModifiers) {
      return this.searchBox.charTyped(pCodePoint, pModifiers);
   }

   @Override
   public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.activeTooltip = null;
      this.list.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      this.searchBox.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      drawCenteredString(pMatrixStack, this.font, this.title, this.width / 2, 8, 16777215);
      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      if (this.activeTooltip != null) {
         this.renderTooltip(pMatrixStack, this.activeTooltip, pMouseX, pMouseY);
      }

   }

   public void setActiveTooltip(List<FormattedCharSequence> list) {
      this.activeTooltip = list;
   }

   public void updateButtonStatus(boolean active) {
      if (this.list != null && active) {
         final ConfigWorldSelectionList.ConfigWorldListEntry selected = this.list.getSelected();
         final boolean fileExists = selected.fileExists();
         this.openButton.visible = fileExists;
         this.createButton.visible = !fileExists;
         this.openButton.active = true;
         this.fileButton.active = fileExists;
         this.copyButton.active = fileExists;
      } else {
         this.openButton.visible = true;
         this.openButton.active = false;
         this.createButton.visible = false;
         this.fileButton.active = false;
         this.copyButton.active = false;
      }
   }

   public Component getDisplayName() {
      return this.displayName;
   }

   public ResourceLocation getBackground() {
      return this.background;
   }

   public ModConfig getConfig() {
      return this.config;
   }

   @Override
   public void removed() {
      if (this.list != null) {
         this.list.children().forEach(ConfigWorldSelectionList.ConfigWorldListEntry::close);
      }
   }
}