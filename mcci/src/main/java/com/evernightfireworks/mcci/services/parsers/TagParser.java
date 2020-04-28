package com.evernightfireworks.mcci.services.parsers;

import com.evernightfireworks.mcci.services.core.CLinkType;
import com.evernightfireworks.mcci.services.core.CNode;
import com.evernightfireworks.mcci.services.core.CNodeType;
import com.evernightfireworks.mcci.services.CraftingManager;
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
        CNode tagNode = this.manager.getOrCreateGlobalNode(identifier, CNodeType.tag);
        for(Item v: tag.values()) {
            CNode itemNode = this.manager.getOrCreateGlobalNode(Registry.ITEM.getId(v), CNodeType.item);
            this.manager.createGlobalSingleLink(itemNode, tagNode, tag.toString(), identifier, CLinkType.tag);
        }
    }

    void parseFluidTag(Identifier identifier, Tag<Fluid> tag) {
        CNode tagNode = this.manager.getOrCreateGlobalNode(identifier, CNodeType.tag);
        for(Fluid v: tag.values()) {
            CNode fluidNode = this.manager.getOrCreateGlobalNode(Registry.FLUID.getId(v), CNodeType.fluid);
            this.manager.createGlobalSingleLink(fluidNode, tagNode, tag.toString(), identifier, CLinkType.tag);
        }
    }

    void parseBlockTag(Identifier identifier, Tag<Block> tag) {
        CNode tagNode = this.manager.getOrCreateGlobalNode(identifier, CNodeType.tag);
        for(Block v: tag.values()) {
            CNode blockTag = this.manager.getOrCreateGlobalNode(Registry.BLOCK.getId(v), CNodeType.block);
            this.manager.createGlobalSingleLink(blockTag, tagNode, tag.toString(), identifier, CLinkType.tag);
        }
    }

    void parseEntityTypeTag(Identifier identifier, Tag<EntityType<?>> tag) {
        CNode tagNode = this.manager.getOrCreateGlobalNode(identifier, CNodeType.tag);
        for(EntityType<?> v: tag.values()) {
            CNode entityTag = this.manager.getOrCreateGlobalNode(Registry.ENTITY_TYPE.getId(v), CNodeType.entity);
            this.manager.createGlobalSingleLink(entityTag, tagNode, tag.toString(), identifier, CLinkType.tag);
        }
    }
}
