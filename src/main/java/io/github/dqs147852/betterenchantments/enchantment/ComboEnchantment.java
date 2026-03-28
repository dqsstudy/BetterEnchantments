package io.github.dqs147852.betterenchantments.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class ComboEnchantment extends Enchantment {

    public ComboEnchantment(Rarity rarity, EquipmentSlot... slots) {
        super(rarity, EnchantmentCategory.WEAPON, slots);
    }

    @Override
    public int getMinLevel() {
        return 1;
    }

    @Override
    public int getMaxLevel() {
        return 2;  // 最高II级
    }

    @Override
    public int getMinCost(int level) {
        // 等级1: 15级
        // 等级2: 25级
        return 10 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 10;
    }

    @Override
    public boolean isTreasureOnly() {
        return false;
    }

    @Override
    public boolean isTradeable() {
        return true;
    }

    @Override
    public boolean isDiscoverable() {
        return true;
    }

    @Override
    public boolean isAllowedOnBooks() {
        return true;
    }

    @Override
    public boolean checkCompatibility(Enchantment other) {
        // 与击退（Knockback）附魔兼容
        return super.checkCompatibility(other);
    }

    // 获取额外击退次数
    public int getExtraKnockbackCount(int level) {
        // 等级1: 1次额外击退（总共2次）
        // 等级2: 2次额外击退（总共3次）
        return level;
    }

    // 获取每次击退的力量
    public float getKnockbackStrength(int knockbackIndex, int level) {
        // 第一次击退：100%力量
        // 第二次击退：80%力量
        // 第三次击退：60%力量
        if (knockbackIndex == 0) {
            return 1.0f;  // 第一次击退，100%力量
        } else if (knockbackIndex == 1) {
            return 0.8f;  // 第二次击退，80%力量
        } else {
            return 0.6f;  // 第三次击退，60%力量
        }
    }
}