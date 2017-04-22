package com.service;

import javax.ws.rs.Path;
import java.nio.ByteBuffer;

/**
 * Модель паттерна
 */
public class PatternModel {
    /**
     * id паттерна в базе данных
     */
    private int id;
    /**
     * Название паттерна
     */
    private String name;
    /**
     * Описание паттерна
     */
    private String description;
    /**
     * Изображение паттерна
     */
    private byte[] image;
    /**
     * Группа паттерна
     */
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

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
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
        if (image == null)
        return id + " " +name + " " + description + " " +group;
        else return id+" "+name+" "+description+" "+group+" "+"with image";
    }
}
