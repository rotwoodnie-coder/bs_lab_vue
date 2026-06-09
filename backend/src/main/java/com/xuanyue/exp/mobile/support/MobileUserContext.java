package com.xuanyue.exp.mobile.support;

/**
 * 移动端用户上下文工具 — 已移除演示用退回到种子 user_id 的逻辑。
 * 前端登录后始终传入真实 userId，不再使用 demo 占位。
 */
public final class MobileUserContext {

    private MobileUserContext() {
    }

    public static String resolveStudentId(String userId) {
        return userId;
    }

    public static String resolveParentId(String userId) {
        return userId;
    }

    public static String resolveTeacherId(String userId) {
        return userId;
    }
}
