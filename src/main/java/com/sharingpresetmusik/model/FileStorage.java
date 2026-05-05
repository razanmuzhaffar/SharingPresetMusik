package com.sharingpresetmusik.model;

public class FileStorage {
    private String base_url;
    private int id_preset;

    public FileStorage() {}

    public FileStorage(String base_url, int id_preset) {
        this.base_url = base_url;
        this.id_preset = id_preset;
    }

    public String getBase_url() {
        return base_url;
    }
    public void setBase_url(String base_url) {
        this.base_url = base_url;
    }
    public int getId_preset() {
        return id_preset;
    }
    public void setId_preset(int id_preset) {
        this.id_preset = id_preset;
    }
}
