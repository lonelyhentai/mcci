package com.evernightfireworks.mcci.blocks;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.block.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CausalBlocks {
    public static final FabricBlockSettings MACHINE_BLOCK_TEMPLATE
            = FabricBlockSettings.of(Material.METAL).strength(5.0f, 5.0f);
    public static final Item.Settings MACHINE_ITEM_TEMPLATE = new Item.Settings().maxCount(1);
    /* causal item group */
    public static final ItemGroup GROUP =
            FabricItemGroupBuilder.create(new Identifier("mcci", "general"))
                    .icon(() -> new ItemStack(CausalMachineBlock.ITEM))
                    .appendItems((it) -> {
                        List<Item> items = Arrays.asList(
                                CausalMachineBlock.ITEM,
                                CraftingPolicyMachineBlock.ITEM,
                                DemandPolicyMachineItem.ITEM
                        );
                        List<ItemStack> mapped = items.stream().map(ItemStack::new).collect(Collectors.toList());
                        it.addAll(mapped);
                    })
                    .build();

    public static void registerMain() {
        CausalMachineBlock.registerMain();
        CraftingPolicyMachineBlock.registerMain();
        DemandPolicyMachineItem.registerMain();
    }

    public static void registerClient() {
        CraftingPolicyMachineBlock.registerClient();
    }
}
