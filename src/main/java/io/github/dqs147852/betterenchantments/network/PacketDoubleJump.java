package io.github.dqs147852.betterenchantments.network;

import io.github.dqs147852.betterenchantments.ModEnchantments;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketDoubleJump {
    private static final Logger LOGGER = LogManager.getLogger();

    // 存储玩家已使用的二段跳次数
    private static final Map<UUID, Integer> serverJumpCounts = new HashMap<>();

    private final int jumpCount;  // 这是第几次二段跳

    public PacketDoubleJump(int jumpCount) {
        this.jumpCount = jumpCount;
    }

    public PacketDoubleJump(FriendlyByteBuf buf) {
        this.jumpCount = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(jumpCount);
    }

    public static void handle(PacketDoubleJump msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                //LOGGER.warn("收到二段跳网络包，但发送者为空");
                return;
            }

            UUID playerId = player.getUUID();
            int jumpCount = msg.jumpCount;

            // 检查玩家是否穿着带有二段跳附魔的护腿
            ItemStack leggings = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS);
            int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(
                    ModEnchantments.DOUBLE_JUMP.get(), leggings);

            if (enchantmentLevel <= 0) {
                //LOGGER.warn("玩家 {} 没有穿戴二段跳护腿", player.getName().getString());
                return;
            }

            // 检查玩家是否在空中
            if (player.onGround() || player.isInWater() || player.isInLava()) {
                //LOGGER.debug("玩家 {} 不满足二段跳条件（在地面/水中/熔岩中）", player.getName().getString());
                return;
            }

            // 获取已使用的跳跃次数
            int usedJumps = serverJumpCounts.getOrDefault(playerId, 0);

            // 计算最大允许跳跃次数
            int maxJumps = enchantmentLevel;  // 等级1:1次，等级2:2次

            // 检查是否超过最大跳跃次数
            if (jumpCount > maxJumps) {
                //LOGGER.warn("玩家 {} 尝试第{}次跳跃，但最大允许{}次",player.getName().getString(), jumpCount, maxJumps);
                return;
            }

            // 检查跳跃计数是否连续
            if (jumpCount != usedJumps + 1) {
                //LOGGER.warn("玩家 {} 跳跃计数不连续，期望{}但收到{}", player.getName().getString(), usedJumps + 1, jumpCount);
                return;
            }

            //LOGGER.info("服务器确认玩家 {} 的第{}次二段跳", player.getName().getString(), jumpCount);

            // 执行二段跳
            double jumpPower = 0.42;

            if (player.hasEffect(net.minecraft.world.effect.MobEffects.JUMP)) {
                int amplifier = player.getEffect(net.minecraft.world.effect.MobEffects.JUMP).getAmplifier();
                jumpPower += (amplifier + 1) * 0.1;
            }

            // 跳跃次数越多，力量稍微减弱
            if (jumpCount > 1) {
                jumpPower *= 0.9;  // 第二次跳跃力量减少10%
            }

            // 保持水平速度，应用垂直速度
            double motionX = player.getDeltaMovement().x;
            double motionZ = player.getDeltaMovement().z;

            player.setDeltaMovement(motionX, jumpPower, motionZ);

            // 播放音效给所有玩家
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    net.minecraft.sounds.SoundEvents.HORSE_JUMP,
                    player.getSoundSource(), 0.5F, 1.0F);

            // 更新已使用的跳跃次数
            serverJumpCounts.put(playerId, jumpCount);

            // 发送粒子效果给所有玩家
            PacketHandler.sendToAllTracking(new PacketSpawnJumpParticles(player.getUUID(), jumpCount), player);
        });
        ctx.get().setPacketHandled(true);
    }

    // 重置玩家的二段跳状态（当玩家落地时）
    public static void resetJumpState(ServerPlayer player) {
        if (player == null) return;

        serverJumpCounts.remove(player.getUUID());
        //LOGGER.debug("重置玩家 {} 的二段跳状态", player.getName().getString());
    }

    // 清理玩家状态
    public static void removePlayerState(UUID playerId) {
        serverJumpCounts.remove(playerId);
        //LOGGER.debug("清理玩家 {} 的二段跳状态", playerId);
    }
}