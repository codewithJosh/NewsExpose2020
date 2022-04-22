package com.codewithjosh.NewsExpose2k20.models;

public class UpdateModel {

    private String update_id;
    private String update_image;
    private String update_content;
    private String user_id;
    private int user_version_code;

    public UpdateModel() {
    }

    public UpdateModel(String update_id, String update_image, String update_content, String user_id, int user_version_code) {
        this.update_id = update_id;
        this.update_image = update_image;
        this.update_content = update_content;
        this.user_id = user_id;
        this.user_version_code = user_version_code;
    }

    public String getUpdate_id() {
        return update_id;
    }

    public void setUpdate_id(String update_id) {
        this.update_id = update_id;
    }

    public String getUpdate_image() {
        return update_image;
    }

    public void setUpdate_image(String update_image) {
        this.update_image = update_image;
    }

    public String getUpdate_content() {
        return update_content;
    }

    public void setUpdate_content(String update_content) {
        this.update_content = update_content;
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
