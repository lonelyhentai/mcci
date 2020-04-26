package com.evernightfireworks.mcci.services.core;

import net.minecraft.util.Identifier;

public class CLink {
    CNode invert;
    CNode outvert;
    Object info;
    Identifier infoId;
    CLinkType kind;
    public CLink(CNode invert, CNode outvert, Object info, Identifier infoId, CLinkType kind) {
        this.invert = invert;
        this.outvert = outvert;
        this.info = info;
        this.infoId = infoId;
        this.kind = kind;
    }

    public static String buildKey(CNode invert, CNode outvert) {
        return String.format("%s#%s", invert.toKey(), outvert.toKey());
    }

    public String toKey() {
        return CLink.buildKey(this.invert, this.outvert);
    }
}
