package io.github.dqs147852.betterenchantments.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketSpawnJumpParticles {
    private static final Logger LOGGER = LogManager.getLogger();

    private final UUID playerId;
    private final int jumpCount;  // 第几次跳跃

    public PacketSpawnJumpParticles(UUID playerId, int jumpCount) {
        this.playerId = playerId;
        this.jumpCount = jumpCount;
    }

    public PacketSpawnJumpParticles(FriendlyByteBuf buf) {
        this.playerId = buf.readUUID();
        this.jumpCount = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(playerId);
        buf.writeInt(jumpCount);
    }

    public static void handle(PacketSpawnJumpParticles msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 只在客户端执行
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.level == null) return;

                // 通过UUID查找玩家
                net.minecraft.world.entity.player.Player player = minecraft.level.getPlayerByUUID(msg.playerId);
                if (player == null) return;

                LOGGER.debug("生成玩家 {} 第{}次跳跃的粒子效果",
                        player.getName().getString(), msg.jumpCount);

                // 生成粒子效果，跳跃次数越多粒子越多
                int particleCount = 8 + (msg.jumpCount - 1) * 4;
                for (int i = 0; i < particleCount; i++) {
                    double offsetX = (player.getRandom().nextDouble() - 0.5) * 0.8;
                    double offsetZ = (player.getRandom().nextDouble() - 0.5) * 0.8;

                    player.level().addParticle(
                            net.minecraft.core.particles.ParticleTypes.CLOUD,
                            player.getX() + offsetX,
                            player.getY(),
                            player.getZ() + offsetZ,
                            0.0, 0.2, 0.0
                    );
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}