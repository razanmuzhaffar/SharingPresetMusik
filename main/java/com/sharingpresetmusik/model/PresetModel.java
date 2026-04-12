package com.sharingpresetmusik.model;

public class PresetModel {
    private int id_preset;
    private String gear_name;
    private String file_extension;

    public PresetModel(int id_preset, String gear_name, String file_extension) {
        this.id_preset = id_preset;
        this.gear_name = gear_name;
        this.file_extension = file_extension;
    }

    public PresetModel() {}

    public int getId_preset() {
        return id_preset;
    }
    public void setId_preset(int id_preset) {
        this.id_preset = id_preset;
    }
    public String getGear_name() {
        return gear_name;
    }
    public void setGear_name(String gear_name) {
        this.gear_name = gear_name;
    }
    public String getFile_extension() {
        return file_extension;
    }
    public void setFile_extension(String file_extension) {
        this.file_extension = file_extension;
    }
}
