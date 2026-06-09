package com.xuanyue.exp.mobile.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 将 JWT 解析出的用户身份注入请求头，兼容现有 Controller 的 @RequestHeader("X-User-Id") 写法。
 */
public class MobileAuthRequestWrapper extends HttpServletRequestWrapper {

    private final MobileAuthUser authUser;
    private final Map<String, String> injectedHeaders;

    public MobileAuthRequestWrapper(HttpServletRequest request, MobileAuthUser authUser) {
        super(request);
        this.authUser = authUser;
        this.injectedHeaders = new LinkedHashMap<String, String>();
        injectedHeaders.put("X-User-Id", safe(authUser.getUserId()));
        injectedHeaders.put("X-User-Role-Id", safe(authUser.getUserRoleId()));
        injectedHeaders.put("X-User-Root-Org-Id", safe(authUser.getRootOrgId()));
    }

    @Override
    public String getHeader(String name) {
        if (name != null) {
            for (Map.Entry<String, String> entry : injectedHeaders.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(name)) {
                    return entry.getValue();
                }
            }
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        java.util.Set<String> names = new java.util.LinkedHashSet<String>();
        Enumeration<String> original = super.getHeaderNames();
        while (original.hasMoreElements()) {
            names.add(original.nextElement());
        }
        names.addAll(injectedHeaders.keySet());
        return Collections.enumeration(names);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        String value = getHeader(name);
        if (value != null) {
            return Collections.enumeration(Collections.singletonList(value));
        }
        return super.getHeaders(name);
    }

    public MobileAuthUser getAuthUser() {
        return authUser;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
