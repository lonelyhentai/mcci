package com.evernightfireworks.mcci.gui;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.minecraft.entity.player.PlayerEntity;

public class CausalMachineBlockScreen extends CottonInventoryScreen<CausalMachineBlockController> {

    public CausalMachineBlockScreen(CausalMachineBlockController container, PlayerEntity player) {
        super(container, player);
    }
}
