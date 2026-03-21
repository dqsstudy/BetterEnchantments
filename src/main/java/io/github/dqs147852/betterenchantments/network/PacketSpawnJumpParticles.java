package io.github.dqs147852.betterenchantments.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketSpawnJumpParticles {
    private final UUID playerId;

    public PacketSpawnJumpParticles(UUID playerId) {
        this.playerId = playerId;
    }

    public PacketSpawnJumpParticles(FriendlyByteBuf buf) {
        this.playerId = buf.readUUID();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(playerId);
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

                // 生成粒子效果
                for (int i = 0; i < 8; i++) {
                    double offsetX = (player.getRandom().nextDouble() - 0.5) * 0.5;
                    double offsetZ = (player.getRandom().nextDouble() - 0.5) * 0.5;

                    player.level().addParticle(
                            net.minecraft.core.particles.ParticleTypes.CLOUD,
                            player.getX() + offsetX,
                            player.getY(),
                            player.getZ() + offsetZ,
                            0.0, 0.1, 0.0
                    );
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}