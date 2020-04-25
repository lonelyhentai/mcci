package com.evernightfireworks.mcci.services.core;

import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;

public class CraftingManager {
    public HashMap<String, CNode> nodes;
    public HashMap<String, ArrayList<CLink>> links;

    public CraftingManager() {
        this.nodes = new HashMap<>();
        this.links = new HashMap<>();
    }

    public CNode getOrCreateNode(Identifier id, CNodeType kind) {
        var key = CNode.buildKey(id, kind);
        var maybeNode = this.nodes.get(key);
        if(maybeNode==null) {
            var newNode = new CNode(id, kind);
            this.nodes.put(key, newNode);
            return newNode;
        } else {
            return maybeNode;
        }
    }

    public void createSingleLink(CNode invert, CNode outvert, Object crafting, Identifier craftingId) {
        var link = new CLink(invert, outvert, crafting, craftingId);
        this.insertLink(link);
    }

    public void createBinaryLinks(CNode nodeA, CNode nodeB, Object crafting, Identifier craftingId) {
        this.createSingleLink(nodeA, nodeB, crafting, craftingId);
        this.createSingleLink(nodeB, nodeA, crafting, craftingId);
    }

    public void insertLink(CLink link) {
        link.invert.outlinks.add(link);
        link.outvert.inlinks.add(link);
        var key = link.toKey();
        if(this.links.containsKey(key)) {
            this.links.get(key).add(link);
        } else {
            var newList = new ArrayList<CLink>();
            newList.add(link);
            this.links.put(key, newList);
        }
    }
}
