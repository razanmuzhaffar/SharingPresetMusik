package com.example.presetmusik;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;

public class PresetController {

    @FXML private TextField searchField;
    @FXML private Button downloadPresetBtn;
    @FXML private Button demoPresetBtn;
    @FXML private Slider volumeSlider;
    @FXML private Label volumeLabel;

    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;  // Tambahan untuk track status

    @FXML
    public void initialize() {
        // Tombol Download Preset
        downloadPresetBtn.setOnAction(e -> downloadPreset());

        // Tombol Demo Preset (Play/Stop)
        demoPresetBtn.setOnAction(e -> {
            if (isPlaying) {
                stopDemo();
            } else {
                playDemo();
            }
        });

        // Volume Slider
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double volume = newVal.doubleValue();
            int percent = (int)(volume * 100);
            volumeLabel.setText(percent + "%");

            if (mediaPlayer != null) {
                mediaPlayer.setVolume(volume);
            }
        });

        // Search
        searchField.setOnAction(e -> {
            String query = searchField.getText();
            System.out.println("Mencari: " + query);
        });
    }

    private void downloadPreset() {
        showAlert("Download Preset", "Mengunduh preset...");
    }

    private void playDemo() {
        try {
            String audioFile = getClass().getResource("/demo/demo_preset.mp3").toURI().getPath();
            File file = new File(audioFile);

            if (!file.exists()) {
                showAlert("Demo Preset", "File demo belum tersedia.");
                return;
            }

            Media media = new Media(file.toURI().toString());

            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }

            mediaPlayer = new MediaPlayer(media);

            // Set volume sesuai slider
            mediaPlayer.setVolume(volumeSlider.getValue());

            mediaPlayer.setOnPlaying(() -> {
                demoPresetBtn.setText("⏸ Stop");
                isPlaying = true;
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                demoPresetBtn.setText("Demo Preset");
                isPlaying = false;
                mediaPlayer.stop();
            });

            mediaPlayer.setOnStopped(() -> {
                if (isPlaying) {
                    demoPresetBtn.setText("Demo Preset");
                    isPlaying = false;
                }
            });

            mediaPlayer.play();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal memutar demo: " + e.getMessage());
        }
    }

    private void stopDemo() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        demoPresetBtn.setText("Demo Preset");
        isPlaying = false;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}