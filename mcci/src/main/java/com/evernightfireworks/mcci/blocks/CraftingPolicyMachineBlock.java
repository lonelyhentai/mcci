package com.evernightfireworks.mcci.blocks;

import com.evernightfireworks.mcci.gui.controllers.CraftingPolicyMachineController;
import com.evernightfireworks.mcci.gui.screens.CraftingPolicyMachineScreen;
import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry;
import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.container.BlockContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CraftingPolicyMachineBlock extends Block implements BlockEntityProvider {
    public CraftingPolicyMachineBlock(Settings settings) {
        super(settings);
    }

    private final Logger logger = LogManager.getFormatterLogger(CraftingPolicyMachineBlock.class.getName());
    public static final Identifier ID = new Identifier("mcci", "crafting_policy_machine");
    public static final Block BLOCK = new CraftingPolicyMachineBlock(CausalBlocks.MACHINE_BLOCK_TEMPLATE.drops(
            ID).build());
    public static final BlockItem ITEM = new BlockItem(BLOCK, CausalBlocks.MACHINE_ITEM_TEMPLATE);
    public static BlockEntityType<CraftingPolicyMachineEntity> ENTITY;

    public static void registerMain() {
        Registry.register(Registry.BLOCK, ID, BLOCK);
        ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, ID,
                BlockEntityType.Builder.create(CraftingPolicyMachineEntity::new, BLOCK).build(null));
        Registry.register(Registry.ITEM, ID, ITEM);
        ContainerProviderRegistry.INSTANCE.registerFactory(ID, (syncId, id, player, buf)->
                new CraftingPolicyMachineController(
                        syncId, player.inventory,
                        BlockContext.create(player.world, buf.readBlockPos())));
    }

    public static void registerClient() {
        ScreenProviderRegistry.INSTANCE.registerFactory(ID, (syncId, id, player, buf) ->
                new CraftingPolicyMachineScreen(
                        new CraftingPolicyMachineController(
                                syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos())), player));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(!world.isClient) {
            ContainerProviderRegistry.INSTANCE.openContainer(ID, player, (packetByteBuf -> packetByteBuf.writeBlockPos(pos)));
        } else {
            logger.info("on block used");
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockEntity createBlockEntity(BlockView view) {
        return new CraftingPolicyMachineEntity();
    }
}
