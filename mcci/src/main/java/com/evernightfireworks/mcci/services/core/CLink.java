package com.evernightfireworks.mcci.services.core;

import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class CLink {
    public CNode startVert;
    public CNode endVert;
    public String info;
    public Identifier infoId;
    public CLinkType kind;
    private static final String CLINKTYPE_TRANSLATION_PREFIX = "service.mcci.core.clink_type.";

    public CLink(CNode startVert, CNode endVert, String info, Identifier infoId, CLinkType kind) {
        this.startVert = startVert;
        this.endVert = endVert;
        this.info = info;
        this.infoId = infoId;
        this.kind = kind;
    }

    public static String buildKey(CNode startVert, CNode endVert) {
        return String.format("%s#%s", startVert.toKey(), endVert.toKey());
    }

    public String toKey() {
        return CLink.buildKey(this.startVert, this.endVert);
    }

    public static String getTypeTranslatableName(CLinkType kind) {
        return new TranslatableText(CLINKTYPE_TRANSLATION_PREFIX + kind.toString()).asString();
    }
}
