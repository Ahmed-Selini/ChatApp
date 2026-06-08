package com.example.chatproject;

public class User {
    private int id;
    private String username;
    private String email;
    private String password;
    private boolean isActive;
    private String codeVerif;
    // NOUVEAU: Pour stocker l'URI de l'image de profil
    private String profileImagePath;
    // NOUVEAU: Pour stocker l'état de blocage
    private boolean isBlocked;

    // Constructeur mis à jour pour correspondre à DatabaseHelper.cursorToUser
    // (8 arguments au total, le dernier étant isBlocked)
    public User(int id, String username, String email, String password, boolean isActive, String codeVerif, String profileImagePath, boolean isBlocked) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.isActive = isActive;
        this.codeVerif = codeVerif;
        this.profileImagePath = profileImagePath;
        this.isBlocked = isBlocked; // Attribution du nouvel attribut
    }

    // --- Getters ---

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public boolean isActive() { return isActive; }
    public String getCodeVerif() { return codeVerif; }
    public String getProfileImagePath() { return profileImagePath; }

    // NOUVEAU Getter
    public boolean isBlocked() { return isBlocked; }

    // --- Setters ---

    public void setActive(boolean active) { this.isActive = active; }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // NOUVEAU Setter
    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }
}