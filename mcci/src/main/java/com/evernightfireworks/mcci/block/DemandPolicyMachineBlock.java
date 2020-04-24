package com.evernightfireworks.mcci.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class DemandPolicyMachineBlock extends Block {
    public static final Identifier ID = new Identifier("mcci", "demand_policy_machine");
    public static final Block BLOCK = new DemandPolicyMachineBlock(CausalBlocks.MACHINE_BLOCK_TEMPLATE.drops(
            ID).build());
    public static final BlockItem ITEM = new BlockItem(BLOCK, CausalBlocks.MACHINE_ITEM_TEMPLATE);

    public DemandPolicyMachineBlock(Settings settings) {
        super(settings);
    }

    public static void registerMain() {
        Registry.register(Registry.BLOCK, ID, BLOCK);
        Registry.register(Registry.ITEM, ID, ITEM);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return ActionResult.PASS;
    }
}
