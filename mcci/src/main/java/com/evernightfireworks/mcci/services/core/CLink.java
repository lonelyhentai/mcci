package com.evernightfireworks.mcci.services.core;

import net.minecraft.util.Identifier;

public class CLink {
    CNode invert;
    CNode outvert;
    Object info;
    Identifier infoId;
    public CLink(CNode invert, CNode outvert, Object info, Identifier infoId) {
        this.invert = invert;
        this.outvert = outvert;
        this.info = info;
        this.infoId = infoId;
    }

    public static String buildKey(CNode invert, CNode outvert) {
        return String.format("%s#%s", invert.toKey(), outvert.toKey());
    }

    public String toKey() {
        return CLink.buildKey(this.invert, this.outvert);
    }
}
