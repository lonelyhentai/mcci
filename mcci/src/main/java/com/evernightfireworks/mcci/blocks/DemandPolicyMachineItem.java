package com.evernightfireworks.mcci.blocks;

import com.evernightfireworks.mcci.gui.screens.DemandPolicyMachineScreen;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DemandPolicyMachineItem extends Item {
    private final Logger logger = LogManager.getFormatterLogger(DemandPolicyMachineItem.class.getName());
    public static final Identifier ID = new Identifier("mcci", "demand_policy_machine");
    public static final Item ITEM = new DemandPolicyMachineItem(CausalBlocks.MACHINE_ITEM_TEMPLATE);

    public DemandPolicyMachineItem(Settings settings) {
        super(settings);
    }

    public static void registerMain() {
        Registry.register(Registry.ITEM, ID, ITEM);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var res = super.use(world, user, hand);
        if(world.isClient()) {
            this.logger.info("on block used");
            MinecraftClient.getInstance()
                    .openScreen(new CottonClientScreen(new DemandPolicyMachineScreen(user)));
        }
        return res;
    }
}
