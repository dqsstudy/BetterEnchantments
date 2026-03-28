package io.github.dqs147852.betterenchantments.network;

import io.github.dqs147852.betterenchantments.BetterEnchantmentsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class PacketHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String PROTOCOL_VERSION = "1";
    private static int packetId = 0;
    private static boolean isRegistered = false;

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(BetterEnchantmentsMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        if (isRegistered) {
            LOGGER.warn("网络包已经注册过了，跳过重复注册");
            return;
        }

        LOGGER.debug("开始注册网络包，当前packetId: {}", packetId);

        // 注册二段跳网络包
        INSTANCE.registerMessage(packetId++,
                PacketDoubleJump.class,
                PacketDoubleJump::encode,
                PacketDoubleJump::new,
                PacketDoubleJump::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        // 注册粒子效果网络包
        INSTANCE.registerMessage(packetId++,
                PacketSpawnJumpParticles.class,
                PacketSpawnJumpParticles::encode,
                PacketSpawnJumpParticles::new,
                PacketSpawnJumpParticles::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        isRegistered = true;
        LOGGER.info("网络包注册完成，共注册了 {} 个网络包", packetId);
    }

    public static <MSG> void send(PacketDistributor.PacketTarget target, MSG message) {
        if (!isRegistered) {
            LOGGER.warn("尝试发送网络包，但网络包尚未注册");
            return;
        }
        INSTANCE.send(target, message);
    }

    public static <MSG> void sendToServer(MSG message) {
        if (!isRegistered) {
            LOGGER.warn("尝试发送网络包到服务器，但网络包尚未注册");
            return;
        }
        INSTANCE.sendToServer(message);
    }

    // 发送给所有追踪该实体的玩家
    public static <MSG> void sendToAllTracking(MSG message, ServerPlayer player) {
        if (!isRegistered) {
            LOGGER.warn("尝试发送网络包给追踪者，但网络包尚未注册");
            return;
        }
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), message);
    }

    // 发送给所有玩家
    public static <MSG> void sendToAll(MSG message) {
        if (!isRegistered) {
            LOGGER.warn("尝试发送网络包给所有玩家，但网络包尚未注册");
            return;
        }
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
}