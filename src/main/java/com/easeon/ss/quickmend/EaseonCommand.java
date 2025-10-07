package com.easeon.ss.quickmend;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class EaseonCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("easeon")
            .requires(source -> source.hasPermissionLevel(Easeon.CONFIG.getRequiredOpLevel()))
            .then(CommandManager.literal("quickmend")
                .executes(ctx -> {
                    boolean enabled = Easeon.CONFIG.isEnabled();
                    ctx.getSource().sendFeedback(
                            () -> Text.literal("Easeon ")
                                    .formatted(Formatting.YELLOW)
                                    .append(Text.literal("QuickMend is currently: ")
                                            .formatted(Formatting.WHITE))
                                    .append(Text.literal(enabled ? "ENABLED" : "DISABLED")
                                            .formatted(enabled ? Formatting.GREEN : Formatting.RED)),
                            false
                    );
                    return 1;
                })
                    .then(CommandManager.literal("on")
                        .executes(ctx -> {
                            if (Easeon.CONFIG.isEnabled()) {
                                ctx.getSource().sendFeedback(
                                        () -> Text.literal("Easeon ")
                                                .formatted(Formatting.YELLOW)
                                                .append(Text.literal("QuickMend is already enabled!")
                                                        .formatted(Formatting.YELLOW)),
                                        false
                                );
                            } else {
                                Easeon.CONFIG.setEnabled(true);
                                ctx.getSource().sendFeedback(
                                        () -> Text.literal("Easeon ")
                                                .formatted(Formatting.YELLOW)
                                                .append(Text.literal("QuickMend has been ")
                                                        .formatted(Formatting.WHITE))
                                                .append(Text.literal("ENABLED")
                                                        .formatted(Formatting.GREEN)),
                                        true
                                );
                            }
                            return 1;
                        })
                    )
                    .then(CommandManager.literal("off")
                        .executes(ctx -> {
                            if (!Easeon.CONFIG.isEnabled()) {
                                ctx.getSource().sendFeedback(
                                        () -> Text.literal("Easeon ")
                                                .formatted(Formatting.YELLOW)
                                                .append(Text.literal("QuickMend is already disabled!")
                                                        .formatted(Formatting.YELLOW)),
                                        false
                                );
                            } else {
                                Easeon.CONFIG.setEnabled(false);
                                ctx.getSource().sendFeedback(
                                        () -> Text.literal("Easeon ")
                                                .formatted(Formatting.YELLOW)
                                                .append(Text.literal("QuickMend has been ")
                                                        .formatted(Formatting.WHITE))
                                                .append(Text.literal("DISABLED")
                                                        .formatted(Formatting.RED)),
                                        true
                                );
                            }
                            return 1;
                        })
                    )
            )
        );
    }
}