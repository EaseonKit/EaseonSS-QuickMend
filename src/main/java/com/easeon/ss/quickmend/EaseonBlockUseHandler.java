package com.easeon.ss.quickmend;

import com.easeon.ss.core.game.EaseonSound;
import com.easeon.ss.core.helper.BlockHelper;
import com.easeon.ss.core.helper.PlayerHelper;
import com.easeon.ss.core.util.system.EaseonLogger;
import net.minecraft.block.AnvilBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

public class EaseonBlockUseHandler {
    private static final EaseonLogger logger = EaseonLogger.of();

    public static ActionResult onBlockUse(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        var pos = hitResult.getBlockPos();
        var state = world.getBlockState(pos);

        if (BlockHelper.not(state, AnvilBlock.class)) {
            return ActionResult.PASS;
        }

        var stack = player.getStackInHand(hand);

        if (stack.isEmpty() ||
            !stack.isDamaged() ||
            !stack.hasEnchantments() ||
            EnchantmentHelper.getEnchantments(stack).getEnchantments().stream().noneMatch(entry -> entry.matchesKey(Enchantments.MENDING))
        ) {
            return ActionResult.PASS;
        }

        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        int currentDamage = stack.getDamage();
        int maxDurability = stack.getMaxDamage();
        int repairAmount;

        if (player.isSneaking()) {
            // 쉬프트 상태면 전체 수리
            repairAmount = currentDamage;
        } else {
            // 10% 수리
            repairAmount = Math.min(maxDurability / 10, currentDamage);
        }

        if (!player.isCreative()) {
            int totalExperience = PlayerHelper.getTotalExperience(player);
            if (totalExperience <= 0) {
                return ActionResult.FAIL;
            }

            // 경험치 제한 반영
            int maxRepairFromExp = totalExperience * 2;
            repairAmount = Math.min(repairAmount, maxRepairFromExp);

            if (repairAmount <= 0) {
                return ActionResult.FAIL;
            }

            int expToConsume = (repairAmount + 1) / 2;
            PlayerHelper.removeExperience(player, expToConsume);
            BlockHelper.damageAnvil(world, pos, state);
        }

        stack.setDamage(currentDamage - repairAmount);
        EaseonSound.playAll(world, player, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.PLAYERS);

        return ActionResult.SUCCESS;
    }
}