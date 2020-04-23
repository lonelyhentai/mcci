package com.evernightfireworks.mcci;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.block.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CausalEngine implements ModInitializer {
    public static final FabricBlockSettings MACHINE_BLOCK_TEMPLATE
            = FabricBlockSettings.of(Material.METAL).strength(5.0f, 5.0f);
    public static final Item.Settings MACHINE_ITEM_TEMPLATE = new Item.Settings().maxCount(32);
    public static final Identifier CAUSAL_MACHINE_IDENTIFY = new Identifier("mcci", "causal_machine");
    public static final Block CAUSAL_MACHINE_BLOCK = new CausalMachineBlock(MACHINE_BLOCK_TEMPLATE.drops(CAUSAL_MACHINE_IDENTIFY).build());
    public static final BlockItem CAUSAL_MACHINE_ITEM = new BlockItem(CAUSAL_MACHINE_BLOCK, MACHINE_ITEM_TEMPLATE);
    public static final Identifier CRAFTING_POLICY_MACHINE_IDENTIFY = new Identifier("mcci", "crafting_policy_machine");
    public static final Block CRAFTING_POLICY_MACHINE_BLOCK = new CraftingPolicyMachineBlock(MACHINE_BLOCK_TEMPLATE.drops(
            CRAFTING_POLICY_MACHINE_IDENTIFY).build());
    public static final BlockItem CRAFTING_POLICY_MACHINE_ITEM = new BlockItem(CRAFTING_POLICY_MACHINE_BLOCK, MACHINE_ITEM_TEMPLATE);
    public static final Identifier DEMAND_POLICY_MACHINE_IDENTIFY = new Identifier("mcci", "demand_policy_machine");
    public static final Block DEMAND_POLICY_MACHINE_BLOCK = new DemandPolicyMachineBlock(MACHINE_BLOCK_TEMPLATE.drops(
            DEMAND_POLICY_MACHINE_IDENTIFY).build());
    public static final BlockItem DEMAND_POLICY_MACHINE_ITEM = new BlockItem(DEMAND_POLICY_MACHINE_BLOCK, MACHINE_ITEM_TEMPLATE);
    public static final ItemGroup CAUSAL_GROUP =
            FabricItemGroupBuilder.create(new Identifier("mcci", "general"))
                    .icon(() -> new ItemStack(CAUSAL_MACHINE_ITEM))
                    .appendItems((it) -> {
                        List<BlockItem> items = Arrays.asList(CAUSAL_MACHINE_ITEM, CRAFTING_POLICY_MACHINE_ITEM, DEMAND_POLICY_MACHINE_ITEM);
                        List<ItemStack> mapped = items.stream().map(ItemStack::new).collect(Collectors.toList());
                        it.addAll(mapped);
                    })
                    .build();
    private static final Logger logger = LogManager.getFormatterLogger("MCCI");

    @Override
    public void onInitialize() {
        logger.info("Hello from causal engine");
        /* register blocks */
        Registry.register(Registry.BLOCK,
                CAUSAL_MACHINE_IDENTIFY, CAUSAL_MACHINE_BLOCK);
        Registry.register(Registry.BLOCK, CRAFTING_POLICY_MACHINE_IDENTIFY
                , CRAFTING_POLICY_MACHINE_BLOCK);
        Registry.register(Registry.BLOCK, DEMAND_POLICY_MACHINE_IDENTIFY
                , DEMAND_POLICY_MACHINE_BLOCK);
        /* register block items */
        Registry.register(Registry.ITEM,
                CAUSAL_MACHINE_IDENTIFY, CAUSAL_MACHINE_ITEM);
        Registry.register(Registry.ITEM,
                CRAFTING_POLICY_MACHINE_IDENTIFY, CRAFTING_POLICY_MACHINE_ITEM);
        Registry.register(Registry.ITEM,
                DEMAND_POLICY_MACHINE_IDENTIFY, DEMAND_POLICY_MACHINE_ITEM);
    }
}
