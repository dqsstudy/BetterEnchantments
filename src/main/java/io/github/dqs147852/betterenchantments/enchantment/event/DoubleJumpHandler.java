package io.github.dqs147852.betterenchantments.event;

import io.github.dqs147852.betterenchantments.enchantment.ModEnchantments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "betteenchantments", value = Dist.CLIENT)
public class DoubleJumpHandler {
    private static final int DOUBLE_JUMP_COOLDOWN = 5; // 冷却时间（tick）

    // 存储每个玩家的状态
    private static final Map<UUID, PlayerJumpState> playerStates = new HashMap<>();

    private static class PlayerJumpState {
        boolean wasOnGround = true;
        boolean hasDoubleJumped = false;
        int cooldown = 0;
        boolean wasJumping = false;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // 获取客户端玩家
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null) return;

        UUID playerId = player.getUUID();

        // 获取或创建玩家状态
        PlayerJumpState state = playerStates.computeIfAbsent(playerId, k -> new PlayerJumpState());

        // 检查玩家是否穿着带有二段跳附魔的护腿
        ItemStack leggings = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS);
        boolean hasDoubleJumpEnchantment = EnchantmentHelper.getItemEnchantmentLevel(
                ModEnchantments.DOUBLE_JUMP.get(), leggings) > 0;

        if (!hasDoubleJumpEnchantment) {
            state.wasOnGround = player.onGround();
            state.hasDoubleJumped = false;
            return;
        }

        // 冷却时间处理
        if (state.cooldown > 0) {
            state.cooldown--;
        }

        // 检测玩家是否刚刚离开地面
        if (state.wasOnGround && !player.onGround()) {
            state.hasDoubleJumped = false;
        }

        // 检测跳跃输入 - 在客户端使用minecraft.options.keyJump
        boolean isJumping = minecraft.options.keyJump.isDown();

        // 如果玩家在空中且没有在地面上，并且按下了跳跃键
        if (!player.onGround() &&
                !player.isInWater() &&
                !player.isInLava() &&
                player.getDeltaMovement().y < 0.1 && // 正在下落
                state.cooldown == 0 &&
                isJumping &&
                !state.wasJumping && // 检测跳跃键刚按下
                !state.hasDoubleJumped) {

            // 执行二段跳
            double jumpPower = 0.42; // 与普通跳跃相同的力量

            // 增加跳跃高度
            if (player.hasEffect(net.minecraft.world.effect.MobEffects.JUMP)) {
                jumpPower += (double)(player.getEffect(net.minecraft.world.effect.MobEffects.JUMP).getAmplifier() + 1) * 0.1F;
            }

            // 应用二段跳
            player.setDeltaMovement(player.getDeltaMovement().x, jumpPower, player.getDeltaMovement().z);

            // 播放跳跃音效
            player.playSound(net.minecraft.sounds.SoundEvents.HORSE_JUMP, 0.5F, 1.0F);

            // 设置状态
            state.hasDoubleJumped = true;
            state.cooldown = DOUBLE_JUMP_COOLDOWN;

            // 发送跳跃粒子效果
            spawnJumpParticles(player);
        }

        // 更新状态
        state.wasOnGround = player.onGround();
        state.wasJumping = isJumping;
    }

    private static void spawnJumpParticles(Player player) {
        // 生成跳跃粒子效果
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

    // 清理不再需要的玩家状态
    @SubscribeEvent
    public static void onClientDisconnect(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
        if (event.getPlayer() != null) {
            playerStates.remove(event.getPlayer().getUUID());
        }
    }
}