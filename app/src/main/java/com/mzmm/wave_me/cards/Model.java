package com.mzmm.wave_me.cards;

public class Model {
    private int image;
    private String title;
    private String desc;
    private int id;

    public int getId() {
        return id;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Model(int image, String title, String desc, int id) {
        this.image = image;
        this.title = title;
        this.desc = desc;
        this.id = id;
    }
}
