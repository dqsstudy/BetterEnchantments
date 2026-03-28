package io.github.dqs147852.betterenchantments.enchantment.event;

import io.github.dqs147852.betterenchantments.config.AnvilResetConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = "betterenchantments")
public class AnvilResetHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    // 存储已处理的物品实体，防止重复处理
    private static final List<UUID> processedItems = new ArrayList<>();

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        // 检查功能是否启用
        if (!AnvilResetConfig.INSTANCE.enabled.get()) {
            return;
        }

        // 只在服务端处理，且只在每tick结束时处理
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide()) {
            return;
        }

        ServerLevel level = (ServerLevel) event.level;

        // 每10个tick检查一次，减少性能开销
        if (level.getGameTime() % 10 != 0) {
            return;
        }

        // 清理已处理的物品列表（防止内存泄漏）
        if (level.getGameTime() % 200 == 0) {
            processedItems.clear();
        }

        // 使用非常大的边界来获取所有物品实体
        AABB worldBounds = new AABB(
                -30000000, level.getMinBuildHeight(), -30000000,
                30000000, level.getMaxBuildHeight(), 30000000
        );

        Predicate<ItemEntity> predicate = entity -> !processedItems.contains(entity.getUUID());
        List<ItemEntity> itemEntities = level.getEntitiesOfClass(ItemEntity.class, worldBounds, predicate);

        for (ItemEntity itemEntity : itemEntities) {
            // 检查物品是否在铁砧上
            BlockPos posBelow = itemEntity.blockPosition().below();
            if (level.getBlockState(posBelow).getBlock() instanceof AnvilBlock) {
                // 检查该铁砧周围范围内的所有物品实体
                checkAndProcessAnvilRecipe(level, posBelow);
            }
        }
    }

    private static void checkAndProcessAnvilRecipe(ServerLevel level, BlockPos anvilPos) {
        // 获取配置的检测范围
        double range = AnvilResetConfig.INSTANCE.detectionRange.get();

        // 获取铁砧周围范围内的所有物品实体
        List<ItemEntity> nearbyItems = level.getEntitiesOfClass(ItemEntity.class,
                new AABB(anvilPos).inflate(range));

        if (nearbyItems.size() < 3) {
            return; // 至少需要3个物品
        }

        // 从配置获取所需数量
        int diamondBlocksRequired = AnvilResetConfig.INSTANCE.diamondBlocksRequired.get();
        int experienceBottlesRequired = AnvilResetConfig.INSTANCE.experienceBottlesRequired.get();

        // 查找配方所需的物品
        ItemEntity equipmentItem = null;
        ItemEntity diamondBlockItem = null;
        ItemEntity experienceBottleItem = null;

        for (ItemEntity itemEntity : nearbyItems) {
            ItemStack itemStack = itemEntity.getItem();
            Item item = itemStack.getItem();

            // 检查是否为装备或附魔书
            if (isEquipmentOrEnchantedBook(itemStack)) {
                if (equipmentItem == null) {
                    equipmentItem = itemEntity;
                }
            }
            // 检查是否为钻石块且数量足够
            else if (item == Items.DIAMOND_BLOCK && itemStack.getCount() >= diamondBlocksRequired) {
                if (diamondBlockItem == null) {
                    diamondBlockItem = itemEntity;
                }
            }
            // 检查是否为经验瓶且数量足够
            else if (item == Items.EXPERIENCE_BOTTLE && itemStack.getCount() >= experienceBottlesRequired) {
                if (experienceBottleItem == null) {
                    experienceBottleItem = itemEntity;
                }
            }
        }

        // 检查是否找到所有所需物品
        if (equipmentItem != null && diamondBlockItem != null && experienceBottleItem != null) {
            // 处理配方
            processResetRecipe(level, anvilPos, equipmentItem, diamondBlockItem, experienceBottleItem);
        }
    }

    private static boolean isEquipmentOrEnchantedBook(ItemStack itemStack) {
        Item item = itemStack.getItem();

        // 检查是否为装备
        if (item instanceof ArmorItem ||
                item instanceof SwordItem ||
                item instanceof DiggerItem || // 包括镐、斧、锹
                item instanceof BowItem ||
                item instanceof CrossbowItem ||
                item instanceof TridentItem ||
                item instanceof FishingRodItem ||
                item instanceof ShieldItem ||
                item instanceof ElytraItem) {
            return true;
        }

        // 检查是否为附魔书
        if (item instanceof EnchantedBookItem) {
            return true;
        }

        // 检查是否为带有附魔的其他物品
        if (itemStack.isEnchanted() ||
                (itemStack.hasTag() && itemStack.getTag().contains("Enchantments"))) {
            return true;
        }

        // 检查是否有修复成本（表示在铁砧中使用过）
        if (itemStack.hasTag() && itemStack.getTag().contains("RepairCost")) {
            return true;
        }

        return false;
    }

    private static void processResetRecipe(ServerLevel level, BlockPos anvilPos,
                                           ItemEntity equipmentItem, ItemEntity diamondBlockItem,
                                           ItemEntity experienceBottleItem) {

        // 标记这些物品为已处理
        processedItems.add(equipmentItem.getUUID());
        processedItems.add(diamondBlockItem.getUUID());
        processedItems.add(experienceBottleItem.getUUID());

        // 获取装备物品堆叠
        ItemStack equipmentStack = equipmentItem.getItem();

        // 重置修复成本（附魔惩罚）
        CompoundTag tag = equipmentStack.getOrCreateTag();
        int oldRepairCost = tag.getInt("RepairCost");
        tag.putInt("RepairCost", 0);

        LOGGER.info("重置物品 {} 的附魔惩罚，原修复成本：{}",
                equipmentStack.getDisplayName().getString(), oldRepairCost);

        // 检查是否消耗材料
        boolean consumeMaterials = AnvilResetConfig.INSTANCE.consumeMaterials.get();
        if (consumeMaterials) {
            int diamondBlocksRequired = AnvilResetConfig.INSTANCE.diamondBlocksRequired.get();
            int experienceBottlesRequired = AnvilResetConfig.INSTANCE.experienceBottlesRequired.get();

            consumeItemStack(diamondBlockItem, diamondBlocksRequired);
            consumeItemStack(experienceBottleItem, experienceBottlesRequired);
        }

        // 播放效果
        playResetEffects(level, anvilPos, equipmentItem);

        // 更新装备物品实体
        equipmentItem.setItem(equipmentStack);
    }

    private static void consumeItemStack(ItemEntity itemEntity, int amount) {
        ItemStack stack = itemEntity.getItem();
        int newCount = stack.getCount() - amount;

        if (newCount <= 0) {
            itemEntity.discard(); // 移除物品实体
        } else {
            stack.setCount(newCount);
            itemEntity.setItem(stack);
        }
    }

    private static void playResetEffects(ServerLevel level, BlockPos pos, ItemEntity equipmentItem) {
        // 播放铁砧使用音效
        level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);

        // 播放附魔成功音效
        level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.5F, 1.0F);

        // 生成粒子效果
        for (int i = 0; i < 20; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 0.5;
            double offsetY = level.random.nextDouble() * 0.5;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.5;

            level.sendParticles(ParticleTypes.ENCHANT,
                    equipmentItem.getX() + offsetX,
                    equipmentItem.getY() + offsetY,
                    equipmentItem.getZ() + offsetZ,
                    1, 0, 0, 0, 0);
        }

        // 生成闪光粒子
        for (int i = 0; i < 10; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 0.3;
            double offsetY = level.random.nextDouble() * 0.3;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.3;

            level.sendParticles(ParticleTypes.FLASH,
                    pos.getX() + 0.5 + offsetX,
                    pos.getY() + 1.0 + offsetY,
                    pos.getZ() + 0.5 + offsetZ,
                    1, 0, 0, 0, 0);
        }

        // 生成经验粒子
        for (int i = 0; i < 5; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 0.2;
            double offsetY = level.random.nextDouble() * 0.2;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.2;

            level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5 + offsetX,
                    pos.getY() + 0.5 + offsetY,
                    pos.getZ() + 0.5 + offsetZ,
                    1, 0, 0, 0, 0);
        }
    }
}