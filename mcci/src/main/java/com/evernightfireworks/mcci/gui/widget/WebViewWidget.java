package com.evernightfireworks.mcci.gui.widget;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

public class WebViewWidget extends WWidget {
    public WebViewWidget() {
        super();
    }

    @Override
    public boolean canResize() {
        return true;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void paintBackground(int x, int y, int mouseX, int mouseY) {
        ScreenDrawing.drawGuiPanel(x, y, this.getWidth() , this.getHeight());
        MinecraftClient client = MinecraftClient.getInstance();

    }
}
