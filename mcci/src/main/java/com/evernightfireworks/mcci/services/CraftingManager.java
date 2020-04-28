package com.evernightfireworks.mcci.services;

import com.evernightfireworks.mcci.services.core.CGraph;
import com.evernightfireworks.mcci.services.core.CLinkType;
import com.evernightfireworks.mcci.services.core.CNode;
import com.evernightfireworks.mcci.services.core.CNodeType;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.IOException;
import java.util.ArrayList;

public class CraftingManager {
    public CGraph global;

    public CraftingManager() {
        this.global = new CGraph();
    }

    public void createBasicNodes() {
        for(Item item:Registry.ITEM) {
            this.getOrCreateGlobalNode(Registry.ITEM.getId(item), CNodeType.item);
        }
        for(Block block:Registry.BLOCK) {
            this.getOrCreateGlobalNode(Registry.BLOCK.getId(block), CNodeType.block);
        }
        for(EntityType<?> entityType: Registry.ENTITY_TYPE) {
            this.getOrCreateGlobalNode(Registry.ENTITY_TYPE.getId(entityType), CNodeType.entity);
        }
        for(Fluid fluid: Registry.FLUID) {
            this.getOrCreateGlobalNode(Registry.FLUID.getId(fluid), CNodeType.fluid);
        }
    }

    public CNode getOrCreateGlobalNode(Identifier id, CNodeType kind) {
        return this.global.getOrCreateNode(id, kind);
    }

    public void createGlobalSingleLink(CNode outvert, CNode invert, String crafting, Identifier craftingId, CLinkType kind) {
        this.global.createSingleLink(outvert, invert, crafting, craftingId, kind);
    }

    public void completeGlobalRemainLinks() {
        var nodes = new ArrayList<>(this.global.nodes.values());
        for (var n : nodes) {
            if (n.kind == CNodeType.item) {
                this.global.linkItemBlock(n);
            }
        }
        nodes = new ArrayList<>(this.global.nodes.values());
        for (var n : nodes) {
            if (n.kind == CNodeType.block) {
                this.global.linkBlockLoot(n);
            } else if (n.kind == CNodeType.entity) {
                this.global.linkEntityLoot(n);
            }
        }
    }

    public CGraph getSubgraphOfGlobal(Item item) {
        return this.global.getSubgraph(item);
    }
}
