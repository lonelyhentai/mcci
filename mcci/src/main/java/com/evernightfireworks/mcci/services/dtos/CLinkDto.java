package com.evernightfireworks.mcci.services.dtos;

public class CLinkDto {
    public int source;
    public int target;
    public String info;
    public String infoId;
    public int kind;

    public CLinkDto(int source, int target, String info, String infoId, int kind) {
        this.source = source;
        this.target = target;
        this.info = info;
        this.infoId = infoId;
        this.kind = kind;
    }
}
