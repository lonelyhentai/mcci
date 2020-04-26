package com.evernightfireworks.mcci.services.parser;

import com.evernightfireworks.mcci.services.core.CNode;
import com.evernightfireworks.mcci.services.core.CNodeType;
import com.evernightfireworks.mcci.services.core.CraftingManager;
import com.evernightfireworks.mcci.services.util.FourConsumer;
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
import java.util.HashMap;

public class LootParser {
    private static Logger logger = LogManager.getFormatterLogger("mcci:services:parser:loot");
    CraftingManager manager;

    private static Object reflectAccessField(Object object, String fieldName, Class targetClass) {
        try {
            Field targetField = targetClass.getDeclaredField(fieldName);
            targetField.setAccessible(true);
            return targetField.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LootParser.logger.error("unexpected field reflect error: " + e.getLocalizedMessage());
            return null;
        }
    }

    public LootParser(CraftingManager manager) {
        this.manager = manager;
    }

    public void parseLoot(LootManager lootManager) {
        var ids = lootManager.getSupplierNames();
        for (Identifier id : ids) {
            CNode lootNode = this.manager.getOrCreateNode(id, CNodeType.loot);
            LootTable lootTable = lootManager.getSupplier(id);
            var pools = (LootPool[]) LootParser.reflectAccessField(lootTable, "pools", lootTable.getClass());
            if(pools==null) {
                break;
            }
            for (LootPool pool : pools) {
                var entries = (LootEntry[]) LootParser.reflectAccessField(pool, "entries", pool.getClass());
                if(entries==null) {
                    break;
                }
                for (LootEntry lootEntry: entries) {
                    this.parseEntry(lootEntry, lootNode, lootTable);
                }
            }
        }
    }

    void parseItemEntry(ItemEntry entry, CNode lootNode, LootTable lootTable) {
        Item item = (Item) LootParser.reflectAccessField(entry, "item", entry.getClass());
        CNode node = this.manager.getOrCreateNode(Registry.ITEM.getId(item), CNodeType.item);
        this.manager.createSingleLink(node, lootNode, lootTable, lootNode.id);
    }

    void parseEmptyEntry(EmptyEntry entry, CNode lootNode, LootTable lootTable) {
    }

    void parseTagEntry(TagEntry entry, CNode lootNode, LootTable lootTable) {
        Tag<Item> name = (Tag<Item>) LootParser.reflectAccessField(entry, "name", entry.getClass());
        CNode node = this.manager.getOrCreateNode(name.getId(), CNodeType.tag);
        this.manager.createSingleLink(node, lootNode, lootTable, lootNode.id);
    }

    void parseLootTableEntry(LootTableEntry entry, CNode lootNode, LootTable lootTable) {
        Identifier id = (Identifier) LootParser.reflectAccessField(entry, "id", entry.getClass());
        CNode node = this.manager.getOrCreateNode(id, CNodeType.loot);
        this.manager.createSingleLink(node, lootNode, lootTable, lootNode.id);
    }

    // @TODO
    void parseDynamicEntry(DynamicEntry entry, CNode lootNode, LootTable lootTable) {
    }

    void parseCombinedEntry(CombinedEntry entry, CNode lootNode, LootTable lootTable) {
        LootEntry[] children = (LootEntry[]) LootParser.reflectAccessField(entry, "children", entry.getClass().getSuperclass());
        if(children==null) {
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
        if(r instanceof ItemEntry) {
            parseItemEntry((ItemEntry) r, n, l);
        } else if(r instanceof EmptyEntry) {
            parseEmptyEntry((EmptyEntry) r, n, l);
        } else if(r instanceof TagEntry) {
            parseTagEntry((TagEntry) r, n, l);
        } else if(r instanceof LootTableEntry) {
            parseLootTableEntry((LootTableEntry) r, n, l);
        } else if(r instanceof DynamicEntry) {
            parseDynamicEntry((DynamicEntry) r, n, l);
        } else if(r instanceof GroupEntry) {
            parseGroupEntry((GroupEntry) r, n, l);
        } else if(r instanceof AlternativeEntry) {
            parseAlternativeEntry((AlternativeEntry) r, n, l);
        } else if(r instanceof SequenceEntry) {
            parseSequenceEntry((SequenceEntry) r, n, l);
        }
    }
}
