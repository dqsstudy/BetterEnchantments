package io.github.dqs147852.betterenchantments.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class AnvilResetConfig {
    public static final ForgeConfigSpec SPEC;
    public static final AnvilResetConfig INSTANCE;

    static {
        Pair<AnvilResetConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder()
                .configure(AnvilResetConfig::new);
        SPEC = specPair.getRight();
        INSTANCE = specPair.getLeft();
    }

    // 是否启用铁砧重置功能
    public final ForgeConfigSpec.BooleanValue enabled;

    // 所需钻石块数量
    public final ForgeConfigSpec.IntValue diamondBlocksRequired;

    // 所需经验瓶数量
    public final ForgeConfigSpec.IntValue experienceBottlesRequired;

    // 检测范围（格数）
    public final ForgeConfigSpec.DoubleValue detectionRange;

    // 是否消耗材料
    public final ForgeConfigSpec.BooleanValue consumeMaterials;

    public AnvilResetConfig(ForgeConfigSpec.Builder builder) {
        builder.push("anvil_reset");

        enabled = builder
                .comment("是否启用铁砧附魔惩罚重置功能")
                .define("enabled", true);

        diamondBlocksRequired = builder
                .comment("重置所需的钻石块数量")
                .defineInRange("diamondBlocksRequired", 1, 1, 64);

        experienceBottlesRequired = builder
                .comment("重置所需的经验瓶数量")
                .defineInRange("experienceBottlesRequired", 20, 1, 64);

        detectionRange = builder
                .comment("铁砧周围的检测范围（格数）")
                .defineInRange("detectionRange", 1.0, 0.5, 3.0);

        consumeMaterials = builder
                .comment("是否消耗材料（钻石块和经验瓶）")
                .define("consumeMaterials", true);

        builder.pop();
    }
}