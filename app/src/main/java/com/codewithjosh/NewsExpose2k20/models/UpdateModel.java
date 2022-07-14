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

    public UpdateModel(final String update_id, final String update_image, final String update_content, final Date update_timestamp, final String user_id) {

        this.update_id = update_id;
        this.update_image = update_image;
        this.update_content = update_content;
        this.update_timestamp = update_timestamp;
        this.user_id = user_id;

    }

    public String getUpdate_id() {

        return update_id;

    }

    public String getUpdate_image() {

        return update_image;

    }

    public String getUpdate_content() {

        return update_content;

    }

    public Date getUpdate_timestamp() {

        return update_timestamp;

    }

    public String getUser_id() {

        return user_id;

    }

}
