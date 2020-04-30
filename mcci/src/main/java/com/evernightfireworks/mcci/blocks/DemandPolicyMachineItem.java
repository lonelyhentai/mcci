package com.evernightfireworks.mcci.blocks;

import net.minecraft.item.Item;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class DemandPolicyMachineItem extends ToolItem {
    public static final Identifier ID = new Identifier("mcci", "demand_policy_machine");
    public static final Item ITEM = new DemandPolicyMachineItem(CausalBlocks.MACHINE_ITEM_TEMPLATE);

    public DemandPolicyMachineItem(Settings settings) {
        super(ToolMaterials.IRON, settings);
    }

    public static void registerMain() {
        Registry.register(Registry.ITEM, ID, ITEM);
    }
}
