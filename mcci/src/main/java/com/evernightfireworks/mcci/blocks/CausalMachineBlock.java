package com.evernightfireworks.mcci.blocks;

import com.evernightfireworks.mcci.services.DemandPoliceService;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CausalMachineBlock extends Block {

    private static final Logger logger = LogManager.getFormatterLogger(CausalMachineBlock.class.getName());
    public static final Identifier ID = new Identifier("mcci", "causal_machine");
    public static final Block BLOCK = new CausalMachineBlock(CausalBlocks.MACHINE_BLOCK_TEMPLATE.drops(ID).build());
    public static final BlockItem ITEM = new BlockItem(BLOCK, CausalBlocks.MACHINE_ITEM_TEMPLATE);

    public static void registerMain() {
        Registry.register(Registry.BLOCK, ID, BLOCK);
        Registry.register(Registry.ITEM,
                ID, ITEM);
    }

    public CausalMachineBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        logger.info("on used");
        if(world.isClient()) {
            var d = new DemandPoliceService();
            d.calculateHungerFactor(player);
        }
        return ActionResult.SUCCESS;
    }
}
