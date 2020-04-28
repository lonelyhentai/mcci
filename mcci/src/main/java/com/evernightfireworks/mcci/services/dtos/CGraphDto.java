package com.evernightfireworks.mcci.services.dtos;

import java.util.ArrayList;

public class CGraphDto {
    public String title;
    public String target;
    public ArrayList<CNodeDto> nodes;
    public ArrayList<CLinkDto> links;
    public ArrayList<CCategoryDto> categories;
    public ArrayList<CCategoryDto> linkCategories;

    public CGraphDto(
            String title, String target, ArrayList<CNodeDto> nodes, ArrayList<CLinkDto> links,
                     ArrayList<CCategoryDto> categories, ArrayList<CCategoryDto> linkCategories) {
        this.title = title;
        this.target = target;
        this.nodes = nodes;
        this.links = links;
        this.categories = categories;
        this.linkCategories = linkCategories;
    }
}
