package io.github.dqs147852.betterenchantments.enchantment.event;

import io.github.dqs147852.betterenchantments.ModEnchantments;
import io.github.dqs147852.betterenchantments.enchantment.VampirismEnchantment;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = "betterenchantments")
public class VampirismHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    // 冷却时间记录（防止快速攻击造成过多治疗）
    private static final int COOLDOWN_TICKS = 5; // 5 tick冷却

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查伤害来源是否为玩家
        if (event.getSource().getEntity() instanceof Player) {
            Player player = (Player) event.getSource().getEntity();
            LivingEntity target = event.getEntity();

            // 检查玩家是否在主手持有武器
            ItemStack mainHandItem = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND);

            // 检查武器是否有吸血鬼附魔
            int vampirismLevel = EnchantmentHelper.getItemEnchantmentLevel(
                    ModEnchantments.VAMPIRISM.get(), mainHandItem);

            if (vampirismLevel > 0) {
                // 计算偷取的生命值
                float damage = event.getAmount();
                float lifestealPercent = ((VampirismEnchantment) ModEnchantments.VAMPIRISM.get())
                        .getLifestealPercent(vampirismLevel);

                float healAmount = damage * lifestealPercent;

                LOGGER.debug("吸血鬼附魔生效: 等级={}, 伤害={}, 偷取百分比={}%, 治疗量={}",
                        vampirismLevel, damage, lifestealPercent * 100, healAmount);

                // 应用治疗（不超过玩家最大生命值）
                float currentHealth = player.getHealth();
                float maxHealth = player.getMaxHealth();

                if (currentHealth < maxHealth) {
                    // 计算实际治疗量（不超过最大生命值）
                    float actualHeal = Math.min(healAmount, maxHealth - currentHealth);

                    if (actualHeal > 0) {
                        // 治疗玩家
                        player.heal(actualHeal);

                        // 播放治疗音效
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, 1.0F);

                        // 在玩家身上生成治疗粒子效果
                        spawnHealParticles(player);

                        // 在目标身上生成吸血粒子效果
                        spawnBloodParticles(target);

                        LOGGER.info("玩家 {} 通过吸血鬼附魔恢复了 {} 点生命值",
                                player.getName().getString(), actualHeal);
                    }
                }
            }
        }
    }

    private static void spawnHealParticles(Player player) {
        if (player.level().isClientSide()) {
            for (int i = 0; i < 5; i++) {
                double offsetX = (player.getRandom().nextDouble() - 0.5) * 0.8;
                double offsetY = player.getRandom().nextDouble() * 1.5;
                double offsetZ = (player.getRandom().nextDouble() - 0.5) * 0.8;

                player.level().addParticle(
                        ParticleTypes.HEART,
                        player.getX() + offsetX,
                        player.getY() + offsetY,
                        player.getZ() + offsetZ,
                        0.0, 0.1, 0.0
                );
            }
        } else {
            // 服务端发送粒子效果给所有玩家
            for (int i = 0; i < 5; i++) {
                double offsetX = (player.getRandom().nextDouble() - 0.5) * 0.8;
                double offsetY = player.getRandom().nextDouble() * 1.5;
                double offsetZ = (player.getRandom().nextDouble() - 0.5) * 0.8;

                ((net.minecraft.server.level.ServerLevel) player.level()).sendParticles(
                        ParticleTypes.HEART,
                        player.getX() + offsetX,
                        player.getY() + offsetY,
                        player.getZ() + offsetZ,
                        1, 0, 0, 0, 0
                );
            }
        }
    }

    private static void spawnBloodParticles(LivingEntity target) {
        if (target.level().isClientSide()) {
            for (int i = 0; i < 3; i++) {
                double offsetX = (target.getRandom().nextDouble() - 0.5) * 0.5;
                double offsetY = target.getRandom().nextDouble() * 1.0;
                double offsetZ = (target.getRandom().nextDouble() - 0.5) * 0.5;

                target.level().addParticle(
                        ParticleTypes.DAMAGE_INDICATOR,
                        target.getX() + offsetX,
                        target.getY() + offsetY,
                        target.getZ() + offsetZ,
                        0.0, 0.1, 0.0
                );
            }
        } else {
            // 服务端发送粒子效果给所有玩家
            for (int i = 0; i < 3; i++) {
                double offsetX = (target.getRandom().nextDouble() - 0.5) * 0.5;
                double offsetY = target.getRandom().nextDouble() * 1.0;
                double offsetZ = (target.getRandom().nextDouble() - 0.5) * 0.5;

                ((net.minecraft.server.level.ServerLevel) target.level()).sendParticles(
                        ParticleTypes.DAMAGE_INDICATOR,
                        target.getX() + offsetX,
                        target.getY() + offsetY,
                        target.getZ() + offsetZ,
                        1, 0, 0, 0, 0
                );
            }
        }
    }
}