package com.matrix_maeny.stories.comments;

public class CommentModel {

    private String username = "", profilePicUrl = "",comment = "";
    private String userId = "";

    public CommentModel() {
    }


    public CommentModel(String userId,String username, String profilePicUrl, String comment) {
        this.userId = userId;
        this.username = username;
        this.profilePicUrl = profilePicUrl;
        this.comment = comment;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
