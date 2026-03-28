package io.github.dqs147852.betterenchantments.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.DamageEnchantment;

public class AdvancedSharpnessEnchantment_ extends Enchantment {

    public AdvancedSharpnessEnchantment_(Rarity rarity, EquipmentSlot... slots) {
        super(rarity, EnchantmentCategory.WEAPON, slots);
    }

    @Override
    public int getMinLevel() {
        return 1;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getMinCost(int level) {
        return 15 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 10;
    }

    @Override
    public float getDamageBonus(int level, net.minecraft.world.entity.MobType mobType) {
        if (level == 1) {
            return 3.0F;
        } else if (level == 2) {
            return 3.0F + 5.0F;
        } else if (level == 3) {
            return 3.0F + 5.0F*2;
        } else if (level == 4) {
            return 3.0F + 5.0F*3;
        } else {
            return 3.0F + 5.0F*4;
        }
    }

    @Override
    public boolean checkCompatibility(Enchantment other) {
        if (other instanceof DamageEnchantment) {
            return false;
        }
        return super.checkCompatibility(other);
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
}