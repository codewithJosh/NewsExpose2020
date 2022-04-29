package com.codewithjosh.NewsExpose2k20.models;

import java.util.Date;

public class UpdateModel {

    private String update_id;
    private String update_image;
    private String update_content;
    private Date update_timestamp;
    private String user_id;

    public UpdateModel() {
    }

    public UpdateModel(String update_id, String update_image, String update_content, Date update_timestamp, String user_id) {
        this.update_id = update_id;
        this.update_image = update_image;
        this.update_content = update_content;
        this.update_timestamp = update_timestamp;
        this.user_id = user_id;
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

    public Date getUpdate_timestamp() {
        return update_timestamp;
    }

    public void setUpdate_timestamp(Date update_timestamp) {
        this.update_timestamp = update_timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

}
