package com.evernightfireworks.mcci.services.managers;

import com.google.common.collect.Streams;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;

public class ShadowProductionManager {

    private final Logger logger = LogManager.getFormatterLogger(ShadowProductionManager.class.getName());
    private final PlayerEntity player;


    public ShadowProductionManager(PlayerEntity player) {
        this.player = player;
    }

    private int calculateToolsFactor() {
        var player = this.player;
        var inventory = player.inventory;
        ArrayList<Integer> toolFactors = new ArrayList<>();
        for(int i=0;i<inventory.getInvSize();i++) {
            ItemStack stack = inventory.getInvStack(i);
            Item item = stack.getItem();
            if(item instanceof ToolItem) {
                int currentFactor = 0;
                ToolMaterial material = ((ToolItem)item).getMaterial();
                if(material==ToolMaterials.DIAMOND) {
                    currentFactor += 5;
                }  else if(material==ToolMaterials.GOLD) {
                    currentFactor += 4;
                } else if(material==ToolMaterials.IRON) {
                    currentFactor += 3;
                } else if(material==ToolMaterials.STONE) {
                    currentFactor += 2;
                } else if(material==ToolMaterials.WOOD) {
                    currentFactor += 1;
                }
                var enchantments = stack.getEnchantments();
                currentFactor += Math.min(enchantments.size(), 3);
                toolFactors.add(currentFactor);
            }
        }
        toolFactors.sort((a,b)->b-a);
        double res = 0;
        double base = 5;
        for(var v: toolFactors) {
            res += (base * v);
            base /= 2;
        }
        return (int)Math.ceil(res);
    }

    private int calculateMaterialFactor() {
        var player = this.player;
        var inventory = player.inventory;
        int materialSize = 0;
        int materialKind = 0;
        for(int i=0;i<inventory.getInvSize();i++){
            ItemStack stack = inventory.getInvStack(i);
            Item item = stack.getItem();
            if(!(item instanceof ToolItem) && (item!=Items.AIR)) {
                materialSize+=stack.getCount();
                materialKind+=1;
            }
        }
        return Math.min(80, (int)Math.sqrt(materialKind*materialSize));
    }



    public int run() {
        int tf = this.calculateToolsFactor();
        int mf = this.calculateMaterialFactor();
        int productionFactor = (tf + mf) * 3 / 2 - (int)Math.sqrt(tf*mf);
        this.logger.info("production factor that combines tool factor and material is " + productionFactor);
        return productionFactor;
    }
}
