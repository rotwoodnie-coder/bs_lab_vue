package com.xuanyue.exp.system.dto;

public class MenuListItem {

    private String menuId;
    private String parentId;
    private String menuName;
    private String menuCode;
    private String menuType;
    private String path;
    private String component;
    private Integer sortOrder;
    private String status;
    private String comments;

    public MenuListItem() {
    }

    public MenuListItem(String menuId, String parentId, String menuName, String menuCode, String menuType, String path, String component, Integer sortOrder, String status, String comments) {
        this.menuId = menuId;
        this.parentId = parentId;
        this.menuName = menuName;
        this.menuCode = menuCode;
        this.menuType = menuType;
        this.path = path;
        this.component = component;
        this.sortOrder = sortOrder;
        this.status = status;
        this.comments = comments;
    }

    public String getMenuId() { return menuId; }
    public void setMenuId(String menuId) { this.menuId = menuId; }
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }
    public String getMenuCode() { return menuCode; }
    public void setMenuCode(String menuCode) { this.menuCode = menuCode; }
    public String getMenuType() { return menuType; }
    public void setMenuType(String menuType) { this.menuType = menuType; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
