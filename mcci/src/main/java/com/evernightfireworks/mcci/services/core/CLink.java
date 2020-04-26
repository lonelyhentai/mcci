package com.evernightfireworks.mcci.services.core;

import net.minecraft.util.Identifier;

public class CLink {
    CNode startVert;
    CNode endVert;
    Object info;
    Identifier infoId;
    CLinkType kind;
    public CLink(CNode startVert, CNode endVert, Object info, Identifier infoId, CLinkType kind) {
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
}
