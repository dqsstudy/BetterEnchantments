package io.github.dqs147852.betterenchantments.event;

import io.github.dqs147852.betterenchantments.enchantment.ModEnchantments;
import io.github.dqs147852.betterenchantments.network.PacketDoubleJump;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "betterenchantments")
public class ServerTickHandler {
    // 服务端存储玩家是否在地面
    private static final Map<UUID, Boolean> serverGroundStates = new HashMap<>();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // 从事件中获取服务器实例
        net.minecraft.server.MinecraftServer server = event.getServer();
        if (server == null) return;

        // 遍历所有在线玩家
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID playerId = player.getUUID();

            // 获取之前的在地面状态
            Boolean wasOnGround = serverGroundStates.get(playerId);
            boolean isOnGround = player.onGround();

            // 如果玩家落地，重置二段跳状态
            if (wasOnGround != null && !wasOnGround && isOnGround) {
                // 检查玩家是否穿着二段跳附魔的护腿
                ItemStack leggings = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS);
                boolean hasDoubleJumpEnchantment = EnchantmentHelper.getItemEnchantmentLevel(
                        ModEnchantments.DOUBLE_JUMP.get(), leggings) > 0;

                if (hasDoubleJumpEnchantment) {
                    PacketDoubleJump.resetJumpState(player);
                }
            }

            // 更新状态
            serverGroundStates.put(playerId, isOnGround);
        }
    }

    // 玩家退出时清理状态
    @SubscribeEvent
    public static void onPlayerLogout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            serverGroundStates.remove(event.getEntity().getUUID());
            PacketDoubleJump.removePlayerState(event.getEntity().getUUID());
        }
    }
}