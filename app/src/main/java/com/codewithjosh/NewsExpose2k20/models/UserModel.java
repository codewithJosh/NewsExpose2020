package com.codewithjosh.NewsExpose2k20.models;

public class UserModel {

    private String user_bio;
    private String user_contact;
    private String user_email;
    private String user_id;
    private String user_image;
    private boolean user_is_admin;
    private boolean user_is_verified;
    private String user_name;
    private int user_version_code;

    public UserModel() {

    }

    public UserModel(final String user_bio, final String user_contact, final String user_email, final String user_id, final String user_image, final boolean user_is_admin, final boolean user_is_verified, final String user_name, final int user_version_code) {

        this.user_bio = user_bio;
        this.user_contact = user_contact;
        this.user_email = user_email;
        this.user_id = user_id;
        this.user_image = user_image;
        this.user_is_admin = user_is_admin;
        this.user_is_verified = user_is_verified;
        this.user_name = user_name;
        this.user_version_code = user_version_code;

    }

    public String getUser_bio()
    {

        return user_bio;

    }

    public String getUser_contact()
    {

        return user_contact;

    }

    public String getUser_email()
    {

        return user_email;

    }

    public String getUser_id()
    {

        return user_id;

    }

    public String getUser_image()
    {

        return user_image;

    }

    public boolean isUser_is_admin()
    {

        return user_is_admin;

    }

    public boolean isUser_is_verified()
    {

        return user_is_verified;

    }

    public String getUser_name()
    {

        return user_name;

    }

    public int getUser_version_code()
    {

        return user_version_code;

    }

    public void setUser_version_code(final int user_version_code)
    {

        this.user_version_code = user_version_code;

    }

}
