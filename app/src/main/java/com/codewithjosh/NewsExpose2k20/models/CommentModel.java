package com.codewithjosh.NewsExpose2k20.models;

import java.util.Date;

public class CommentModel {

    private String comment_content;
    private Date comment_timestamp;
    private String user_id;

    public CommentModel() {

    }

    public CommentModel(final String comment_content, final Date comment_timestamp, final String user_id) {

        this.comment_content = comment_content;
        this.comment_timestamp = comment_timestamp;
        this.user_id = user_id;

    }

    public String getComment_content() {

        return comment_content;

    }

    public Date getComment_timestamp() {

        return comment_timestamp;

    }

    public String getUser_id() {

        return user_id;

    }

}
