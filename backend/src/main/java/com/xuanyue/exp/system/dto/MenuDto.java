package com.xuanyue.exp.system.dto;

public class MenuDto {

    private String menuId;
    private String parentId;
    private String menuName;
    private String menuPath;
    private String menuIcon;
    private Integer orderNum;
    private String status;

    public MenuDto() {
    }

    public MenuDto(String menuId, String parentId, String menuName, String menuPath, String menuIcon, Integer orderNum, String status) {
        this.menuId = menuId;
        this.parentId = parentId;
        this.menuName = menuName;
        this.menuPath = menuPath;
        this.menuIcon = menuIcon;
        this.orderNum = orderNum;
        this.status = status;
    }

    public String getMenuId() { return menuId; }
    public void setMenuId(String menuId) { this.menuId = menuId; }
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }
    public String getMenuPath() { return menuPath; }
    public void setMenuPath(String menuPath) { this.menuPath = menuPath; }
    public String getMenuIcon() { return menuIcon; }
    public void setMenuIcon(String menuIcon) { this.menuIcon = menuIcon; }
    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
