package com.codewithjosh.NewsExpose2k20.models;

public class CommentModel {

    private String comment_content;
    private String user_id;
    private int user_version_code;

    public CommentModel() {
    }

    public CommentModel(String comment_content, String user_id, int user_version_code) {
        this.comment_content = comment_content;
        this.user_id = user_id;
        this.user_version_code = user_version_code;
    }

    public String getComment_content() {
        return comment_content;
    }

    public void setComment_content(String comment_content) {
        this.comment_content = comment_content;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public int getUser_version_code() {
        return user_version_code;
    }

    public void setUser_version_code(int user_version_code) {
        this.user_version_code = user_version_code;
    }

}
