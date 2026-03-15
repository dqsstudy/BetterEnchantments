package io.github.dqs147852.betterenchantments.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.DamageEnchantment;

public class AdvancedSharpnessEnchantment extends Enchantment {

    public AdvancedSharpnessEnchantment(Rarity rarity, EquipmentSlot... slots) {
        super(rarity, EnchantmentCategory.WEAPON, slots);
    }

    @Override
    public int getMinLevel() {
        return 1;  // 最小等级为I
    }

    @Override
    public int getMaxLevel() {
        return 5;  // 最大等级为V
    }

    @Override
    public int getMinCost(int level) {
        // 确保每个等级都有不同的附魔成本
        // 这样在附魔台中才会出现不同等级
        switch (level) {
            case 1: return 15;  // 等级I
            case 2: return 20;  // 等级II
            case 3: return 25;  // 等级III
            case 4: return 30;  // 等级IV
            case 5: return 35;  // 等级V
            default: return 15;
        }
    }

    @Override
    public int getMaxCost(int level) {
        // 最大成本比最小成本稍高
        return this.getMinCost(level) + 10;
    }

    @Override
    public float getDamageBonus(int level, net.minecraft.world.entity.MobType mobType) {
        // 根据您的要求计算伤害加成
        // 高级锋利I = 锋利III的效果 = 3.0F伤害
        if (level == 1) {
            return 3.0F;  // 对应原版锋利III
        } else if (level == 2) {
            return 4.5F;  // (3.0²)/2 = 4.5
        } else if (level == 3) {
            return 10.125F;  // (4.5²)/2 = 10.125
        } else if (level == 4) {
            return 51.26F;  // (10.125²)/2 ≈ 51.26
        } else {  // level == 5
            return 1313.8F;  // (51.26²)/2 ≈ 1313.8
        }
    }

    @Override
    public boolean checkCompatibility(Enchantment other) {
        // 与原版锋利附魔冲突
        if (other instanceof DamageEnchantment) {
            return false;
        }
        return super.checkCompatibility(other);
    }

    @Override
    public boolean isTreasureOnly() {
        return false;  // 可以在附魔台中获得
    }

    @Override
    public boolean isTradeable() {
        return true;  // 可以交易
    }

    @Override
    public boolean isDiscoverable() {
        return true;  // 可以在附魔台中发现
    }

    @Override
    public boolean isAllowedOnBooks() {
        return true;  // 可以出现在附魔书上
    }
}