package com.sharingpresetmusik.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Preset {

    @JsonProperty("id")
    private int id_preset;

    @JsonProperty("user_id")
    private int id_user;

    private String preset_name;
    private String preset_desc;
    private int preset_download;
    private String synth_model;
    private String file_url;
    private String category;
    private String genre;
    private String demo_url;

    public Preset() {}

    public Preset(int id_preset, int id_user, String preset_name, String preset_desc,
                  int preset_download, String synth_model, String file_url, String category) {
        this.id_preset       = id_preset;
        this.id_user         = id_user;
        this.preset_name     = preset_name;
        this.preset_desc     = preset_desc;
        this.preset_download = preset_download;
        this.synth_model     = synth_model;
        this.file_url        = file_url;
        this.category        = category;
    }

    public int getId_preset() { return id_preset; }
    public void setId_preset(int id_preset) { this.id_preset = id_preset; }

    public int getId_user() { return id_user; }
    public void setId_user(int id_user) { this.id_user = id_user; }

    public String getPreset_name() { return preset_name; }
    public void setPreset_name(String preset_name) { this.preset_name = preset_name; }

    public String getPreset_desc() { return preset_desc; }
    public void setPreset_desc(String preset_desc) { this.preset_desc = preset_desc; }

    public int getPreset_download() { return preset_download; }
    public void setPreset_download(int preset_download) { this.preset_download = preset_download; }

    public String getSynth_model() { return synth_model; }
    public void setSynth_model(String synth_model) { this.synth_model = synth_model; }

    public String getFile_url() { return file_url; }
    public void setFile_url(String file_url) { this.file_url = file_url; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getDemo_url() { return demo_url; }
    public void setDemo_url(String demo_url) { this.demo_url = demo_url; }

    public void incrementDownload() { this.preset_download += 1; }
}