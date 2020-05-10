package com.evernightfireworks.mcci.gui.screens;

import ca.weblite.webview.WebView;
import com.evernightfireworks.mcci.services.CausalService;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.net.URL;

public class CausalMachineScreen extends LightweightGuiDescription {
    private final Logger logger = LogManager.getFormatterLogger(CausalMachineScreen.class.getName());

    public CausalMachineScreen(PlayerEntity player) {
        WGridPanel root = new WGridPanel(8);
        setRootPanel(root);
        root.setSize(64, 64);
        WSprite logo = new WSprite(new Identifier("mcci:icon-long.png"));
        root.add(logo, 1, 1, 12, 9);
        WLabel titleLabel = new WLabel(new TranslatableText("item.mcci.causal_machine"), titleColor);
        root.add(titleLabel, 2, 11, 6, 2);
        WButton button = new WButton(new TranslatableText("gui.mcci.causal_machine.analyse_button"));
        root.add(button, 9, 10, 4, 2);
        button.setOnClick(CausalService::viewRoot);
        root.validate(this);
    }
}
