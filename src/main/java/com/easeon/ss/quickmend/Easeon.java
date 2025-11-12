package com.easeon.ss.quickmend;

import com.easeon.ss.core.api.common.base.BaseToggleModule;
import com.easeon.ss.core.api.definitions.enums.EventPhase;
import com.easeon.ss.core.api.events.EaseonBlockUse;
import com.easeon.ss.core.api.events.EaseonBlockUse.BlockUseTask;
import net.fabricmc.api.ModInitializer;

public class Easeon extends BaseToggleModule implements ModInitializer {
    private BlockUseTask task;

    @Override
    public void onInitialize() {
        logger.info("Initialized!");
    }

    public void updateTask() {
        if (config.enabled && task == null) {
            task = EaseonBlockUse.on(EventPhase.BEFORE, EaseonBlockUseHandler::onBlockUse);
        }
        if (!config.enabled && task != null) {
            EaseonBlockUse.off(task);
            task = null;
        }
    }
}