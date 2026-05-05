package com.sharingpresetmusik.controller;

import com.sharingpresetmusik.model.Preset;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class PresetCardController {

    @FXML private Button playButton;
    @FXML private Label titleLabel;
    @FXML private Label dateLabel;
    @FXML private Label genreTag;
    @FXML private Label typeTag;
    @FXML private Label instrumentTag;
    @FXML private Slider audioSlider;

    private Preset preset;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private Runnable onPlayCallback;

    public void setPreset(Preset preset) {
        this.preset = preset;
        titleLabel.setText(preset.getPreset_name());
        dateLabel.setText("");
        genreTag.setText(preset.getCategory() != null ? preset.getCategory() : "");

        // typeTag → tampilkan extension dari synth_model
        // synth_model bisa berisi ".vital", ".fxp", ".prst", dll
        String synthModel = preset.getSynth_model();
        if (synthModel != null && !synthModel.isBlank()) {
            // Pastikan selalu diawali titik dan lowercase
            typeTag.setText(synthModel.startsWith(".")
                    ? synthModel.toLowerCase()
                    : "." + synthModel.toLowerCase());
        } else {
            typeTag.setText("");
        }

        instrumentTag.setText("");
    }

    public void setOnPlayCallback(Runnable callback) {
        this.onPlayCallback = callback;
    }

    @FXML
    private void handlePlay() {
        if (isPlaying) pauseAudio();
        else {
            if (onPlayCallback != null) onPlayCallback.run();
            playAudio();
        }
    }

    private void playAudio() {
        if (mediaPlayer == null) {
            try {
                String fileUrl = preset.getFile_url();
                if (fileUrl == null || fileUrl.isBlank()) {
                    titleLabel.setText("⚠ Audio tidak ditemukan");
                    return;
                }
                Media media = new Media(fileUrl);
                mediaPlayer = new MediaPlayer(media);

                mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                    if (!audioSlider.isValueChanging()) {
                        double total = mediaPlayer.getTotalDuration().toSeconds();
                        if (total > 0) {
                            audioSlider.setValue(newTime.toSeconds() / total * 100);
                        }
                    }
                });

                mediaPlayer.setOnEndOfMedia(() -> {
                    isPlaying = false;
                    playButton.setText("▶");
                    audioSlider.setValue(0);
                    mediaPlayer.seek(Duration.ZERO);
                });

                audioSlider.setDisable(false);
                audioSlider.valueChangingProperty().addListener((obs, wasChanging, changing) -> {
                    if (!changing) {
                        double seek = audioSlider.getValue() / 100.0
                                * mediaPlayer.getTotalDuration().toSeconds();
                        mediaPlayer.seek(Duration.seconds(seek));
                    }
                });

            } catch (Exception e) {
                titleLabel.setText("⚠ Gagal load audio");
                e.printStackTrace();
                return;
            }
        }

        mediaPlayer.play();
        isPlaying = true;
        playButton.setText("⏸");
    }

    private void pauseAudio() {
        if (mediaPlayer != null) mediaPlayer.pause();
        isPlaying = false;
        playButton.setText("▶");
    }

    public void stopAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.seek(Duration.ZERO);
        }
        isPlaying = false;
        playButton.setText("▶");
        audioSlider.setValue(0);
    }

    public boolean isPlaying() { return isPlaying; }
    public Preset getPreset()  { return preset; }

    public void dispose() {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }
}