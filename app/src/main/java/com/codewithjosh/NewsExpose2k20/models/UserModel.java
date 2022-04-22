package com.codewithjosh.NewsExpose2k20.models;

public class UserModel {

    private String user_bio;
    private String user_email;
    private String user_id;
    private String user_image;
    private boolean user_is_admin;
    private String user_name;
    private int user_version_code;

    public UserModel() {
    }

    public UserModel(String user_bio, String user_email, String user_id, String user_image, boolean user_is_admin, String user_name, int user_version_code) {
        this.user_bio = user_bio;
        this.user_email = user_email;
        this.user_id = user_id;
        this.user_image = user_image;
        this.user_is_admin = user_is_admin;
        this.user_name = user_name;
        this.user_version_code = user_version_code;
    }

    public String getUser_bio() {
        return user_bio;
    }

    public void setUser_bio(String user_bio) {
        this.user_bio = user_bio;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_image() {
        return user_image;
    }

    public void setUser_image(String user_image) {
        this.user_image = user_image;
    }

    public boolean isUser_is_admin() {
        return user_is_admin;
    }

    public void setUser_is_admin(boolean user_is_admin) {
        this.user_is_admin = user_is_admin;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public int getUser_version_code() {
        return user_version_code;
    }

    public void setUser_version_code(int user_version_code) {
        this.user_version_code = user_version_code;
    }

}
