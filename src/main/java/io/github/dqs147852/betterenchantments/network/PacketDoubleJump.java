package io.github.dqs147852.betterenchantments.network;

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

    // 存储玩家二段跳状态的服务端
    private static final Map<UUID, Boolean> serverDoubleJumpUsed = new HashMap<>();

    public PacketDoubleJump() {
        // 空构造函数
    }

    public PacketDoubleJump(FriendlyByteBuf buf) {
        // 空的解码器
    }

    public void encode(FriendlyByteBuf buf) {
        // 空的编码器
    }

    public static void handle(PacketDoubleJump msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                LOGGER.warn("收到二段跳网络包，但发送者为空");
                return;
            }

            UUID playerId = player.getUUID();

            // 检查玩家是否已经使用过二段跳
            Boolean hasDoubleJumped = serverDoubleJumpUsed.get(playerId);
            if (hasDoubleJumped != null && hasDoubleJumped) {
                LOGGER.debug("玩家 {} 已使用过二段跳，忽略请求", player.getName().getString());
                return;
            }

            // 检查玩家是否穿着带有二段跳附魔的护腿
            ItemStack leggings = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS);
            int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(
                    net.minecraftforge.registries.ForgeRegistries.ENCHANTMENTS.getValue(
                            new net.minecraft.resources.ResourceLocation("betteenchantments", "double_jump")
                    ), leggings);

            if (enchantmentLevel <= 0) {
                LOGGER.warn("玩家 {} 没有穿戴二段跳护腿", player.getName().getString());
                return;
            }

            // 检查玩家是否在空中
            if (player.onGround() || player.isInWater() || player.isInLava()) {
                LOGGER.debug("玩家 {} 不满足二段跳条件（在地面/水中/熔岩中）", player.getName().getString());
                return;
            }

            LOGGER.info("服务器确认玩家 {} 的二段跳", player.getName().getString());

            // 执行二段跳
            double jumpPower = 0.42;

            if (player.hasEffect(net.minecraft.world.effect.MobEffects.JUMP)) {
                int amplifier = player.getEffect(net.minecraft.world.effect.MobEffects.JUMP).getAmplifier();
                jumpPower += (amplifier + 1) * 0.1;
            }

            // 保持水平速度，应用垂直速度
            double motionX = player.getDeltaMovement().x;
            double motionZ = player.getDeltaMovement().z;

            player.setDeltaMovement(motionX, jumpPower, motionZ);

            // 播放音效给所有玩家
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    net.minecraft.sounds.SoundEvents.HORSE_JUMP,
                    player.getSoundSource(), 0.5F, 1.0F);

            // 标记为已使用二段跳
            serverDoubleJumpUsed.put(playerId, true);

            // 发送粒子效果给所有玩家
            PacketHandler.sendToAllTracking(new PacketSpawnJumpParticles(player.getUUID()), player);
        });
        ctx.get().setPacketHandled(true);
    }

    // 重置玩家的二段跳状态（当玩家落地时）
    public static void resetJumpState(ServerPlayer player) {
        if (player == null) return;

        serverDoubleJumpUsed.remove(player.getUUID());
        LOGGER.debug("重置玩家 {} 的二段跳状态", player.getName().getString());
    }

    // 清理玩家状态
    public static void removePlayerState(UUID playerId) {
        serverDoubleJumpUsed.remove(playerId);
        LOGGER.debug("清理玩家 {} 的二段跳状态", playerId);
    }
}