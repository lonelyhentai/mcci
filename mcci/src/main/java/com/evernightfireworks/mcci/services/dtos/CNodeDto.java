package com.evernightfireworks.mcci.services.dtos;

public class CNodeDto {
    public int id;
    public String name;
    public int category;
    public int symbolSize;

    public CNodeDto(int id, String name, int kind, int symbolSize) {
        this.id = id;
        this.name = name;
        this.category = kind;
        this.symbolSize = symbolSize;
    }
}
