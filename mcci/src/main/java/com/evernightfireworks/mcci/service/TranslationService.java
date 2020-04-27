package com.evernightfireworks.mcci.service;

import com.evernightfireworks.mcci.service.core.CNode;
import com.evernightfireworks.mcci.service.core.CNodeType;
import com.evernightfireworks.mcci.service.core.CraftingManager;
import net.minecraft.text.TranslatableText;

import java.io.IOException;
import java.util.stream.Collectors;

public class TranslationService {
    public static void saveLootNodeKeys(CraftingManager manager) throws IOException {
        String content = String.format("[%s]",manager.global.nodes.values().stream()
                .filter(n->n.kind== CNodeType.loot)
                .map(n->String.format("\"%s\"", n.toKey()))
                .collect(Collectors.joining(",")));
        FileSystemManager.writeRuntimeResource("temp/loot.json", content);
    }

    public static void saveTagNodeKeys(CraftingManager manager) throws IOException {
        String content = String.format("[%s]",manager.global.nodes.values().stream()
                .filter(n->n.kind== CNodeType.tag)
                .map(n->String.format("\"%s\"", n.toKey()))
                .collect(Collectors.joining(",")));
        FileSystemManager.writeRuntimeResource("temp/tag.json", content);
    }

    public static String translate(CNode node) {
        String translationKey = String.format("service.mcci.translation_service.%s", node.toKey().replaceAll("[@:/]", "_"));
        return new TranslatableText(translationKey).asString();
    }
}
