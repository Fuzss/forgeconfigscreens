package fuzs.forgeconfigscreens.client.gui.screens;

import fuzs.forgeconfigscreens.client.gui.data.IEntryData;
import net.minecraft.network.chat.Component;

import java.util.function.Predicate;
import java.util.stream.Stream;

enum ShownEntriesFilter {
    ALL(6, "configmenusforge.gui.tooltip.showing.all", data -> true),
    ENTRIES(2, "configmenusforge.gui.tooltip.showing.entries", Predicate.not(IEntryData::category), true),
    CATEGORIES(8, "configmenusforge.gui.tooltip.showing.categories", IEntryData::category, true),
    EDITED(3, "configmenusforge.gui.tooltip.showing.edited", Predicate.not(IEntryData::mayDiscardChanges)),
    RESETTABLE(7, "configmenusforge.gui.tooltip.showing.resettable", IEntryData::mayResetValue);

    private static final String SHOWING_TRANSLATION_KEY = "configmenusforge.gui.tooltip.showing";
    private static final int[] DEFAULT_FILTERS_INDICES = Stream.of(ShownEntriesFilter.values()).filter(Predicate.not(ShownEntriesFilter::searchOnly)).mapToInt(Enum::ordinal).toArray();

    private final int textureX;
    private final Component message;
    private final Predicate<IEntryData> predicate;
    private final boolean searchOnly;

    ShownEntriesFilter(int textureIndex, String translationKey, Predicate<IEntryData> predicate) {
        this(textureIndex, translationKey, predicate, false);
    }

    ShownEntriesFilter(int textureIndex, String translationKey, Predicate<IEntryData> predicate, boolean searchOnly) {
        this.textureX = textureIndex * 20;
        this.message = Component.translatable(SHOWING_TRANSLATION_KEY, Component.translatable(translationKey));
        this.predicate = predicate;
        this.searchOnly = searchOnly;
    }

    public int getTextureX() {
        return this.textureX;
    }

    public Component getMessage() {
        return this.message;
    }

    public boolean test(IEntryData data, boolean empty) {
        return this.predicate.test(data) || empty && data.category();
    }

    private boolean searchOnly() {
        return this.searchOnly;
    }

    public static int cycle(int index, boolean search, boolean reversed) {
        if (!search) {
            for (int i = 0; i < DEFAULT_FILTERS_INDICES.length; i++) {
                if (DEFAULT_FILTERS_INDICES[i] == index) {
                    index = i;
                    break;
                }
            }
        }
        int length = search ? ShownEntriesFilter.values().length : DEFAULT_FILTERS_INDICES.length;
        int amount = reversed ? -1 : 1;
        index = (index + amount + length) % length;
        return search ? index : DEFAULT_FILTERS_INDICES[index];
    }
}
