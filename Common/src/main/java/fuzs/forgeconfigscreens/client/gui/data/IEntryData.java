package fuzs.forgeconfigscreens.client.gui.data;


import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fuzs.forgeconfigscreens.client.helper.ServerConfigUploader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface IEntryData {

    String getPath();

    @Nullable
    String getComment();

    Component getTitle();

    /**
     * @return title or colored title for search
     */
    default Component getDisplayTitle(String searchHighlight) {
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
    private Component getColoredTitle(String title, int length, List<Integer> indices) {
        MutableComponent component = Component.literal(title.substring(0, indices.get(0))).withStyle(ChatFormatting.GRAY);
        for (int i = 0, indicesSize = indices.size(); i < indicesSize; i++) {
            int start = indices.get(i);
            int end = start + length;
            component.append(Component.literal(title.substring(start, end)).withStyle(ChatFormatting.WHITE));
            int nextStart;
            int j = i;
            if (++j < indicesSize) {
                nextStart = indices.get(j);
            } else {
                nextStart = title.length();
            }
            component.append(Component.literal(title.substring(end, nextStart)).withStyle(ChatFormatting.GRAY));
        }
        return component;
    }

    /**
     * all starting indices of query for highlighting text
     */
    private List<Integer> getSearchIndices(String title, String query) {
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
        ForgeConfigSpec spec = ServerConfigUploader.findForgeConfigSpec(config.getSpec()).orElse(null);
        if (config.getConfigData() == null || spec == null || !spec.isLoaded()) return Map.of();
        Map<Object, IEntryData> allData = Maps.newHashMap();
        makeValueToDataMap(spec, spec.getValues(), config.getConfigData(), allData);
        return ImmutableMap.copyOf(allData);
    }

    private static void makeValueToDataMap(ForgeConfigSpec spec, UnmodifiableConfig values, CommentedConfig comments, Map<Object, IEntryData> allData) {
        values.valueMap().forEach((path, value) -> {
            if (value instanceof UnmodifiableConfig category) {
                final EntryData.CategoryEntryData data = new EntryData.CategoryEntryData(path, category, comments.getComment(path));
                allData.put(category, data);
                makeValueToDataMap(spec, category, (CommentedConfig) comments.valueMap().get(path), allData);
            } else if (value instanceof ForgeConfigSpec.ConfigValue<?> configValue) {
                final EntryData.ConfigEntryData<?> data = new EntryData.ConfigEntryData<>(path, configValue, spec.getRaw(configValue.getPath()));
                allData.put(configValue, data);
            }
        });
    }
}