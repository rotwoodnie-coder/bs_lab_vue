package com.xuanyue.exp.system.dto;

import java.util.ArrayList;
import java.util.List;

public class MenuTreeItem {

    private String key;
    private String path;
    private String label;
    private String icon;
    private List<MenuTreeItem> children = new ArrayList<MenuTreeItem>();

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<MenuTreeItem> getChildren() {
        return children;
    }

    public void setChildren(List<MenuTreeItem> children) {
        this.children = children;
    }
}
