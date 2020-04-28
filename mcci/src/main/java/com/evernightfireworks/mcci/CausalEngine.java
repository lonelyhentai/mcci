package com.evernightfireworks.mcci;

import com.evernightfireworks.mcci.blocks.CausalBlocks;
import com.evernightfireworks.mcci.services.CraftingPolicyService;
import net.fabricmc.api.ModInitializer;


import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CausalEngine implements ModInitializer {
    private static final Logger logger = LogManager.getFormatterLogger(CausalEngine.class.getName());
    public static CraftingPolicyService CRAFTING_POLICY_SERVICE;

    @Override
    public void onInitialize() {
        logger.info("Causal engine initializing...");
        CausalBlocks.registerMain();
        CraftingPolicyService.registerMain();
        CRAFTING_POLICY_SERVICE = new CraftingPolicyService();
        logger.info("Causal engine initialized");
    }
}
