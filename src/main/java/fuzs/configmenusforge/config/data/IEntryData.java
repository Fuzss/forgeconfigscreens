package fuzs.configmenusforge.config.data;


import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fuzs.configmenusforge.client.gui.data.EntryData;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface IEntryData {

    String getPath();

    @Nullable
    String getComment();

    ITextComponent getTitle();

    /**
     * @return title or colored title for search
     */
    default ITextComponent getDisplayTitle(String searchHighlight) {
        if (searchHighlight != null && !searchHighlight.isEmpty()) {
            List<Integer> indices = this.getSearchIndices(this.getSearchableTitle(), searchHighlight);
            if (!indices.isEmpty()) {
                return this.getColoredTitle(this.getTitle().getString(), searchHighlight.length(), indices);
            }
        }
        return this.getTitle();
    }

    /**
     * title used in search results highlighting current query
     */
    default ITextComponent getColoredTitle(String title, int length, List<Integer> indices) {
        IFormattableTextComponent component = new StringTextComponent(title.substring(0, indices.get(0))).withStyle(TextFormatting.GRAY);
        for (int i = 0, indicesSize = indices.size(); i < indicesSize; i++) {
            int start = indices.get(i);
            int end = start + length;
            component.append(new StringTextComponent(title.substring(start, end)).withStyle(TextFormatting.WHITE));
            int nextStart;
            int j = i;
            if (++j < indicesSize) {
                nextStart = indices.get(j);
            } else {
                nextStart = title.length();
            }
            component.append(new StringTextComponent(title.substring(end, nextStart)).withStyle(TextFormatting.GRAY));
        }
        return component;
    }

    /**
     * all starting indices of query for highlighting text
     */
    default List<Integer> getSearchIndices(String title, String query) {
        List<Integer> indices = Lists.newLinkedList();
        if (!query.isEmpty()) {
            int index = title.indexOf(query);
            while (index >= 0) {
                indices.add(index);
                index = title.indexOf(query, index + 1);
            }
        }
        return indices;
    }

    default String getSearchableTitle() {
        return this.getTitle().getString().toLowerCase(Locale.ROOT);
    }

    default boolean mayInclude(String searchHighlight) {
        return searchHighlight == null || searchHighlight.isEmpty() || this.getSearchableTitle().contains(searchHighlight);
    }

    boolean mayResetValue();

    /**
     * @return can cancel without warning
     */
    boolean mayDiscardChanges();

    void resetCurrentValue();

    void discardCurrentValue();

    /**
     * save to actual config, called when pressing done
     */
    void saveConfigValue();

    boolean category();

    static Comparator<IEntryData> getDefaultComparator(final boolean reversed) {
        final Comparator<IEntryData> defaultComparator = Comparator.comparing(o -> o.getTitle().getString());
        return Comparator.comparing(IEntryData::category).reversed().thenComparing(reversed ? defaultComparator.reversed() : defaultComparator);
    }

    static Comparator<IEntryData> getSearchComparator(final String searchHighlight, final boolean reversed) {
        if (searchHighlight != null && !searchHighlight.isEmpty()) {
            // when searching sort by index of query, only if both match sort alphabetically
            return Comparator.<IEntryData>comparingInt(o -> o.getSearchableTitle().indexOf(searchHighlight)).thenComparing(getDefaultComparator(false));
        }
        return getDefaultComparator(reversed);
    }

    static Map<Object, IEntryData> makeValueToDataMap(ModConfig config) {
        if (checkInvalid(config)) {
            return ImmutableMap.of();
        }
        Map<Object, IEntryData> allData = Maps.newHashMap();
        ForgeConfigSpec spec = (ForgeConfigSpec) config.getSpec();
        makeValueToDataMap(spec, spec.getValues(), config.getConfigData(), allData);
        return ImmutableMap.copyOf(allData);
    }

    static boolean checkInvalid(ModConfig config) {
        return config.getConfigData() == null || config.getSpec() == null || !config.getSpec().isLoaded();
    }

    static void makeValueToDataMap(ForgeConfigSpec spec, UnmodifiableConfig values, CommentedConfig comments, Map<Object, IEntryData> allData) {
        values.valueMap().forEach((path, value) -> {
            if (value instanceof UnmodifiableConfig) {
                UnmodifiableConfig category = (UnmodifiableConfig) value;
                final EntryData.CategoryEntryData data = new EntryData.CategoryEntryData(path, category, comments.getComment(path));
                allData.put(category, data);
                makeValueToDataMap(spec, category, (CommentedConfig) comments.valueMap().get(path), allData);
            } else if (value instanceof ForgeConfigSpec.ConfigValue<?>) {
                ForgeConfigSpec.ConfigValue<?> configValue = (ForgeConfigSpec.ConfigValue<?>) value;
                final EntryData.ConfigEntryData<?> data = new EntryData.ConfigEntryData<>(path, configValue, spec.getRaw(configValue.getPath()));
                allData.put(configValue, data);
            }
        });
    }
}