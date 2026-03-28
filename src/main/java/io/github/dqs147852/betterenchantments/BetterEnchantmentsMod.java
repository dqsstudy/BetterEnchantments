package io.github.dqs147852.betterenchantments;

import io.github.dqs147852.betterenchantments.config.AnvilResetConfig;
import io.github.dqs147852.betterenchantments.network.PacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(BetterEnchantmentsMod.MOD_ID)
public class BetterEnchantmentsMod {
    public static final String MOD_ID = "betterenchantments";
    public static final Logger LOGGER = LogManager.getLogger();

    public BetterEnchantmentsMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册配置
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AnvilResetConfig.SPEC,
                MOD_ID + "-anvil-reset.toml");

        // 注册事件总线
        MinecraftForge.EVENT_BUS.register(this);

        // 注册附魔
        ModEnchantments.ENCHANTMENTS.register(modEventBus);

        // 监听FMLCommonSetupEvent事件，用于注册网络包
        modEventBus.addListener(this::onCommonSetup);

        LOGGER.info("Better Enchantments Mod 已加载");
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        // 在CommonSetup中注册网络包
        event.enqueueWork(() -> {
            LOGGER.info("正在注册网络包...");
            PacketHandler.register();
            LOGGER.info("网络包注册完成");
        });
    }
}