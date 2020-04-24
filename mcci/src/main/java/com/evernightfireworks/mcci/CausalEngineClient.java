package com.evernightfireworks.mcci;

import com.evernightfireworks.mcci.block.CausalBlocks;
import net.fabricmc.api.ClientModInitializer;

public class CausalEngineClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CausalBlocks.registerClient();
    }
}
