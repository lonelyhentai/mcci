package com.evernightfireworks.mcci.gui.screen;

import com.evernightfireworks.mcci.gui.controller.CausalMachineBlockController;
import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import net.minecraft.entity.player.PlayerEntity;

public class CausalMachineBlockScreen extends CottonInventoryScreen<CausalMachineBlockController> {

    public CausalMachineBlockScreen(CausalMachineBlockController container, PlayerEntity player) {
        super(container, player);
    }
}
