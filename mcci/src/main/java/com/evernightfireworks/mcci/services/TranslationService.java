package com.evernightfireworks.mcci.services;

import com.evernightfireworks.mcci.services.core.CNode;
import net.minecraft.text.TranslatableText;

import java.io.IOException;
import java.util.stream.Collectors;

public class TranslationService {
    public static void saveLootNodeKeys(CraftingManager manager) throws IOException {
        String content = String.format("[%s]",manager.global.nodes.values().stream()
                .map(n->String.format("\"%s\"", n.toKey()))
                .collect(Collectors.joining(",")));
        FileSystemManager.writeRuntimeResource("temp/nodes.json", content);
    }

    public static String translate(CNode node) {
        String translationKey = String.format("service.mcci.translation_service.%s", node.toKey().replaceAll("[@:/]", "_"));
        return new TranslatableText(translationKey).asString();
    }
}
