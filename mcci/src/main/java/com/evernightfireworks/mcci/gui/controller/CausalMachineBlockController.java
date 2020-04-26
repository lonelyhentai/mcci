package com.evernightfireworks.mcci.gui.controller;


import com.evernightfireworks.mcci.gui.widget.WebViewWidget;
import io.github.cottonmc.cotton.gui.CottonCraftingController;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import net.minecraft.container.BlockContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.recipe.RecipeType;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CausalMachineBlockController extends CottonCraftingController {

    private final Logger logger = LogManager.getFormatterLogger("mcci:gui:causal_machine_controller");

    public CausalMachineBlockController(int syncId, PlayerInventory playerInventory, BlockContext context) {
        super(RecipeType.CRAFTING, syncId, playerInventory, getBlockInventory(context), getBlockPropertyDelegate(context));

        WGridPanel root = new WGridPanel(16);
        setRootPanel(root);
        root.setSize(320, 320);

        WItemSlot itemSlot = WItemSlot.of(blockInventory, 0);
        root.add(itemSlot, 3, 2);

        WButton generateButton = new WButton(new TranslatableText("gui.mcci.causal_machine.generation_button"));
        generateButton.setOnClick(()->{
            this.logger.info("clicked generation button");
        });
        root.add(generateButton, 1, 4, 5, 1);
        root.add(this.createPlayerInventoryPanel(), 9, 1);
        var canvas = new WebViewWidget();
        root.add(canvas, 1, 7, 18, 12);
        root.validate(this);
    }
}
