package com.evernightfireworks.mcci.services.core;

import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;

public class CNode {
    public Identifier id;
    public CNodeType kind;
    public ArrayList<CLink> inlinks;
    public ArrayList<CLink> outlinks;

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
}
