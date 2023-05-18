package fuzs.forgeconfigscreens.client.gui.data;


import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.google.common.collect.Iterators;
import fuzs.forgeconfigscreens.client.gui.screens.ConfigScreen;
import fuzs.forgeconfigscreens.client.gui.util.ScreenUtil;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class EntryData implements IEntryData {

    private final String path;
    private final String comment;
    private final Component title;

    EntryData(String path, String comment, Component title) {
        this.path = path;
        this.comment = comment;
        this.title = title;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Nullable
    @Override
    public String getComment() {
        return this.comment;
    }

    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    public boolean mayResetValue() {
        return false;
    }

    @Override
    public boolean mayDiscardChanges() {
        return true;
    }

    @Override
    public void resetCurrentValue() {
    }

    @Override
    public void discardCurrentValue() {
    }

    @Override
    public void saveConfigValue() {
    }

    @Override
    public boolean category() {
        return false;
    }

    public static class CategoryEntryData extends EntryData {

        private final UnmodifiableConfig config;
        private ConfigScreen screen;

        public CategoryEntryData(String path, UnmodifiableConfig config, String comment) {
            super(path, comment, ScreenUtil.formatLabel(path));
            this.config = config;
        }

        public UnmodifiableConfig getConfig() {
            return this.config;
        }

        public ConfigScreen getScreen() {
            return this.screen;
        }

        public void setScreen(ConfigScreen screen) {
            this.screen = screen;
        }

        @Override
        public boolean category() {
            return true;
        }
    }

    public static class ConfigEntryData<T> extends EntryData {

        private final ForgeConfigSpec.ConfigValue<T> configValue;
        private final ForgeConfigSpec.ValueSpec valueSpec;
        private T currentValue;

        public ConfigEntryData(String path, ForgeConfigSpec.ConfigValue<T> configValue, ForgeConfigSpec.ValueSpec valueSpec) {
            super(path, valueSpec.getComment(), createLabel(path, configValue, valueSpec));
            this.configValue = configValue;
            this.valueSpec = valueSpec;
            this.currentValue = configValue.get();
        }

        @Override
        public boolean mayResetValue() {
            return !listSafeEquals(this.currentValue, this.getDefaultValue());
        }

        @Override
        public boolean mayDiscardChanges() {
            return listSafeEquals(this.configValue.get(), this.currentValue);
        }

        private static <T> boolean listSafeEquals(T o1, T o2) {
            // attempts to solve an issue where types of lists won't match when one is read from file
            // (due to enum being converted to string, long to int)
            if (o1 instanceof List<?> list1 && o2 instanceof List<?> list2) {
                final Stream<String> stream1 = list1.stream().map(o -> o instanceof Enum<?> e ? e.name() : o.toString());
                final Stream<String> stream2 = list2.stream().map(o -> o instanceof Enum<?> e ? e.name() : o.toString());
                return Iterators.elementsEqual(stream1.iterator(), stream2.iterator());
            }
            return o1.equals(o2);
        }

        @Override
        public void resetCurrentValue() {
            this.currentValue = this.getDefaultValue();
        }

        @Override
        public void discardCurrentValue() {
            this.currentValue = this.configValue.get();
        }

        @Override
        public void saveConfigValue() {
            this.configValue.set(this.currentValue);
        }

        @SuppressWarnings("unchecked")
        public T getDefaultValue() {
            return (T) this.valueSpec.getDefault();
        }

        public T getCurrentValue() {
            return this.currentValue;
        }

        public void setCurrentValue(T currentValue) {
            this.currentValue = currentValue;
        }

        public ForgeConfigSpec.ValueSpec getValueSpec() {
            return this.valueSpec;
        }

        public List<String> getFullPath() {
            return this.configValue.getPath();
        }

        /**
         * Tries to create a readable label from the given config value and spec. This will
         * first attempt to create a label from the translation key in the spec, otherwise it
         * will create a readable label from the raw config value name.
         *
         * @param configValue the config value
         * @param valueSpec   the associated value spec
         * @return a readable label string
         */
        private static Component createLabel(String path, ForgeConfigSpec.ConfigValue<?> configValue, ForgeConfigSpec.ValueSpec valueSpec) {
            if (valueSpec.getTranslationKey() != null && I18n.exists(valueSpec.getTranslationKey())) {
                return new TranslatableComponent(valueSpec.getTranslationKey());
            }
            return ScreenUtil.formatLabel(path);
        }
    }
}