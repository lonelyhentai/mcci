package com.evernightfireworks.mcci.block;

import com.evernightfireworks.mcci.gui.CausalMachineBlockController;
import com.evernightfireworks.mcci.gui.CausalMachineBlockScreen;
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

public class CausalMachineBlock extends Block implements BlockEntityProvider {
    public static Identifier ID = new Identifier("mcci", "causal_machine");
    private final static Logger logger = LogManager.getFormatterLogger(ID.toString());
    public static BlockEntityType<CausalMachineBlockEntity> ENTITY;
    public static final Block BLOCK = new CausalMachineBlock(CausalBlocks.MACHINE_BLOCK_TEMPLATE.drops(ID).build());
    public static final BlockItem ITEM = new BlockItem(BLOCK, CausalBlocks.MACHINE_ITEM_TEMPLATE);

    public static void registerMain() {
        Registry.register(Registry.BLOCK, ID, BLOCK);
        ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, ID,
                BlockEntityType.Builder.create(CausalMachineBlockEntity::new, BLOCK).build(null));
        Registry.register(Registry.ITEM,
                ID, ITEM);
        ContainerProviderRegistry.INSTANCE.registerFactory(ID, (syncId, id, player, buf) ->
                new CausalMachineBlockController(syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos())));
    }

    public static void registerClient() {
        ScreenProviderRegistry.INSTANCE.registerFactory(ID, (syncId, id, player, buf)->
                new CausalMachineBlockScreen(
                        new CausalMachineBlockController(
                                syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos())),player));
    }

    public CausalMachineBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        logger.info("on used");
        if(!world.isClient) {
            BlockEntity be = world.getBlockEntity(pos);
            ContainerProviderRegistry.INSTANCE.openContainer(ID, player, (packetByteBuf -> packetByteBuf.writeBlockPos(pos)));
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockEntity createBlockEntity(BlockView var1) {
        return new CausalMachineBlockEntity();
    }
}
