package com.easeon.ss.quickmend;

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

public class QuickMendHandler {
    public static ActionResult onBlockUse(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (!Easeon.CONFIG.isEnabled()) return ActionResult.PASS;

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

        // 크리에이티브 모드 체크
        boolean isCreative = player.isCreative();

        int totalExperience = getTotalExperience(player);

        // 경험치가 없고 크리에이티브도 아니면 기본 모루 GUI 열기
        if (!isCreative && totalExperience <= 0) {
            return ActionResult.PASS;
        }

        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        int currentDamage = stack.getDamage();
        int maxDurability = stack.getMaxDamage();
        int repairAmount;

        if (player.isSneaking()) {
            // 크리에이티브면 전체 수리, 아니면 경험치만큼
            repairAmount = isCreative ? currentDamage : Math.min(totalExperience * 2, currentDamage);
        } else {
            int tenPercentRepair = maxDurability / 10;
            repairAmount = Math.min(tenPercentRepair, currentDamage);

            if (!isCreative) {
                int maxRepairFromExp = totalExperience * 2;
                repairAmount = Math.min(repairAmount, maxRepairFromExp);
            }
        }

        if (repairAmount <= 0) {
            return ActionResult.FAIL;
        }

        int expToConsume = (repairAmount + 1) / 2;

        stack.setDamage(currentDamage - repairAmount);

        // 크리에이티브가 아닐 때만 경험치 소모
        if (!isCreative) {
            removeExperience(player, expToConsume);
        }

        // 크리에이티브가 아닐 때만 모루 내구도 소모
        if (!isCreative) {
            damageAnvil(world, pos, state);
        }

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