package io.github.dqs147852.betterenchantments.enchantment.event;

import io.github.dqs147852.betterenchantments.ModEnchantments;
import io.github.dqs147852.betterenchantments.network.PacketDoubleJump;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = "betterenchantments")
public class GroundDetectionHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // 只在服务端处理
        if (event.player.level().isClientSide()) return;

        if (!(event.player instanceof ServerPlayer)) return;

        ServerPlayer player = (ServerPlayer) event.player;

        // 检查玩家是否穿着带有二段跳附魔的护腿
        ItemStack leggings = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS);
        boolean hasDoubleJumpEnchantment = EnchantmentHelper.getItemEnchantmentLevel(
                ModEnchantments.DOUBLE_JUMP.get(), leggings) > 0;

        if (!hasDoubleJumpEnchantment) return;

        // 如果玩家落地，重置二段跳状态
        if (player.onGround()) {
            PacketDoubleJump.resetJumpState(player);
        }
    }

    // 玩家退出时清理状态
    @SubscribeEvent
    public static void onPlayerLogout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            PacketDoubleJump.removePlayerState(event.getEntity().getUUID());
            LOGGER.debug("玩家 {} 退出，清理二段跳状态", event.getEntity().getName().getString());
        }
    }

    // 玩家重生时重置状态
    @SubscribeEvent
    public static void onPlayerRespawn(net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            PacketDoubleJump.resetJumpState((ServerPlayer) event.getEntity());
           LOGGER.debug("玩家 {} 重生，重置二段跳状态", event.getEntity().getName().getString());
        }
    }
}