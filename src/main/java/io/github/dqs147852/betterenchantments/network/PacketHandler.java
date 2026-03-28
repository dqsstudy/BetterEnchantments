package io.github.dqs147852.betterenchantments.network;

import io.github.dqs147852.betterenchantments.BetterEnchantmentsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static int packetId = 0;

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(BetterEnchantmentsMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
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
    }

    public static <MSG> void send(PacketDistributor.PacketTarget target, MSG message) {
        INSTANCE.send(target, message);
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    // 发送给所有追踪该实体的玩家
    public static <MSG> void sendToAllTracking(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), message);
    }

    // 发送给所有玩家
    public static <MSG> void sendToAll(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
}