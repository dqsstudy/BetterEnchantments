package io.github.dqs147852.betterenchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, BetterEnchantments.MOD_ID);

    public static final RegistryObject<Enchantment> ADVANCED_SHARPNESS =
            ENCHANTMENTS.register("advanced_sharpness",
                    () -> new io.github.dqs147852.betterenchantments.enchantment.AdvancedSharpnessEnchantment(
                            Enchantment.Rarity.RARE,
                            EquipmentSlot.MAINHAND
                    ));
}