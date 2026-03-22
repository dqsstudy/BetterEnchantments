package io.github.dqs147852.betterenchantments.event;

import io.github.dqs147852.betterenchantments.enchantment.ModEnchantments;
import io.github.dqs147852.betterenchantments.network.PacketDoubleJump;
import io.github.dqs147852.betterenchantments.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "betterenchantments", value = Dist.CLIENT)
public class DoubleJumpHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    // 客户端状态存储
    private static final Map<UUID, ClientJumpState> clientStates = new HashMap<>();

    private static class ClientJumpState {
        boolean wasOnGround = true;
        int jumpsUsed = 0;  // 已使用的二段跳次数
        int maxJumps = 1;   // 最大二段跳次数（根据附魔等级）
        int groundTicks = 0;
    }

    // 客户端Tick事件 - 用于状态更新
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null) return;

        UUID playerId = player.getUUID();
        ClientJumpState state = clientStates.computeIfAbsent(playerId, k -> new ClientJumpState());

        // 检查玩家是否穿着带有二段跳附魔的护腿
        ItemStack leggings = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS);
        int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(
                ModEnchantments.DOUBLE_JUMP.get(), leggings);
        boolean hasDoubleJumpEnchantment = enchantmentLevel > 0;

        if (!hasDoubleJumpEnchantment) {
            // 没有附魔时重置状态
            state.wasOnGround = player.onGround();
            state.jumpsUsed = 0;
            state.maxJumps = 1;
            state.groundTicks = 0;
            return;
        }

        // 根据附魔等级设置最大跳跃次数
        // 等级1: 1次额外跳跃（总共2次）
        // 等级2: 2次额外跳跃（总共3次）
        state.maxJumps = enchantmentLevel;

        // 检测玩家是否刚刚离开地面
        boolean isOnGround = player.onGround();

        // 重置二段跳的条件：在地面停留足够时间
        if (isOnGround) {
            state.groundTicks++;
            if (state.groundTicks > 2) { // 在地面停留3个tick后重置
                if (state.jumpsUsed > 0) {
                    LOGGER.debug("玩家 {} 已落地，重置二段跳状态，已使用次数: {}",
                            player.getName().getString(), state.jumpsUsed);
                }
                state.jumpsUsed = 0;
            }
        } else {
            state.groundTicks = 0;
        }

        // 更新地面状态
        state.wasOnGround = isOnGround;

        // 输出调试信息
        if (player.tickCount % 20 == 0) { // 每秒输出一次
            LOGGER.debug("二段跳状态: 等级={}, 地面={}, 已用/最大={}/{}, 地面ticks={}, 垂直速度={}",
                    enchantmentLevel, isOnGround, state.jumpsUsed, state.maxJumps,
                    state.groundTicks, player.getDeltaMovement().y);
        }
    }

    // 按键输入事件 - 更可靠的按键检测
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null) return;

        // 检查是否是空格键被按下
        if (minecraft.options.keyJump.matches(event.getKey(), event.getScanCode())) {
            UUID playerId = player.getUUID();
            ClientJumpState state = clientStates.get(playerId);

            if (state == null) return;

            // 检查玩家是否穿着带有二段跳附魔的护腿
            ItemStack leggings = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS);
            int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(
                    ModEnchantments.DOUBLE_JUMP.get(), leggings);
            boolean hasDoubleJumpEnchantment = enchantmentLevel > 0;

            if (!hasDoubleJumpEnchantment) return;

            // 检查是否在空中且可以二段跳
            boolean isInAir = !player.onGround() && !player.isInWater() && !player.isInLava();
            boolean isFalling = player.getDeltaMovement().y < 0;
            boolean canDoubleJump = isInAir && isFalling && state.jumpsUsed < state.maxJumps;

            if (canDoubleJump && event.getAction() == 1) { // 按键按下
                LOGGER.info("检测到第{}次二段跳按键，发送网络包", state.jumpsUsed + 1);

                // 发送网络包到服务端
                PacketHandler.sendToServer(new PacketDoubleJump(state.jumpsUsed + 1));

                // 客户端本地记录
                state.jumpsUsed++;

                // 立即在客户端执行二段跳，不等待服务器响应
                executeDoubleJumpLocal(player, state.jumpsUsed);
            }
        }
    }

    // 本地执行二段跳（立即反馈）
    private static void executeDoubleJumpLocal(Player player, int jumpCount) {
        if (player == null) return;

        double jumpPower = 0.42; // 基础跳跃力量

        // 跳跃效果增强
        if (player.hasEffect(net.minecraft.world.effect.MobEffects.JUMP)) {
            int amplifier = player.getEffect(net.minecraft.world.effect.MobEffects.JUMP).getAmplifier();
            jumpPower += (amplifier + 1) * 0.1;
        }

        // 第二次跳跃稍微减弱
        if (jumpCount > 1) {
            jumpPower *= 0.9; // 第二次跳跃力量减少10%
        }

        // 保持水平速度，应用垂直速度
        double motionX = player.getDeltaMovement().x;
        double motionZ = player.getDeltaMovement().z;

        player.setDeltaMovement(motionX, jumpPower, motionZ);

        // 播放跳跃音效
        player.playSound(net.minecraft.sounds.SoundEvents.HORSE_JUMP, 0.5F, 1.0F);

        // 生成粒子效果
        if (player.level().isClientSide()) {
            int particleCount = 8 + (jumpCount - 1) * 4; // 跳跃次数越多粒子越多
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
    }

    // 玩家退出时清理状态
    @SubscribeEvent
    public static void onClientDisconnect(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
        if (event.getPlayer() != null) {
            clientStates.remove(event.getPlayer().getUUID());
        }
    }
}