package io.github.dqs147852.betterenchantments.event;

import io.github.dqs147852.betterenchantments.enchantment.ModEnchantments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "betteenchantments", value = Dist.CLIENT)
public class ParticleHandler {

    @SubscribeEvent
    public static void onClientTickParticles(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null) return;

        // 检查玩家是否穿着带有二段跳附魔的护腿
        ItemStack leggings = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS);
        boolean hasDoubleJumpEnchantment = EnchantmentHelper.getItemEnchantmentLevel(
                ModEnchantments.DOUBLE_JUMP.get(), leggings) > 0;

        if (hasDoubleJumpEnchantment && !player.onGround() && !player.isInWater()) {
            // 在玩家脚下生成持续粒子效果
            for (int i = 0; i < 2; i++) {
                double offsetX = (player.getRandom().nextDouble() - 0.5) * 0.3;
                double offsetZ = (player.getRandom().nextDouble() - 0.5) * 0.3;

                player.level().addParticle(
                        net.minecraft.core.particles.ParticleTypes.CLOUD,
                        player.getX() + offsetX,
                        player.getY(),
                        player.getZ() + offsetZ,
                        0.0, 0.0, 0.0
                );
            }
        }
    }
}