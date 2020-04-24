package com.evernightfireworks.mcci;

import com.evernightfireworks.mcci.block.CausalBlocks;
import net.fabricmc.api.ModInitializer;


import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CausalEngine implements ModInitializer {

    public static final Identifier ID = new Identifier("mcci","causal_engine");
    private static final Logger logger = LogManager.getFormatterLogger("MCCI");

    @Override
    public void onInitialize() {
        logger.info("Causal engine initializing...");
        CausalBlocks.registerMain();
        logger.info("Causal engine initialized");
    }
}
