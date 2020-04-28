package com.evernightfireworks.mcci.gui.screens;

import com.evernightfireworks.mcci.gui.controllers.CraftingPolicyMachineController;
import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.minecraft.entity.player.PlayerEntity;

public class CraftingPolicyMachineScreen extends CottonInventoryScreen<CraftingPolicyMachineController> {

    public CraftingPolicyMachineScreen(CraftingPolicyMachineController container, PlayerEntity player) {
        super(container, player);
    }
}
