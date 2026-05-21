package com.zhuji.common.security.util;

import jakarta.servlet.http.HttpServletRequest;

public class CurrentUser {

    public static Long getUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return userId != null ? (Long) userId : null;
    }

    public static String getUsername(HttpServletRequest request) {
        Object username = request.getAttribute("username");
        return username != null ? (String) username : null;
    }
}
