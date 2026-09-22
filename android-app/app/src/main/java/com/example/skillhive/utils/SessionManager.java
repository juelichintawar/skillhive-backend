package com.example.skillhive.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "SkillHiveSession";

    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";

    // Profile photo
    private static final String KEY_PROFILE_IMAGE_URI =
            "profileImageUri";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {

        preferences = context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );
    }


    // =========================================================
    // SAVE LOGIN SESSION
    // =========================================================

    public void saveSession(
            String token,
            Long userId,
            String fullName,
            String email,
            String role
    ) {

        preferences.edit()
                .putString(KEY_TOKEN, token)
                .putLong(
                        KEY_USER_ID,
                        userId != null ? userId : -1
                )
                .putString(KEY_FULL_NAME, fullName)
                .putString(KEY_EMAIL, email)
                .putString(KEY_ROLE, role)
                .apply();
    }


    // =========================================================
    // GET TOKEN
    // =========================================================

    public String getToken() {

        return preferences.getString(
                KEY_TOKEN,
                null
        );
    }


    // =========================================================
    // GET USER ID
    // =========================================================

    public Long getUserId() {

        long userId =
                preferences.getLong(
                        KEY_USER_ID,
                        -1
                );

        return userId == -1 ? null : userId;
    }


    // =========================================================
    // GET FULL NAME
    // =========================================================

    public String getFullName() {

        return preferences.getString(
                KEY_FULL_NAME,
                null
        );
    }


    // =========================================================
    // GET EMAIL
    // =========================================================

    public String getEmail() {

        return preferences.getString(
                KEY_EMAIL,
                null
        );
    }


    // =========================================================
    // GET ROLE
    // =========================================================

    public String getRole() {

        return preferences.getString(
                KEY_ROLE,
                null
        );
    }


    // =========================================================
    // SAVE PROFILE PHOTO URI
    // =========================================================

    public void saveProfileImageUri(String uri) {

        preferences.edit()
                .putString(
                        KEY_PROFILE_IMAGE_URI,
                        uri
                )
                .apply();
    }


    // =========================================================
    // GET PROFILE PHOTO URI
    // =========================================================

    public String getProfileImageUri() {

        return preferences.getString(
                KEY_PROFILE_IMAGE_URI,
                null
        );
    }


    // =========================================================
    // CHECK LOGIN
    // =========================================================

    public boolean isLoggedIn() {

        return getToken() != null
                && !getToken().isEmpty();
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    public void logout() {

        preferences.edit()
                .clear()
                .apply();
    }
}