package com.evernightfireworks.mcci.service.core;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.stream.Collectors;

public class CGraph {
    public HashMap<String, CNode> nodes;
    public HashMap<String, ArrayList<CLink>> links;

    public CGraph() {
        this.nodes = new HashMap<>();
        this.links = new HashMap<>();
    }

    public CNode getOrCreateNode(Identifier id, CNodeType kind) {
        CNode maybeNode = this.getNode(id, kind);
        if (maybeNode == null) {
            CNode newNode = new CNode(id, kind);
            this.insertNode(newNode);
            return newNode;
        }
        return maybeNode;
    }

    public void createSingleLink(CNode endVert, CNode startVert, Object crafting, Identifier craftingId, CLinkType kind) {
        var link = new CLink(startVert, endVert, crafting, craftingId, kind);
        this.insertLink(link);
    }

    public void createBinaryLinks(CNode nodeA, CNode nodeB, Object crafting, Identifier craftingId, CLinkType kind) {
        this.createSingleLink(nodeA, nodeB, crafting, craftingId, kind);
        this.createSingleLink(nodeB, nodeA, crafting, craftingId, kind);
    }

    public void insertLink(CLink link) {
        link.startVert.outlinks.add(link);
        link.endVert.inlinks.add(link);
        var key = link.toKey();
        if (this.links.containsKey(key)) {
            this.links.get(key).add(link);
        } else {
            var newList = new ArrayList<CLink>();
            newList.add(link);
            this.links.put(key, newList);
        }
    }

    public void insertNode(CNode node) {
        this.nodes.put(node.toKey(), node);
    }

    public void linkItemBlock(CNode n) {
        Item item = Registry.ITEM.get(n.id);
        if (item instanceof BlockItem) {
            Block block = ((BlockItem) item).getBlock();
            CNode blockNode = this.getOrCreateNode(Registry.BLOCK.getId(block), CNodeType.block);
            this.createSingleLink(blockNode, n, null, n.id, CLinkType.item_block);
        }
    }

    public void linkBlockLoot(CNode n) {
        Block block = Registry.BLOCK.get(n.id);
        var lootTableId = block.getDropTableId();
        if (lootTableId != null) {
            CNode lootNode = this.getOrCreateNode(lootTableId, CNodeType.loot);
            this.createSingleLink(lootNode, n, null, n.id, CLinkType.block_loot);
        }
    }

    public void linkEntityLoot(CNode n) {
        EntityType<?> entity = Registry.ENTITY_TYPE.get(n.id);
        var lootTableId = entity.getLootTableId();
        if (lootTableId != null) {
            CNode lootNode = this.getOrCreateNode(lootTableId, CNodeType.loot);
            this.createSingleLink(lootNode, n, null, n.id, CLinkType.entity_loot);
        }
    }

    public CNode getNode(CNode anotherNode) {
        return this.nodes.get(anotherNode.toKey());
    }

    public CNode getNode(Identifier id, CNodeType kind) {
        return this.nodes.get(CNode.buildKey(id, kind));
    }

    public ArrayList<CLink> getLinks(CLink anotherLink) {
        return this.links.get(anotherLink.toKey());
    }

    public ArrayList<CLink> getLinks(CNode anotherStartVert, CNode anotherEndVert) {
        return this.links.get(CLink.buildKey(anotherStartVert, anotherEndVert));
    }

    public void copyInsertNode(CNode anotherNode) {
        this.insertNode(new CNode(anotherNode.id, anotherNode.kind));
    }

    public void copyInsertLinks(CGraph another, CNode anotherStartVert, CNode anotherEndVert) {
        CNode thisStartVert = this.getNode(anotherStartVert);
        CNode thisEndVert = this.getNode(anotherEndVert);
        ArrayList<CLink> links = another.getLinks(anotherStartVert, anotherEndVert).stream()
                .map(l-> new CLink(thisStartVert, thisEndVert, l.info, l.infoId, l.kind)).collect(Collectors.toCollection(ArrayList::new));
        this.links.put(CLink.buildKey(thisStartVert, thisEndVert), links);
        thisStartVert.outlinks.addAll(links);
        thisEndVert.inlinks.addAll(links);
    }

    public CGraph getSubgraph(Item item) {
        Identifier itemId = Registry.ITEM.getId(item);
        CNode start = this.getNode(itemId, CNodeType.item);
        if (start == null) {
            return null;
        }
        Deque<CNode> deque = new ArrayDeque<>();
        CGraph subgraph = new CGraph();
        deque.addLast(start);
        subgraph.copyInsertNode(start);
        while(!deque.isEmpty()) {
            CNode current = deque.pollFirst();
            for(CLink link: current.inlinks) {
                CNode linkedNode = link.startVert;
                if(subgraph.getNode(linkedNode)==null) {
                    deque.addLast(linkedNode);
                    subgraph.copyInsertNode(linkedNode);
                    subgraph.copyInsertLinks(this, linkedNode, current);
                }
            }
        }
        return subgraph;
    }
}
