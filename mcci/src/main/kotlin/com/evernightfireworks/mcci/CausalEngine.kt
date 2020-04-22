package com.evernightfireworks.mcci

import net.fabricmc.api.ModInitializer
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager

class CausalEngine:  ModInitializer {
    companion object {
        val CAUSAL_MACHINE = CausalMachine(Item.Settings().group(ItemGroup.MISC).maxCount(1))
    }
    val logger = LogManager.getFormatterLogger("KotlinLanguageTest")
    override fun onInitialize() {
        logger.info("Hello from causal engine")
        Registry.register(Registry.ITEM,
                Identifier("mcci", "causal_machine"), CAUSAL_MACHINE);
    }
}