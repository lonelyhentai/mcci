package com.evernightfireworks.mcci.service.parser;

import com.evernightfireworks.mcci.service.core.CLinkType;
import com.evernightfireworks.mcci.service.core.CNode;
import com.evernightfireworks.mcci.service.core.CNodeType;
import com.evernightfireworks.mcci.service.core.CraftingManager;
import net.minecraft.item.Item;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.*;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Arrays;

public class LootParser {
    private final Logger logger = LogManager.getFormatterLogger("mcci:services:parser:loot");
    CraftingManager manager;

    private Object reflectAccessField(
            Object object, String fieldName,
            @SuppressWarnings("rawtypes") Class targetClass
    ) {
        try {
            Field targetField = targetClass.getDeclaredField(fieldName);
            targetField.setAccessible(true);
            return targetField.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            this.logger.error("unexpected field reflect error: " + e.getLocalizedMessage());
            return null;
        }
    }

    public LootParser(CraftingManager manager) {
        this.manager = manager;
    }

    public void parseLoot(LootManager lootManager) {
        var ids = lootManager.getSupplierNames();
        for (Identifier id : ids) {
            CNode lootNode = this.manager.getOrCreateGlobalNode(id, CNodeType.loot);
            LootTable lootTable = lootManager.getSupplier(id);
            var pools = (LootPool[])this.reflectAccessField(lootTable, "pools", lootTable.getClass());
            if (pools == null) {
                break;
            }
            for (LootPool pool : pools) {
                var entries = (LootEntry[])this.reflectAccessField(pool, "entries", pool.getClass());
                if (entries == null) {
                    break;
                }
                for (LootEntry lootEntry : entries) {
                    this.parseEntry(lootEntry, lootNode, lootTable);
                }
            }
        }
    }

    void parseItemEntry(ItemEntry entry, CNode lootNode, LootTable lootTable) {
        Item item = (Item)this.reflectAccessField(entry, "item", entry.getClass());
        CNode node = this.manager.getOrCreateGlobalNode(Registry.ITEM.getId(item), CNodeType.item);
        this.manager.createGlobalSingleLink(node, lootNode, lootTable, lootNode.id, CLinkType.loot_table);
    }

    void parseEmptyEntry(
            @SuppressWarnings("unused") EmptyEntry entry,
            @SuppressWarnings("unused") CNode lootNode,
            @SuppressWarnings("unused") LootTable lootTable) {
    }

    void parseTagEntry(TagEntry entry, CNode lootNode, LootTable lootTable) {
        try {
            @SuppressWarnings("unchecked")
            Tag<Item> name = (Tag<Item>) this.reflectAccessField(entry, "name", entry.getClass());
            if(name==null) {
                this.logger.warn("failed to access tag entry name, skipped");
                return;
            }
            CNode node = this.manager.getOrCreateGlobalNode(name.getId(), CNodeType.tag);
            this.manager.createGlobalSingleLink(node, lootNode, lootTable, lootNode.id, CLinkType.loot_table);
        } catch (ClassCastException e) {
            this.logger.warn("failed to cast from tag entry's name to Tag<Item>, skipped");
        }
    }

    void parseLootTableEntry(LootTableEntry entry, CNode lootNode, LootTable lootTable) {
        Identifier id = (Identifier)this.reflectAccessField(entry, "id", entry.getClass());
        CNode node = this.manager.getOrCreateGlobalNode(id, CNodeType.loot);
        this.manager.createGlobalSingleLink(node, lootNode, lootTable, lootNode.id, CLinkType.loot_table);
    }

    // @TODO
    void parseDynamicEntry(
            @SuppressWarnings("unused") DynamicEntry entry,
            @SuppressWarnings("unused") CNode lootNode,
            @SuppressWarnings("unused") LootTable lootTable) {
    }

    void parseCombinedEntry(CombinedEntry entry, CNode lootNode, LootTable lootTable) {
        LootEntry[] children = (LootEntry[]) this.reflectAccessField(entry, "children", entry.getClass().getSuperclass());
        if (children == null) {
            return;
        }
        Arrays.stream(children).forEach(c -> this.parseEntry(c, lootNode, lootTable));
    }

    void parseAlternativeEntry(AlternativeEntry entry, CNode lootNode, LootTable lootTable) {
        this.parseCombinedEntry(entry, lootNode, lootTable);
    }

    void parseGroupEntry(GroupEntry entry, CNode lootNode, LootTable lootTable) {
        this.parseCombinedEntry(entry, lootNode, lootTable);
    }

    void parseSequenceEntry(SequenceEntry entry, CNode lootNode, LootTable lootTable) {
        this.parseCombinedEntry(entry, lootNode, lootTable);
    }

    void parseEntry(LootEntry r, CNode n, LootTable l) {
        if (r instanceof ItemEntry) {
            parseItemEntry((ItemEntry) r, n, l);
        } else if (r instanceof EmptyEntry) {
            parseEmptyEntry((EmptyEntry) r, n, l);
        } else if (r instanceof TagEntry) {
            parseTagEntry((TagEntry) r, n, l);
        } else if (r instanceof LootTableEntry) {
            parseLootTableEntry((LootTableEntry) r, n, l);
        } else if (r instanceof DynamicEntry) {
            parseDynamicEntry((DynamicEntry) r, n, l);
        } else if (r instanceof GroupEntry) {
            parseGroupEntry((GroupEntry) r, n, l);
        } else if (r instanceof AlternativeEntry) {
            parseAlternativeEntry((AlternativeEntry) r, n, l);
        } else if (r instanceof SequenceEntry) {
            parseSequenceEntry((SequenceEntry) r, n, l);
        }
    }
}
