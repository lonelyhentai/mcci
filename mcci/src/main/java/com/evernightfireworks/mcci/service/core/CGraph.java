package com.evernightfireworks.mcci.service.core;

import com.evernightfireworks.mcci.service.dto.CCategoryDto;
import com.evernightfireworks.mcci.service.dto.CGraphDto;
import com.evernightfireworks.mcci.service.dto.CLinkDto;
import com.evernightfireworks.mcci.service.dto.CNodeDto;
import com.google.gson.Gson;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.*;
import java.util.stream.Collectors;

public class CGraph {
    public Item target;
    public HashMap<String, CNode> nodes;
    public HashMap<String, ArrayList<CLink>> links;
    public HashMap<String, Integer> priority;
    public int maxPriority;

    static class Counter {
        public int count = 0;
        public HashMap<String,Integer> mapping = new HashMap<>();
    }

    public CGraph() {
        target = null;
        this.nodes = new HashMap<>();
        this.links = new HashMap<>();
        this.priority = new HashMap<>();
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

    public void createSingleLink(CNode endVert, CNode startVert, String crafting, Identifier craftingId, CLinkType kind) {
        var link = new CLink(startVert, endVert, crafting, craftingId, kind);
        this.insertLink(link);
    }

    public void createBinaryLinks(CNode nodeA, CNode nodeB, String crafting, Identifier craftingId, CLinkType kind) {
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
        this.insertNode(node, 0);
    }

    public void insertNode(CNode node, int priority) {
        this.maxPriority = Math.max(priority, this.maxPriority);
        this.priority.put(node.toKey(), priority);
        this.nodes.put(node.toKey(), node);
    }

    public void linkItemBlock(CNode n) {
        Item item = Registry.ITEM.get(n.id);
        if (item instanceof BlockItem) {
            Block block = ((BlockItem) item).getBlock();
            CNode blockNode = this.getOrCreateNode(Registry.BLOCK.getId(block), CNodeType.block);
            this.createSingleLink(blockNode, n, "", n.id, CLinkType.item_block);
        }
    }

    public void linkBlockLoot(CNode n) {
        Block block = Registry.BLOCK.get(n.id);
        var lootTableId = block.getDropTableId();
        if (lootTableId != null) {
            CNode lootNode = this.getOrCreateNode(lootTableId, CNodeType.loot);
            this.createSingleLink(lootNode, n, "", n.id, CLinkType.block_loot);
        }
    }

    public void linkEntityLoot(CNode n) {
        EntityType<?> entity = Registry.ENTITY_TYPE.get(n.id);
        var lootTableId = entity.getLootTableId();
        if (lootTableId != null) {
            CNode lootNode = this.getOrCreateNode(lootTableId, CNodeType.loot);
            this.createSingleLink(lootNode, n, "", n.id, CLinkType.entity_loot);
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

    public void copyInsertNode(CNode anotherNode, int priority) {
        this.insertNode(new CNode(anotherNode.id, anotherNode.kind), priority);
    }

    public void copyInsertLinks(CGraph another, CNode anotherStartVert, CNode anotherEndVert) {
        CNode thisStartVert = this.getNode(anotherStartVert);
        CNode thisEndVert = this.getNode(anotherEndVert);
        ArrayList<CLink> links = another.getLinks(anotherStartVert, anotherEndVert).stream()
                .map(l -> new CLink(thisStartVert, thisEndVert, l.info, l.infoId, l.kind)).collect(Collectors.toCollection(ArrayList::new));
        this.links.put(CLink.buildKey(thisStartVert, thisEndVert), links);
        thisStartVert.outlinks.addAll(links);
        thisEndVert.inlinks.addAll(links);
    }

    public int getPriority(CNode node) {
        return this.priority.get(node.toKey());
    }

    public CGraph getSubgraph(Item item) {
        Identifier itemId = Registry.ITEM.getId(item);
        CNode start = this.getNode(itemId, CNodeType.item);
        CGraph subgraph = new CGraph();
        if (start == null) {
            return subgraph;
        }
        Deque<CNode> deque = new ArrayDeque<>();
        subgraph.target = item;
        deque.addLast(start);
        subgraph.copyInsertNode(start, 0);
        while (!deque.isEmpty()) {
            CNode current = deque.pollFirst();
            int currentPriority = subgraph.getPriority(current);
            for (CLink link : current.inlinks) {
                CNode linkedNode = link.startVert;
                if (subgraph.getNode(linkedNode) == null) {
                    deque.addLast(linkedNode);
                    subgraph.copyInsertNode(linkedNode, currentPriority+1);
                    subgraph.copyInsertLinks(this, linkedNode, current);
                }
            }
        }
        return subgraph;
    }

    public String toString() {
        var nodeCounter = new Counter();
        ArrayList<CNodeDto> nodes = this.nodes.values().stream()
                .map((n) -> {
                    String translation = n.getTranslatableName();
                    nodeCounter.mapping.put(n.toKey(), nodeCounter.count);
                    var res = new CNodeDto(nodeCounter.count, translation, n.kind.ordinal(),
                            (int)Math.pow((double) 1.7, (double) this.maxPriority - this.getPriority(n)));
                    nodeCounter.count += 1;
                    return res;
                })
                .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<CLinkDto> links = this.links.values().stream()
                .flatMap((listOfLink) -> listOfLink
                        .stream()
                        .map(l ->
                                new CLinkDto(
                                        nodeCounter.mapping.get(l.startVert.toKey()),
                                        nodeCounter.mapping.get(l.endVert.toKey()),
                                        l.info, l.infoId.toString(), l.kind.ordinal()
                                )
                        ))
                .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<CCategoryDto> categories =
                Arrays.stream(CNodeType.values())
                        .map(t -> new CCategoryDto(CNode.getTypeTranslatableName(t)))
                        .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<CCategoryDto> linkCategories =
                Arrays.stream(CLinkType.values())
                        .map(t -> new CCategoryDto(CLink.getTypeTranslatableName(t)))
                        .collect(Collectors.toCollection(ArrayList::new));
        String targetName = new TranslatableText(this.target.getTranslationKey()).asString();
        String title =  new TranslatableText("service.mcci.core.cgraph.crafting_graph").asString();
        CGraphDto self = new CGraphDto(
                title,
                targetName,
                nodes, links,
                categories, linkCategories
        );
        Gson gson = new Gson();
        return gson.toJson(self);
    }
}
