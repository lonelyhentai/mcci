package com.evernightfireworks.mcci

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.block.Material
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class CausalEngine:  ModInitializer {
    companion object {
        val MACHINE_BLOCK_TEMPLATE = FabricBlockSettings.of(Material.METAL).strength(5.0f, 5.0f)
        val MACHINE_ITEM_TEMPLATE = Item.Settings().maxCount(1)
        val CAUSAL_MACHINE_IDENTIFY = Identifier("mcci", "causal_machine")
        val CAUSAL_MACHINE_BLOCK = CausalMachineBlock(MACHINE_BLOCK_TEMPLATE.drops(CAUSAL_MACHINE_IDENTIFY).build())
        val CAUSAL_MACHINE_ITEM = BlockItem(CAUSAL_MACHINE_BLOCK, MACHINE_ITEM_TEMPLATE)
        val CRAFTING_POLICY_MACHINE_IDENTIFY = Identifier("mcci", "crafting_policy_machine")
        val CRAFTING_POLICY_MACHINE_BLOCK = CraftingPolicyMachineBlock(MACHINE_BLOCK_TEMPLATE.drops(
            CRAFTING_POLICY_MACHINE_IDENTIFY).build())
        val CRAFTING_POLICY_MACHINE_ITEM = BlockItem(CRAFTING_POLICY_MACHINE_BLOCK, MACHINE_ITEM_TEMPLATE)
        val DEMAND_POLICY_MACHINE_IDENTIFY = Identifier("mcci", "demand_policy_machine")
        val DEMAND_POLICY_MACHINE_BLOCK = DemandPolicyMachineBlock(MACHINE_BLOCK_TEMPLATE.drops(
            DEMAND_POLICY_MACHINE_IDENTIFY).build())
        val DEMAND_POLICY_MACHINE_ITEM = BlockItem(DEMAND_POLICY_MACHINE_BLOCK, MACHINE_ITEM_TEMPLATE)
        @Suppress("UNUSED")
        val CAUSAL_GROUP =
            FabricItemGroupBuilder.create(Identifier("mcci", "general"))
                .icon { ItemStack(CAUSAL_MACHINE_ITEM) }
                .appendItems{
                    val items = listOf(CAUSAL_MACHINE_ITEM, CRAFTING_POLICY_MACHINE_ITEM, DEMAND_POLICY_MACHINE_ITEM).map { ItemStack(it) }
                    it.addAll(items)
                }
                .build()
    }

    private val logger: Logger = LogManager.getFormatterLogger("KotlinLanguageTest")
    override fun onInitialize() {
        logger.info("Hello from causal engine")
        /** register blocks **/
        Registry.register(Registry.BLOCK,
                CAUSAL_MACHINE_IDENTIFY, CAUSAL_MACHINE_BLOCK)
        Registry.register(Registry.BLOCK, CRAFTING_POLICY_MACHINE_IDENTIFY
            , CRAFTING_POLICY_MACHINE_BLOCK)
        Registry.register(Registry.BLOCK, DEMAND_POLICY_MACHINE_IDENTIFY
            , DEMAND_POLICY_MACHINE_BLOCK)
        /** register block items **/
        Registry.register(Registry.ITEM,
            CAUSAL_MACHINE_IDENTIFY, CAUSAL_MACHINE_ITEM)
        Registry.register(Registry.ITEM,
            CRAFTING_POLICY_MACHINE_IDENTIFY, CRAFTING_POLICY_MACHINE_ITEM)
        Registry.register(Registry.ITEM,
            DEMAND_POLICY_MACHINE_IDENTIFY, DEMAND_POLICY_MACHINE_ITEM)
    }
}