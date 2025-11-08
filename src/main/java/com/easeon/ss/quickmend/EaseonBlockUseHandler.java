package com.easeon.ss.quickmend;

import com.easeon.ss.core.util.system.EaseonLogger;
import com.easeon.ss.core.wrapper.EaseonPlayer;
import com.easeon.ss.core.wrapper.EaseonWorld;
import net.minecraft.block.AnvilBlock;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

public class EaseonBlockUseHandler {
    private static final EaseonLogger logger = EaseonLogger.of();

    public static ActionResult onBlockUse(ServerPlayerEntity playerEntity, World mcWorld, Hand hand, BlockHitResult hitResult) {
        var player = new EaseonPlayer(playerEntity);
        var world = new EaseonWorld(mcWorld);
        var pos = hitResult.getBlockPos();
        var anvil = world.getBlockState(pos);

        if (anvil.not(AnvilBlock.class))
            return ActionResult.PASS;

        var handItem = player.getStackInHand(hand);

        if (handItem.isEmpty() ||
            !handItem.isDamaged() ||
            !handItem.hasEnchant(Enchantments.MENDING))
        {
            return ActionResult.PASS;
        }

        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        int currentDamage = handItem.getDamage();
        int maxDurability = handItem.getMaxDamage();
        int repairAmount = player.isSneaking()
            ? currentDamage
            : Math.min(maxDurability / 10, currentDamage);

        if (!player.isCreative()) {
            int totalExperience = player.getTotalExperience();
            if (totalExperience <= 0) {
                return ActionResult.PASS;
            }

            // 경험치 제한 반영
            int maxRepairFromExp = totalExperience * 2;
            repairAmount = Math.min(repairAmount, maxRepairFromExp);

            if (repairAmount <= 0) {
                return ActionResult.FAIL;
            }

            int expToConsume = (repairAmount + 1) / 2;
            player.addXP(-expToConsume);
            anvil.damageAnvil(world, pos);
        }

        handItem.setDamage(currentDamage - repairAmount);
        world.playSound(player.getPos(), SoundEvents.BLOCK_ANVIL_USE, SoundCategory.PLAYERS, 1.0f);

        return ActionResult.SUCCESS;
    }
}