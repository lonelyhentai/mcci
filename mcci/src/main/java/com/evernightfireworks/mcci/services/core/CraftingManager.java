package com.evernightfireworks.mcci.services.core;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import java.util.ArrayList;

public class CraftingManager {
    public CGraph global;

    public CraftingManager() {
        this.global = new CGraph();
    }

    public CNode getOrCreateGlobalNode(Identifier id, CNodeType kind) {
        return this.global.getOrCreateNode(id, kind);
    }

    public void createGlobalSingleLink(CNode outvert, CNode invert, Object crafting, Identifier craftingId, CLinkType kind) {
        this.global.createSingleLink(outvert, invert, crafting, craftingId, kind);
    }

    public void createGlobalBinaryLinks(CNode nodeA, CNode nodeB, Object crafting, Identifier craftingId, CLinkType kind) {
        this.global.createBinaryLinks(nodeA, nodeB, crafting, craftingId, kind);
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
