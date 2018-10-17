package com.pushparaj.firebasenotification;

/**
 * Created by Vijay on 20-06-2017.
 */

public class DataFields {
    String image;
    String name;
    String status;
    String id;

    public String getFull_image() {
        return full_image;
    }

    public void setFull_image(String full_image) {
        this.full_image = full_image;
    }

    String full_image;

    public DataFields(String image, String name, String status, String id) {
        this.image = image;
        this.name = name;
        this.status = status;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DataFields() {
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
