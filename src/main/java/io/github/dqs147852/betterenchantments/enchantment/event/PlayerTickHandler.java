package io.github.dqs147852.betterenchantments.event;

import io.github.dqs147852.betterenchantments.enchantment.ModEnchantments;
import io.github.dqs147852.betterenchantments.network.PacketDoubleJump;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "betterenchantments")
public class PlayerTickHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    // 服务端存储玩家是否在地面
    private static final Map<UUID, Boolean> serverGroundStates = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // 只在服务端处理
        if (event.player.level().isClientSide()) return;

        if (!(event.player instanceof ServerPlayer)) return;

        ServerPlayer player = (ServerPlayer) event.player;
        UUID playerId = player.getUUID();

        // 获取之前的在地面状态
        Boolean wasOnGround = serverGroundStates.get(playerId);
        boolean isOnGround = player.onGround();

        LOGGER.debug("玩家 {} 地面状态: 之前={}, 现在={}",
                player.getName().getString(), wasOnGround, isOnGround);

        // 如果玩家落地，重置二段跳状态
        if (wasOnGround != null && !wasOnGround && isOnGround) {
            // 检查玩家是否穿着二段跳附魔的护腿
            ItemStack leggings = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS);
            boolean hasDoubleJumpEnchantment = EnchantmentHelper.getItemEnchantmentLevel(
                    ModEnchantments.DOUBLE_JUMP.get(), leggings) > 0;

            if (hasDoubleJumpEnchantment) {
                LOGGER.info("玩家 {} 落地，重置二段跳状态", player.getName().getString());
                PacketDoubleJump.resetJumpState(player);
            }
        }

        // 更新状态
        serverGroundStates.put(playerId, isOnGround);
    }

    // 玩家退出时清理状态
    @SubscribeEvent
    public static void onPlayerLogout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            serverGroundStates.remove(event.getEntity().getUUID());
            PacketDoubleJump.removePlayerState(event.getEntity().getUUID());
            LOGGER.debug("玩家 {} 退出，清理地面状态", event.getEntity().getName().getString());
        }
    }
}