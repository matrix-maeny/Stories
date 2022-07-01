package com.matrix_maeny.stories.fragments.profile;

public class FollowModel {

    private String username = "", profilePicUrl = "";//,if = "";
    private String userId = "";


    public FollowModel() {
    }

    public FollowModel(String username, String profilePicUrl, String userId) {
        this.username = username;
        this.profilePicUrl = profilePicUrl;
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
