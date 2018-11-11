package com.openclassrooms.go4lunch.models;

import android.support.annotation.Nullable;

/**
 * Model used for User objects
 */
public class User {

    private String uid;
    private String username;
    @Nullable
    private String urlPicture;

    public User(String uid) {
        this.uid = uid;
    }

    public User(){}

    public User(String uid, String username) {
        this.uid = uid;
        this.username = username;
    }

    public User(String uid, String username, @Nullable String urlPicture) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
    }

    // --- GETTERS ---
    public String getUid() { return uid; }
    public String getUsername() { return username; }

    @Nullable
    public String getUrlPicture() {
        return urlPicture;
    }
}
