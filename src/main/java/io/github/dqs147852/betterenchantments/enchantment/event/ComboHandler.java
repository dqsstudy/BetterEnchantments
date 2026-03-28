package io.github.dqs147852.betterenchantments.enchantment.event;

import io.github.dqs147852.betterenchantments.ModEnchantments;
import io.github.dqs147852.betterenchantments.enchantment.ComboEnchantment;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

@Mod.EventBusSubscriber(modid = "betterenchantments")
public class ComboHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    // 存储连击任务
    private static final Map<UUID, ComboInfo> activeCombos = new HashMap<>();

    // 连击信息类
    private static class ComboInfo {
        Player attacker;
        LivingEntity target;
        int comboLevel;
        int remainingKnockbacks;
        Vec3 attackDirection;
        int delayTicks = 0;
        int knockbackIndex = 0;

        ComboInfo(Player attacker, LivingEntity target, int comboLevel, Vec3 direction) {
            this.attacker = attacker;
            this.target = target;
            this.comboLevel = comboLevel;
            this.attackDirection = direction.normalize();
            this.remainingKnockbacks = ((ComboEnchantment) ModEnchantments.COMBO.get())
                    .getExtraKnockbackCount(comboLevel);
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        // 只在服务端处理
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        // 检查伤害来源是否为玩家
        if (event.getSource().getEntity() instanceof Player) {
            Player player = (Player) event.getSource().getEntity();
            LivingEntity target = event.getEntity();
            UUID targetId = target.getUUID();

            // 检查玩家是否在主手持有武器
            ItemStack mainHandItem = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND);

            // 检查武器是否有连击附魔
            int comboLevel = EnchantmentHelper.getItemEnchantmentLevel(
                    ModEnchantments.COMBO.get(), mainHandItem);

            if (comboLevel > 0) {
                // 如果目标已有连击效果，先移除旧的
                if (activeCombos.containsKey(targetId)) {
                    activeCombos.remove(targetId);
                }

                // 计算攻击方向
                Vec3 direction = target.position().subtract(player.position()).normalize();

                // 创建新的连击信息
                ComboInfo comboInfo = new ComboInfo(player, target, comboLevel, direction);
                activeCombos.put(targetId, comboInfo);

                // 立即执行第一次击退
                executeKnockback(comboInfo, 0);
                comboInfo.knockbackIndex = 1;

                LOGGER.info("连击附魔生效: 等级={}, 目标={}, 额外击退次数={}",
                        comboLevel, target.getType().getDescription().getString(), comboInfo.remainingKnockbacks);
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        // 处理所有活跃的连击
        Iterator<Map.Entry<UUID, ComboInfo>> iterator = activeCombos.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, ComboInfo> entry = iterator.next();
            ComboInfo comboInfo = entry.getValue();
            UUID targetId = entry.getKey();

            // 检查目标是否仍然有效
            if (comboInfo.target == null || !comboInfo.target.isAlive() || comboInfo.target.isRemoved()) {
                iterator.remove();
                continue;
            }

            // 检查攻击者是否仍然有效
            if (comboInfo.attacker == null || !comboInfo.attacker.isAlive()) {
                iterator.remove();
                continue;
            }

            // 增加延迟
            comboInfo.delayTicks++;

            // 每8个tick执行一次击退（修改前是4tick）
            if (comboInfo.delayTicks >= 8 && comboInfo.knockbackIndex <= comboInfo.remainingKnockbacks) {
                executeKnockback(comboInfo, comboInfo.knockbackIndex);
                comboInfo.knockbackIndex++;
                comboInfo.delayTicks = 0;

                // 如果完成所有击退，移除这个连击
                if (comboInfo.knockbackIndex > comboInfo.remainingKnockbacks) {
                    iterator.remove();
                }
            }
        }
    }

    private static void executeKnockback(ComboInfo comboInfo, int knockbackIndex) {
        if (comboInfo.target == null || comboInfo.attacker == null) {
            return;
        }

        // 获取击退力量
        float strength = ((ComboEnchantment) ModEnchantments.COMBO.get())
                .getKnockbackStrength(knockbackIndex, comboInfo.comboLevel);

        // 计算击退方向
        // 对于后续击退，可以稍微随机化方向
        double angleVariation = 0.0;
        if (knockbackIndex > 0) {
            angleVariation = (comboInfo.target.getRandom().nextDouble() - 0.5) * 0.3;
        }

        Vec3 direction = rotateVector(comboInfo.attackDirection, angleVariation);

        // 计算Y轴偏移
        // 第一次击退：中等Y轴偏移
        // 第二次击退：较大Y轴偏移
        // 第三次击退：较小Y轴偏移
        float yOffset = calculateYOffset(knockbackIndex);

        // 应用击退向量
        Vec3 knockbackVector = direction.scale(0.6 * strength);

        // 修改Y轴分量，增加适当的Y轴偏移
        // 原版击退Y轴速度为0.2 + (strength * 0.1)
        // 现在改为根据击退次数计算不同的Y轴偏移
        double knockbackY = 0.2 + (strength * 0.1) + yOffset;

        // 应用击退
        comboInfo.target.push(knockbackVector.x, knockbackY, knockbackVector.z);

        // 播放效果
        playKnockbackEffects(comboInfo, knockbackIndex, strength, yOffset);

        LOGGER.debug("执行第{}次击退: 力量={}, Y轴偏移={}, 目标={}",
                knockbackIndex + 1, strength, yOffset, comboInfo.target.getType().getDescription().getString());
    }

    // 计算Y轴偏移
    private static float calculateYOffset(int knockbackIndex) {
        // 击退次数对应的Y轴偏移
        switch (knockbackIndex) {
            case 0: // 第一次额外击退
                return 0.3f;  // 中等Y轴偏移
            case 1: // 第二次额外击退
                return 0.4f;  // 较大Y轴偏移
            case 2: // 第三次额外击退（如果有）
                return 0.2f;  // 较小Y轴偏移
            default:
                return 0.2f;
        }
    }

    private static Vec3 rotateVector(Vec3 vector, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double newX = vector.x * cos - vector.z * sin;
        double newZ = vector.x * sin + vector.z * cos;
        return new Vec3(newX, vector.y, newZ);
    }

    private static void playKnockbackEffects(ComboInfo comboInfo, int knockbackIndex, float strength, float yOffset) {
        if (comboInfo.target == null) {
            return;
        }

        // 播放音效（音调随击退次数和Y轴偏移变化）
        float pitch = 1.0f + (knockbackIndex * 0.1f) + (yOffset * 0.2f);
        pitch = Math.min(pitch, 1.5f); // 限制最大音调

        // 选择不同的音效
        net.minecraft.sounds.SoundEvent sound = SoundEvents.PLAYER_ATTACK_KNOCKBACK;
        if (yOffset > 0.3f) {
            sound = SoundEvents.PLAYER_ATTACK_CRIT; // 高Y轴偏移使用暴击音效
        }

        comboInfo.target.level().playSound(null, comboInfo.target.getX(), comboInfo.target.getY(), comboInfo.target.getZ(),
                sound, SoundSource.PLAYERS, 0.7F, pitch);

        // 根据Y轴偏移生成不同数量的粒子效果
        int particleCount = 5 + knockbackIndex * 2 + (int)(yOffset * 5);

        for (int i = 0; i < particleCount; i++) {
            double offsetX = (comboInfo.target.getRandom().nextDouble() - 0.5) * 0.6;
            double offsetY = comboInfo.target.getRandom().nextDouble() * 0.6;
            double offsetZ = (comboInfo.target.getRandom().nextDouble() - 0.5) * 0.6;

            // 选择粒子类型
            net.minecraft.core.particles.ParticleOptions particleType;

            if (yOffset > 0.3f) {
                // 高Y轴偏移使用暴击粒子
                particleType = ParticleTypes.CRIT;
            } else if (yOffset > 0.2f) {
                // 中等Y轴偏移使用伤害粒子
                particleType = ParticleTypes.DAMAGE_INDICATOR;
            } else {
                // 低Y轴偏移使用云粒子
                particleType = ParticleTypes.CLOUD;
            }

            if (comboInfo.target.level().isClientSide()) {
                comboInfo.target.level().addParticle(
                        particleType,
                        comboInfo.target.getX() + offsetX,
                        comboInfo.target.getY() + offsetY + 0.5,
                        comboInfo.target.getZ() + offsetZ,
                        offsetX * 0.1, 0.1 + yOffset * 0.5, offsetZ * 0.1
                );
            } else {
                // 服务端发送粒子
                ((net.minecraft.server.level.ServerLevel) comboInfo.target.level()).sendParticles(
                        particleType,
                        comboInfo.target.getX() + offsetX,
                        comboInfo.target.getY() + offsetY + 0.5,
                        comboInfo.target.getZ() + offsetZ,
                        1, 0, 0, 0, 0
                );
            }
        }

        // 生成向上的粒子轨迹，显示Y轴偏移
        if (yOffset > 0.2f) {
            int trailParticles = 3 + (int)(yOffset * 3);
            for (int i = 0; i < trailParticles; i++) {
                double trailY = comboInfo.target.getY() + (i * 0.2);
                double offsetX = (comboInfo.target.getRandom().nextDouble() - 0.5) * 0.4;
                double offsetZ = (comboInfo.target.getRandom().nextDouble() - 0.5) * 0.4;

                if (comboInfo.target.level().isClientSide()) {
                    comboInfo.target.level().addParticle(
                            ParticleTypes.ENCHANT,
                            comboInfo.target.getX() + offsetX,
                            trailY,
                            comboInfo.target.getZ() + offsetZ,
                            0.0, 0.1, 0.0
                    );
                } else {
                    ((net.minecraft.server.level.ServerLevel) comboInfo.target.level()).sendParticles(
                            ParticleTypes.ENCHANT,
                            comboInfo.target.getX() + offsetX,
                            trailY,
                            comboInfo.target.getZ() + offsetZ,
                            1, 0, 0, 0, 0
                    );
                }
            }
        }

        // 生成打击特效粒子
        for (int i = 0; i < 3; i++) {
            double offsetX = (comboInfo.target.getRandom().nextDouble() - 0.5) * 0.3;
            double offsetY = comboInfo.target.getRandom().nextDouble() * 0.3;
            double offsetZ = (comboInfo.target.getRandom().nextDouble() - 0.5) * 0.3;

            if (comboInfo.target.level().isClientSide()) {
                comboInfo.target.level().addParticle(
                        ParticleTypes.DAMAGE_INDICATOR,
                        comboInfo.target.getX() + offsetX,
                        comboInfo.target.getY() + offsetY + 1.0,
                        comboInfo.target.getZ() + offsetZ,
                        0.0, 0.05 + yOffset * 0.2, 0.0
                );
            } else {
                ((net.minecraft.server.level.ServerLevel) comboInfo.target.level()).sendParticles(
                        ParticleTypes.DAMAGE_INDICATOR,
                        comboInfo.target.getX() + offsetX,
                        comboInfo.target.getY() + offsetY + 1.0,
                        comboInfo.target.getZ() + offsetZ,
                        1, 0, 0, 0, 0
                );
            }
        }
    }

    // 清理事件
    @SubscribeEvent
    public static void onLivingDeath(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            activeCombos.remove(event.getEntity().getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        // 清理该玩家的所有连击效果
        activeCombos.entrySet().removeIf(entry ->
                entry.getValue().attacker != null &&
                        entry.getValue().attacker.getUUID().equals(event.getEntity().getUUID())
        );
    }
}