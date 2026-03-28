package io.github.dqs147852.betterenchantments.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class VampirismEnchantment extends Enchantment {

    public VampirismEnchantment(Rarity rarity, EquipmentSlot... slots) {
        super(rarity, EnchantmentCategory.WEAPON, slots);
    }

    @Override
    public int getMinLevel() {
        return 1;
    }

    @Override
    public int getMaxLevel() {
        return 3;  // 最高III级
    }

    @Override
    public int getMinCost(int level) {
        // 等级1: 20级
        // 等级2: 30级
        // 等级3: 40级
        return 15 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 10;
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

    @Override
    public boolean checkCompatibility(Enchantment other) {
        // 与其他武器附魔兼容
        return super.checkCompatibility(other);
    }

    // 获取生命偷取百分比
    public float getLifestealPercent(int level) {
        // 等级1: 20%
        // 等级2: 25%
        // 等级3: 30%
        return 0.20f + (level - 1) * 0.05f;
    }
}