package com.openclassrooms.go4lunch.models;

import android.support.annotation.Nullable;

public class User {

    private String uid;
    private String username;
    @Nullable
    private String urlPicture;

    public User() {}

    public User(String uid, String username, @Nullable String urlPicture) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
    }

    // --- GETTERS ---
    public String getUid() { return uid; }
    public String getUsername() { return username; }

    // --- SETTERS ---
    // public void setUsername(String username) { this.username = username; }
    // public void setUid(String uid) { this.uid = uid; }
    // public void setUrlPicture(@Nullable String urlPicture) { this.urlPicture = urlPicture; }
}
