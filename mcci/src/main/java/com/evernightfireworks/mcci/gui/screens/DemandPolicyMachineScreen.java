package com.evernightfireworks.mcci.gui.screens;

import com.evernightfireworks.mcci.services.DemandPoliceService;
import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WSprite;
import io.github.cottonmc.cotton.gui.widget.data.Alignment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class DemandPolicyMachineScreen extends LightweightGuiDescription {
    public DemandPolicyMachineScreen(PlayerEntity player) {
        WGridPanel root = new WGridPanel(8);
        setRootPanel(root);
        root.setSize(224, 112);
        // title
        WSprite logo = new WSprite(new Identifier("mcci:icon-long.png"));
        root.add(logo, 1, 1, 12, 9);
        WLabel titleLabel = new WLabel(new TranslatableText("item.mcci.demand_policy_machine"),titleColor);
        root.add(titleLabel, 2, 11, 6, 2);
        WButton button = new WButton(new TranslatableText("gui.mcci.demand_policy_machine.calculate_button"));
        root.add(button, 9, 10, 4, 2);
        // set hunger factor
        WSprite hungerIcon = new WSprite(new Identifier("minecraft:textures/item/bread.png"));
        root.add(hungerIcon, 15, 1, 2, 2);
        WLabel hungerLabel = new WLabel(new TranslatableText("gui.mcci.demand_policy_machine.hunger_factor"));
        hungerLabel.setAlignment(Alignment.RIGHT);
        root.add(hungerLabel, 18, 1, 6, 2);
        WLabel hungerText = new WLabel(new LiteralText("?"));
        root.add(hungerText, 25, 1, 2,2);
        // set safety factor
        WSprite safetyIcon = new WSprite(new Identifier("minecraft:textures/item/iron_chestplate.png"));
        root.add(safetyIcon, 15, 4, 2, 2);
        WLabel safetyLabel = new WLabel(new TranslatableText("gui.mcci.demand_policy_machine.safety_factor"));
        safetyLabel.setAlignment(Alignment.RIGHT);
        root.add(safetyLabel,  18, 4, 6, 2);

        WLabel safetyText = new WLabel(new LiteralText("?"));
        root.add(safetyText, 25, 4, 2, 2);
        // set production factor
        WSprite productionIcon = new WSprite(new Identifier("minecraft:textures/item/iron_pickaxe.png"));
        root.add(productionIcon, 15, 7, 2, 2);
        WLabel productionLabel = new WLabel(new TranslatableText("gui.mcci.demand_policy_machine.production_factor"));
        productionLabel.setAlignment(Alignment.RIGHT);
        root.add(productionLabel, 18, 7, 6, 2);
        WLabel productionText = new WLabel(new LiteralText("?"));
        root.add(productionText, 25, 7, 2, 2);
        // set development factor
        WSprite developmentIcon = new WSprite(new Identifier("minecraft:textures/item/book.png"));
        root.add(developmentIcon, 15, 10, 2, 2);
        WLabel developmentLabel = new WLabel(new TranslatableText("gui.mcci.demand_policy_machine.development_factor"));
        developmentLabel.setAlignment(Alignment.RIGHT);
        root.add(developmentLabel, 18, 10, 6, 2);
        WLabel developmentText = new WLabel(new LiteralText("?"));
        root.add(developmentText, 25, 10, 2, 2);
        // set risk factor
        WSprite riskIcon = new WSprite(new Identifier("minecraft:textures/item/blaze_powder.png"));
        root.add(riskIcon, 15, 13, 2, 2);
        WLabel riskLabel = new WLabel(new TranslatableText("gui.mcci.demand_policy_machine.risk_factor"));
        riskLabel.setAlignment(Alignment.RIGHT);
        root.add(riskLabel, 18, 13, 6, 2);
        WLabel riskText = new WLabel(new LiteralText("?"));
        root.add(riskText, 25, 13, 2,2);
        button.setOnClick(()->{
            // hunger value
            int hungerFactor = DemandPoliceService.calculateHungerFactor(player);
            hungerText.setText(new LiteralText(""+hungerFactor));
            // safety value
            int safetyFactor = DemandPoliceService.calculateSafetyFactor(player);
            safetyText.setText(new LiteralText(""+safetyFactor));
            // production value
            int productionFactor = DemandPoliceService.calculateProductionFactor(player);
            productionText.setText(new LiteralText(""+productionFactor));
            // development value
            int developmentFactor = DemandPoliceService.calculateDevelopmentFactor(player);
            developmentText.setText(new LiteralText(""+developmentFactor));
            // risk value
            int riskFactor = DemandPoliceService.calculateRiskFactor(hungerFactor, safetyFactor, productionFactor, developmentFactor);
            riskText.setText(new LiteralText("" + riskFactor));
        });
        root.validate(this);
    }

    @Override
    public void addPainters() {
        getRootPanel().setBackgroundPainter(BackgroundPainter.VANILLA);
    }
}
