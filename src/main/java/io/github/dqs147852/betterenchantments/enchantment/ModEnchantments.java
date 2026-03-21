package io.github.dqs147852.betterenchantments.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, BetterEnchantments.MOD_ID);

//    public static final RegistryObject<Enchantment> ADVANCED_SHARPNESS =
//            ENCHANTMENTS.register("advanced_sharpness",
//                    () -> new AdvancedSharpnessEnchantment(
//                            Enchantment.Rarity.RARE,
//                            EquipmentSlot.MAINHAND
//                    ));

    public static final RegistryObject<Enchantment> ADVANCED_PROTECTION =
            ENCHANTMENTS.register("advanced_protection",
                    () -> new io.github.dqs147852.betterenchantments.enchantment.AdvancedProtectionEnchantment(
                            Enchantment.Rarity.RARE,
                            EquipmentSlot.HEAD,
                            EquipmentSlot.CHEST,
                            EquipmentSlot.LEGS,
                            EquipmentSlot.FEET
                    ));
    public static final RegistryObject<Enchantment> ADVANCED_SHARPNESS =
            ENCHANTMENTS.register("advance_sharpness",
                    () -> new AdvancedSharpnessEnchantment_(
                            Enchantment.Rarity.RARE,
                            EquipmentSlot.MAINHAND
                    ));
}