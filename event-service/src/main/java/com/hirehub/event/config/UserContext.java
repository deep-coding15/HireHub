package com.hirehub.event.config;

import java.util.UUID;

public class UserContext {
    private static final ThreadLocal<UserInfo> userInfo = new ThreadLocal<>();

    public static void setUser(UUID userId, String email, String role, String fullName) {
        userInfo.set(new UserInfo(userId, email, role, fullName));
    }

    public static UserInfo getUser() {
        return userInfo.get();
    }

    public static UUID getUserId() {
        UserInfo info = userInfo.get();
        return info != null ? info.userId : null;
    }

    public static String getEmail() {
        UserInfo info = userInfo.get();
        return info != null ? info.email : null;
    }

    public static String getRole() {
        UserInfo info = userInfo.get();
        return info != null ? info.role : null;
    }

    public static void clear() {
        userInfo.remove();
    }

    public static class UserInfo {
        public final UUID userId;
        public final String email;
        public final String role;
        public final String fullName;

        public UserInfo(UUID userId, String email, String role, String fullName) {
            this.userId = userId;
            this.email = email;
            this.role = role;
            this.fullName = fullName;
        }
    }
}

