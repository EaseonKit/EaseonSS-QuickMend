package com.easeon.ss.quickmend;

import com.easeon.ss.core.util.system.EaseonLogger;
import com.easeon.ss.core.wrapper.EaseonBlockHit;
import com.easeon.ss.core.wrapper.EaseonPlayer;
import com.easeon.ss.core.wrapper.EaseonWorld;
import net.minecraft.block.AnvilBlock;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class EaseonBlockUseHandler {
    private static final EaseonLogger logger = EaseonLogger.of();
    private static final int REPAIR_RATE_PERCENT = 10;
    private static final int XP_TO_DURABILITY_RATIO = 2;

    public static ActionResult onBlockUse(EaseonWorld world, EaseonPlayer player, Hand hand, EaseonBlockHit hit) {
        final var pos = hit.getBlockPos();
        final var anvil = world.getBlockState(pos);
        if (anvil.not(AnvilBlock.class) || world.isClient())
            return ActionResult.PASS;

        final var tool = player.getStackInHand(hand);
        final int damage = tool.getDamage();
        if (damage <= 0 || !tool.hasEnchant(Enchantments.MENDING))
            return ActionResult.PASS;

        int repairAmount = player.isSneaking()
            ? damage
            : Math.min(tool.getMaxDamage() / REPAIR_RATE_PERCENT, damage);

        if (player.isSurvival()) {
            final int xp = player.getTotalExperience();
            if (xp <= 0)
                return ActionResult.PASS;

            // 경험치 제한 반영
            repairAmount = Math.min(repairAmount, xp * XP_TO_DURABILITY_RATIO);
            final int expToConsume = (repairAmount + 1) / XP_TO_DURABILITY_RATIO;
            player.addXP(-expToConsume);
            anvil.damageAnvil(world, pos);
        }
        tool.repair(repairAmount);
        world.playSound(player.getPos(), SoundEvents.BLOCK_ANVIL_USE, SoundCategory.PLAYERS, 1.0f);

        return ActionResult.SUCCESS;
    }
}