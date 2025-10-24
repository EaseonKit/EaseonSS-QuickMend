package com.easeon.ss.quickmend;

import com.easeon.ss.core.util.system.EaseonLogger;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EaseonBlockUseHandler {
    private static EaseonLogger logger = EaseonLogger.of();

    public static ActionResult onBlockUse(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (!(state.getBlock() instanceof AnvilBlock)) {
            return ActionResult.PASS;
        }

        ItemStack stack = player.getStackInHand(hand);

        if (stack.isEmpty() || !stack.isDamaged() ||
                !stack.hasEnchantments() || EnchantmentHelper.getEnchantments(stack).getEnchantments().stream().noneMatch(entry -> entry.matchesKey(Enchantments.MENDING))
        ) {
            return ActionResult.PASS;
        }

        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        int totalExperience = getTotalExperience(player);
        if (totalExperience <= 0) {
            return ActionResult.FAIL;
        }

        int currentDamage = stack.getDamage();
        int maxDurability = stack.getMaxDamage();
        int repairAmount;

        if (player.isSneaking()) {
            repairAmount = Math.min(totalExperience * 2, currentDamage);
        } else {
            int tenPercentRepair = maxDurability / 10;
            repairAmount = Math.min(tenPercentRepair, currentDamage);

            int maxRepairFromExp = totalExperience * 2;
            repairAmount = Math.min(repairAmount, maxRepairFromExp);
        }

        if (repairAmount <= 0) {
            return ActionResult.FAIL;
        }

        int expToConsume = (repairAmount + 1) / 2;

        stack.setDamage(currentDamage - repairAmount);
        removeExperience(player, expToConsume);
        damageAnvil(world, pos, state);

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_ANVIL_USE, SoundCategory.PLAYERS,
                0.5f, 1.0f);

        return ActionResult.SUCCESS;
    }

    private static void damageAnvil(World world, BlockPos pos, BlockState state) {
        if (world.random.nextFloat() < 0.12f) {
            BlockState damagedState = AnvilBlock.getLandingState(state);
            if (damagedState == null) {
                world.removeBlock(pos, false);
                world.syncWorldEvent(1029, pos, 0);
            } else {
                world.setBlockState(pos, damagedState, 2);
                world.syncWorldEvent(1030, pos, 0);
            }
        }
    }

    private static int getTotalExperience(PlayerEntity player) {
        int level = player.experienceLevel;
        int totalExp = 0;

        if (level <= 16) {
            totalExp = level * level + 6 * level;
        } else if (level <= 31) {
            totalExp = (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            totalExp = (int) (4.5 * level * level - 162.5 * level + 2220);
        }

        totalExp += Math.round(player.experienceProgress * getExpForLevel(level));

        return totalExp;
    }

    private static int getExpForLevel(int level) {
        if (level <= 15) {
            return 2 * level + 7;
        } else if (level <= 30) {
            return 5 * level - 38;
        } else {
            return 9 * level - 158;
        }
    }

    private static void removeExperience(PlayerEntity player, int amount) {
        int totalExp = getTotalExperience(player);
        int newTotalExp = Math.max(0, totalExp - amount);

        int newLevel = 0;
        int expForNewLevel = 0;

        if (newTotalExp == 0) {
            player.experienceLevel = 0;
            player.experienceProgress = 0.0f;
            player.totalExperience = 0;
            return;
        }

        while (expForNewLevel < newTotalExp) {
            newLevel++;
            expForNewLevel += getExpForLevel(newLevel - 1);
        }
        newLevel--;
        expForNewLevel -= getExpForLevel(newLevel);

        int expInCurrentLevel = newTotalExp - expForNewLevel;
        float progress = (float) expInCurrentLevel / getExpForLevel(newLevel);

        player.experienceLevel = newLevel;
        player.experienceProgress = progress;
        player.totalExperience = newTotalExp;
    }
}