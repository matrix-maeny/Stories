package com.matrix_maeny.stories.posts;

public class PostModel {

    private String title, tagLine,content, imageUrl, references = "";
    private String username = "";
    private String userUid = "";
    private int likes = 0, comments = 0;
    private String localDate;


    public PostModel() {
    }

    public PostModel(String userUid,String username,String title, String tagLine, String content, String imageUrl, String references,String localDate) {
        this.userUid = userUid;
        this.username = username;
        this.title = title;
        this.tagLine = tagLine;
        this.content = content;
        this.imageUrl = imageUrl;
        this.references = references;
        this.localDate = localDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTagLine() {
        return tagLine;
    }

    public void setTagLine(String tagLine) {
        this.tagLine = tagLine;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getReferences() {
        return references;
    }

    public void setReferences(String references) {
        this.references = references;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public String getLocalDate() {
        return localDate;
    }

    public void setLocalDate(String localDate) {
        this.localDate = localDate;
    }
}
