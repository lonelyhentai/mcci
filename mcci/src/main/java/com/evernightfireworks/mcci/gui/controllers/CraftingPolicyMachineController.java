package com.evernightfireworks.mcci.gui.controllers;


import ca.weblite.webview.WebView;
import com.evernightfireworks.mcci.CausalEngine;
import com.evernightfireworks.mcci.services.CraftingPolicyService;
import com.evernightfireworks.mcci.services.core.CGraph;
import com.evernightfireworks.mcci.services.interfaces.CGraphContainer;
import io.github.cottonmc.cotton.gui.CottonCraftingController;
import io.github.cottonmc.cotton.gui.widget.*;
import net.minecraft.container.BlockContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.recipe.RecipeType;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;

public class CraftingPolicyMachineController extends CottonCraftingController {

    private final Logger logger = LogManager.getFormatterLogger(CraftingPolicyMachineController.class.getName());

    public CraftingPolicyMachineController(int syncId, PlayerInventory playerInventory, BlockContext context) {
        super(RecipeType.CRAFTING, syncId, playerInventory, getBlockInventory(context), getBlockPropertyDelegate(context));

        WGridPanel root = new WGridPanel(8);
        setRootPanel(root);
        root.setSize(168, 176);

        WSprite logo = new WSprite(new Identifier("mcci:icon-long.png"));
        root.add(logo, 1, 1, 12, 9);

        WLabel title = new WLabel(new TranslatableText("item.mcci.causal_machine"));
        root.add(title, 14, 3, 4, 2);

        WItemSlot itemSlot = WItemSlot.of(blockInventory, 0);
        root.add(itemSlot, 18, 2, 2, 2);

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
        root.add(generateButton, 14, 6, 7, 2);
        root.add(this.createPlayerInventoryPanel(), 1, 10);
        root.validate(this);
    }
}
