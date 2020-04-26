package com.evernightfireworks.mcci.services.core;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

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

    public void createSingleLink(CNode invert, CNode outvert, Object crafting, Identifier craftingId, CLinkType kind) {
        var link = new CLink(invert, outvert, crafting, craftingId, kind);
        this.insertLink(link);
    }

    public void createBinaryLinks(CNode nodeA, CNode nodeB, Object crafting, Identifier craftingId, CLinkType kind) {
        this.createSingleLink(nodeA, nodeB, crafting, craftingId, kind);
        this.createSingleLink(nodeB, nodeA, crafting, craftingId, kind);
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

    public void completeLinks() {
        var nodes = new ArrayList<>(this.nodes.values());
        for(var n: nodes) {
            if(n.kind==CNodeType.item) {
                this.linkItemBlock(n);
            }
        }
        nodes = new ArrayList<>(this.nodes.values());
        for(var n: nodes) {
            if(n.kind==CNodeType.block) {
                this.linkBlockLoot(n);
            }
            else if(n.kind==CNodeType.entity) {
                this.linkEntityLoot(n);
            }
        }
    }

    public void linkItemBlock(CNode n) {
        Item item = Registry.ITEM.get(n.id);
        if(item instanceof BlockItem) {
            Block block = ((BlockItem) item).getBlock();
            CNode blockNode = this.getOrCreateNode(Registry.BLOCK.getId(block), CNodeType.block);
            this.createSingleLink(blockNode, n, null, n.id, CLinkType.item_block);
        }
    }

    public void linkBlockLoot(CNode n) {
        Block block = Registry.BLOCK.get(n.id);
        var lootTableId = block.getDropTableId();
        if(lootTableId!=null) {
            CNode lootNode = this.getOrCreateNode(lootTableId, CNodeType.loot);
            this.createSingleLink(lootNode, n, null, n.id, CLinkType.block_loot);
        }
    }

    public void linkEntityLoot(CNode n) {
        EntityType<?> entity =  Registry.ENTITY_TYPE.get(n.id);
        var lootTableId = entity.getLootTableId();
        if(lootTableId!=null) {
            CNode lootNode = this.getOrCreateNode(lootTableId, CNodeType.loot);
            this.createSingleLink(lootNode, n, null, n.id, CLinkType.entity_loot);
        }
    }
}
