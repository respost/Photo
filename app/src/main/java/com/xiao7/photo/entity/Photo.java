package com.xiao7.photo.entity;

public class Photo {
    private String name;
    private String date;
    private long size;
    private String path;

    /**
     * 构造函数
     */
    public Photo() {
    }

    public Photo(String name, String date, long size, String path) {
        this.name = name;
        this.date = date;
        this.size = size;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
