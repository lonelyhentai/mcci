package com.evernightfireworks.mcci.services.parser;

import com.evernightfireworks.mcci.services.core.CNode;
import com.evernightfireworks.mcci.services.core.CNodeType;
import com.evernightfireworks.mcci.services.core.CraftingManager;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tag.RegistryTagManager;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class TagParser {
    CraftingManager manager;
    public TagParser(CraftingManager manager) {
        this.manager = manager;
    }

    public void parseTags(RegistryTagManager tags) {
        tags.items().getEntries().forEach(this::parseItemTag);
        tags.fluids().getEntries().forEach(this::parseFluidTag);
        tags.blocks().getEntries().forEach(this::parseBlockTag);
        tags.entityTypes().getEntries().forEach(this::parseEntityTypeTag);
    }

    void parseItemTag(Identifier identifier, Tag<Item> tag) {
        CNode tagNode = this.manager.getOrCreateNode(identifier, CNodeType.tag);
        for(Item v: tag.values()) {
            CNode itemNode = this.manager.getOrCreateNode(Registry.ITEM.getId(v), CNodeType.item);
            this.manager.createBinaryLinks(tagNode, itemNode, tag, identifier);
        }
    }

    void parseFluidTag(Identifier identifier, Tag<Fluid> tag) {
        CNode tagNode = this.manager.getOrCreateNode(identifier, CNodeType.tag);
        for(Fluid v: tag.values()) {
            CNode fluidNode = this.manager.getOrCreateNode(Registry.FLUID.getId(v), CNodeType.fluid);
            this.manager.createBinaryLinks(tagNode, fluidNode, tag, identifier);
        }
    }

    void parseBlockTag(Identifier identifier, Tag<Block> tag) {
        CNode tagNode = this.manager.getOrCreateNode(identifier, CNodeType.tag);
        for(Block v: tag.values()) {
            CNode blockTag = this.manager.getOrCreateNode(Registry.BLOCK.getId(v), CNodeType.block);
            this.manager.createBinaryLinks(tagNode, blockTag, tag, identifier);
        }
    }

    void parseEntityTypeTag(Identifier identifier, Tag<EntityType<?>> tag) {
        CNode tagNode = this.manager.getOrCreateNode(identifier, CNodeType.tag);
        for(EntityType<?> v: tag.values()) {
            CNode blockTag = this.manager.getOrCreateNode(Registry.ENTITY_TYPE.getId(v), CNodeType.entity);
            this.manager.createBinaryLinks(tagNode, blockTag, tag, identifier);
        }
    }
}
