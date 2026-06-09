package com.xuanyue.exp.mobile.repository;

/**
 * 首页瀑布流条目引用（用于 UNION 分页后再批量加载详情）
 */
public class MobileFeedRef {

    private final String itemId;
    private final String itemSource;

    public MobileFeedRef(String itemId, String itemSource) {
        this.itemId = itemId;
        this.itemSource = itemSource;
    }

    public String getItemId() {
        return itemId;
    }

    public String getItemSource() {
        return itemSource;
    }
}
