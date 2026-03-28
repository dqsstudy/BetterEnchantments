package io.github.dqs147852.betterenchantments;

import io.github.dqs147852.betterenchantments.enchantment.AdvancedSharpnessEnchantment_;
import io.github.dqs147852.betterenchantments.enchantment.AdvancedProtectionEnchantment;
import io.github.dqs147852.betterenchantments.enchantment.DoubleJumpEnchantment;
import io.github.dqs147852.betterenchantments.enchantment.VampirismEnchantment;
import io.github.dqs147852.betterenchantments.enchantment.ComboEnchantment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, BetterEnchantmentsMod.MOD_ID);

    public static final RegistryObject<Enchantment> ADVANCED_SHARPNESS =
            ENCHANTMENTS.register("advanced_sharpness",
                    () -> new AdvancedSharpnessEnchantment_(
                            Enchantment.Rarity.RARE,
                            EquipmentSlot.MAINHAND
                    ));

    public static final RegistryObject<Enchantment> ADVANCED_PROTECTION =
            ENCHANTMENTS.register("advanced_protection",
                    () -> new AdvancedProtectionEnchantment(
                            Enchantment.Rarity.RARE,
                            EquipmentSlot.HEAD,
                            EquipmentSlot.CHEST,
                            EquipmentSlot.LEGS,
                            EquipmentSlot.FEET
                    ));

    public static final RegistryObject<Enchantment> DOUBLE_JUMP =
            ENCHANTMENTS.register("double_jump",
                    () -> new DoubleJumpEnchantment(
                            Enchantment.Rarity.RARE,
                            EquipmentSlot.LEGS
                    ));

    public static final RegistryObject<Enchantment> VAMPIRISM =
            ENCHANTMENTS.register("vampirism",
                    () -> new VampirismEnchantment(
                            Enchantment.Rarity.UNCOMMON,
                            EquipmentSlot.MAINHAND
                    ));

    public static final RegistryObject<Enchantment> COMBO =
            ENCHANTMENTS.register("combo",
                    () -> new ComboEnchantment(
                            Enchantment.Rarity.UNCOMMON,
                            EquipmentSlot.MAINHAND
                    ));
}