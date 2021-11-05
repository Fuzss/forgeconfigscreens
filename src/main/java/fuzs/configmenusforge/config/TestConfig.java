package fuzs.configmenusforge.config;

import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;

public class TestConfig {
    public final ForgeConfigSpec.ConfigValue<String> stringValue;
    public final ForgeConfigSpec.BooleanValue booleanValue;
    public final ForgeConfigSpec.IntValue intValue;
    public final ForgeConfigSpec.DoubleValue doubleValue;
    public final ForgeConfigSpec.LongValue longValue;
    public final ForgeConfigSpec.EnumValue<ChatFormatting> enumValue;
    public final ForgeConfigSpec.ConfigValue<List<? extends Boolean>> booleanListValue;
    public final ForgeConfigSpec.ConfigValue<List<? extends Integer>> intListValue;
    public final ForgeConfigSpec.ConfigValue<List<? extends Long>> longListValue;
    public final ForgeConfigSpec.ConfigValue<List<? extends Double>> doubleListValue;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> stringListValue;
    public final ForgeConfigSpec.ConfigValue<List<? extends Enum<?>>> enumListValue;
    public final ForgeConfigSpec.ConfigValue<List<? extends Integer>> listValue;

    public TestConfig(ForgeConfigSpec.Builder builder) {
        this.booleanValue = builder.comment("This is a Boolean value").define("booleanValue", false);
        this.intValue = builder.comment("This is an Integer value").defineInRange("int_Value", 5, 0, 10);
        this.longValue = builder.comment("This is a Long value").defineInRange("long_value", 2L, 0L, 10L);
        this.doubleValue = builder.comment("This is a Double value").defineInRange("double_Value", 1.0, 0.0, 10.0);
        this.stringValue = builder.comment("This is an String value").define("stringValue", "YEP");
        this.enumValue = builder.comment("This is an Enum value").defineEnum("enumValue", ChatFormatting.WHITE);
        builder.comment("YEP").push("list_properties");
        this.booleanListValue = builder.comment("This is a List of Boolean values").defineList("booleanListValue", Lists.newArrayList(true, true, false, true, false), o -> true);
        this.intListValue = builder.comment("This is a List of Integer values").defineList("intListValue", Lists.newArrayList(0, 1, 2, 3, 4, 5), o -> true);
        this.longListValue = builder.comment("This is a List of Long values").defineList("longListValue", Lists.newArrayList(0L, 1L, 2L, 3L, 4L, 5L), o -> true);
        this.doubleListValue = builder.comment("This is a List of Double values").defineList("doubleListValue", Lists.newArrayList(0.0, 1.0, 2.0, 3.0, 4.0, 5.0),o -> true);
        this.stringListValue = builder.comment("This is a List of String values").defineList("stringListValue", Lists.newArrayList("YEP", "YEP", "YEP"), o -> true);
        this.enumListValue = builder.comment("This is a List of Enum values").defineList("enumListValue", Lists.newArrayList(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST), o -> true);
        this.listValue = builder.comment("This is a List of unknown values").defineList("unknownListValue", Collections.emptyList(), o -> true);
        builder.pop();
    }

    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final TestConfig CLIENT;
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final TestConfig COMMON;
    public static final ForgeConfigSpec SERVER_SPEC;
    public static final TestConfig SERVER;

    static {
        final Pair<TestConfig, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(TestConfig::new);
        CLIENT_SPEC = clientSpecPair.getRight();
        CLIENT = clientSpecPair.getLeft();
        final Pair<TestConfig, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(TestConfig::new);
        COMMON_SPEC = commonSpecPair.getRight();
        COMMON = commonSpecPair.getLeft();
        final Pair<TestConfig, ForgeConfigSpec> serverSpecPair = new ForgeConfigSpec.Builder().configure(TestConfig::new);
        SERVER_SPEC = serverSpecPair.getRight();
        SERVER = serverSpecPair.getLeft();
    }
}
