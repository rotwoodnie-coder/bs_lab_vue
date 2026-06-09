package com.xuanyue.exp.mobile.support;

import java.util.UUID;

public final class MobileIds {

    private MobileIds() {
    }

    public static String newId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String newId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
