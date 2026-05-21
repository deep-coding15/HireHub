package com.hirehub.candidature.config;

import java.time.Instant;
import java.util.UUID;

public class UserContext {
    private static final ThreadLocal<UserInfo> userInfo = new ThreadLocal<>();

    public static void setUser(UUID userId, String email, String role, String fullName) {
        userInfo.set(new UserInfo(userId, email, email, role, fullName, null, null, null, false, false));
    }

    public static void setUser(UserInfo info) {
        userInfo.set(info);
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
        public final String username;
        public final String role;
        public final String fullName;
        public final String token;
        public final Instant issuedAt;
        public final Instant expiresAt;
        public final boolean tokenValid;
        public final boolean recruiterApproved;

        public UserInfo(UUID userId, String email, String role, String fullName) {
            this(userId, email, email, role, fullName, null, null, null, false, false);
        }

        public UserInfo(UUID userId,
                        String email,
                        String username,
                        String role,
                        String fullName,
                        String token,
                        Instant issuedAt,
                        Instant expiresAt,
                        boolean tokenValid,
                        boolean recruiterApproved) {
            this.userId = userId;
            this.email = email;
            this.username = username;
            this.role = role;
            this.fullName = fullName;
            this.token = token;
            this.issuedAt = issuedAt;
            this.expiresAt = expiresAt;
            this.tokenValid = tokenValid;
            this.recruiterApproved = recruiterApproved;
        }
    }
}

