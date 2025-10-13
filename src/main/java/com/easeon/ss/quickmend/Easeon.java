package com.easeon.ss.quickmend;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Easeon implements ModInitializer {
    public static final String MOD_ID = "easeon-quickmend";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final ConfigManager CONFIG = new ConfigManager();

    @Override
    public void onInitialize() {
        LOGGER.info("QuickMend Mod Initializing...");

        CONFIG.load();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            EaseonCommand.register(dispatcher);
            LOGGER.info("Commands registered!");
        });

        UseBlockCallback.EVENT.register(QuickMendHandler::onBlockUse);

        LOGGER.info("QuickMend Mod Initialized!");
    }
}