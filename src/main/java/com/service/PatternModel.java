package com.service;

import java.nio.ByteBuffer;

/**
 * Created by ZloiY on 06.04.17.
 */
public class PatternModel {
    private int id;
    private String name;
    private String description;
    private ByteBuffer image;
    private int group;

    public PatternModel(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ByteBuffer getImage() {
        return image;
    }

    public void setImage(ByteBuffer image) {
        this.image = image;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return id + " " +name + " " + description + " " +group+"\n";
    }
}
