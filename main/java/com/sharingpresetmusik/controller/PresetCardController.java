package com.example.sharingpresetmusic;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.net.URL;

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
        titleLabel.setText(preset.getTitle());
        dateLabel.setText(preset.getUploadDate());
        genreTag.setText(preset.getGenre());
        typeTag.setText(preset.getPresetType());
        instrumentTag.setText(preset.getInstrumentType());
    }

    public void setOnPlayCallback(Runnable callback) {
        this.onPlayCallback = callback;
    }

    @FXML
    private void handlePlay() {
        if (isPlaying) {
            pauseAudio();
        } else {
            if (onPlayCallback != null) onPlayCallback.run();
            playAudio();
        }
    }

    private void playAudio() {
        if (mediaPlayer == null) {
            try {
                URL audioUrl = getClass().getResource(preset.getAudioFileName());
                if (audioUrl == null) {
                    titleLabel.setText("⚠ Audio tidak ditemukan");
                    return;
                }
                Media media = new Media(audioUrl.toString());
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
    public Preset getPreset() { return preset; }

    public void dispose() {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }
}
