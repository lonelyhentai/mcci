package com.evernightfireworks.mcci.gui.controllers;


import ca.weblite.webview.WebView;
import com.evernightfireworks.mcci.CausalEngine;
import com.evernightfireworks.mcci.services.CraftingPolicyService;
import com.evernightfireworks.mcci.services.core.CGraph;
import com.evernightfireworks.mcci.services.interfaces.CGraphContainer;
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

import java.net.MalformedURLException;
import java.net.URL;

public class CraftingPolicyMachineController extends CottonCraftingController {

    private final Logger logger = LogManager.getFormatterLogger(CraftingPolicyMachineController.class.getName());

    public CraftingPolicyMachineController(int syncId, PlayerInventory playerInventory, BlockContext context) {
        super(RecipeType.CRAFTING, syncId, playerInventory, getBlockInventory(context), getBlockPropertyDelegate(context));

        WGridPanel root = new WGridPanel(16);
        setRootPanel(root);
        root.setSize(256, 128);

        WItemSlot itemSlot = WItemSlot.of(blockInventory, 0);
        root.add(itemSlot, 2, 2);

        WButton generateButton = new WButton(new TranslatableText("gui.mcci.causal_machine.generation_button"));
        generateButton.setOnClick(() -> {
            if(this.playerInventory.player.world.isClient()) {
                this.logger.info("clicked generation button");
                if(!this.blockInventory.isInvEmpty()) {
                    try {
                        URL url = CraftingPolicyService.getWebViewGraphURL();
                        new Thread(() -> {
                            WebView webview = new WebView();
                            webview.url(url.toString());
                            webview.title("Crafting Graph");
                            webview.resizable(true);
                            webview.show();
                        }).start();
                    } catch (MalformedURLException e) {
                        this.logger.error("failed to open webview", e);
                    }
                }
            }
        });
        root.add(generateButton, 1, 4, 3, 1);
        root.add(this.createPlayerInventoryPanel(), 5, 1);
        root.validate(this);
    }
}
