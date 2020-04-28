package com.evernightfireworks.mcci.services.core;

import com.evernightfireworks.mcci.services.TranslationService;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import java.util.ArrayList;

public class CNode {
    public Identifier id;
    public CNodeType kind;
    public ArrayList<CLink> inlinks;
    public ArrayList<CLink> outlinks;
    private static final String CNODETYPE_TRANSLATION_PREFIX = "service.mcci.core.cnode_type.";

    public CNode(Identifier id, CNodeType kind) {
        this.id = id;
        this.kind = kind;
        this.inlinks = new ArrayList<>();
        this.outlinks = new ArrayList<>();
    }

    public String toKey() {
        return CNode.buildKey(this.id, this.kind);
    }

    public static CNode fromKey(String key) {
        var pair = CNode.parseKey(key);
        return new CNode(pair.getKey(), pair.getValue());
    }

    public static String buildKey(Identifier id, CNodeType kind) {
        return String.format("%s@%s", kind.toString(), id.toString());
    }

    public static Pair<Identifier, CNodeType> parseKey(String key) {
        var parseEntries = key.split("@");
        String idStr = parseEntries[1];
        String kindStr = parseEntries[0];
        Identifier id = new Identifier(idStr);
        CNodeType kind = CNodeType.valueOf(kindStr);
        return new ImmutablePair<>(id, kind);
    }

    public String getTranslatableName() {
        if(this.kind==CNodeType.block) {
            return new TranslatableText(Registry.BLOCK.get(this.id).getTranslationKey()).asString();
        } else if(this.kind==CNodeType.item) {
            return new TranslatableText(Registry.ITEM.get(this.id).getTranslationKey()).asString();
        } else if(this.kind==CNodeType.entity) {
            return new TranslatableText(Registry.ENTITY_TYPE.get(this.id).getTranslationKey()).asString();
        } else if(this.kind==CNodeType.fluid) {
            return new TranslatableText(Registry.FLUID.get(this.id).getBucketItem().getTranslationKey()).asString();
        } else if(this.kind==CNodeType.tag||this.kind==CNodeType.loot) {
            return TranslationService.translate(this);
        } else {
            return new TranslatableText(String.format("%s.%s.%s",
                    this.kind.toString(),
                    this.id.getNamespace(),
                    this.id.getPath().replace('/','.')
                    )).asString();
        }
    }

    public static String getTypeTranslatableName(CNodeType kind) {
        return new TranslatableText( CNODETYPE_TRANSLATION_PREFIX + kind.toString()).asString();
    }
}
