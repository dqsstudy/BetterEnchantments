package io.github.dqs147852.betterenchantments;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(BetterEnchantments.MOD_ID)
public class BetterEnchantments {
    public static final String MOD_ID = "betteenchantments";
    public static final Logger LOGGER = LogManager.getLogger();

    public BetterEnchantments() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册附魔
        ModEnchantments.ENCHANTMENTS.register(modEventBus);

        // 注册其他内容（可扩展）
        modEventBus.addListener(this::setup);

        LOGGER.info("BetterEnchantments mod 已加载!");
    }

    private void setup(FMLCommonSetupEvent event) {
        LOGGER.info("高级锋利附魔已注册");
    }
}