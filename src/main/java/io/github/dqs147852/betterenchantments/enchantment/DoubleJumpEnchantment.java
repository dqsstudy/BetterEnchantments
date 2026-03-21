package io.github.dqs147852.betterenchantments.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class DoubleJumpEnchantment extends Enchantment {

    public DoubleJumpEnchantment(Rarity rarity, EquipmentSlot... slots) {
        super(rarity, EnchantmentCategory.ARMOR_LEGS, slots);
    }

    @Override
    public int getMinLevel() {
        return 1;
    }

    @Override
    public int getMaxLevel() {
        return 1; // 只有一个等级
    }

    @Override
    public int getMinCost(int level) {
        return 15; // 附魔成本
    }

    @Override
    public int getMaxCost(int level) {
        return 30;
    }

    @Override
    public boolean isTreasureOnly() {
        return false; // 可以在附魔台中获得
    }

    @Override
    public boolean isTradeable() {
        return true; // 可以交易
    }

    @Override
    public boolean isDiscoverable() {
        return true; // 可以在附魔台中发现
    }

    @Override
    public boolean isAllowedOnBooks() {
        return true; // 可以出现在附魔书上
    }

    @Override
    public boolean canEnchant(net.minecraft.world.item.ItemStack stack) {
        // 只能在护腿上附魔
        if (stack.getItem() instanceof net.minecraft.world.item.ArmorItem) {
            net.minecraft.world.item.ArmorItem armor = (net.minecraft.world.item.ArmorItem) stack.getItem();
            return armor.getEquipmentSlot() == EquipmentSlot.LEGS;
        }
        return false;
    }

    @Override
    public boolean checkCompatibility(Enchantment other) {
        // 不与其他附魔冲突
        return super.checkCompatibility(other);
    }
}