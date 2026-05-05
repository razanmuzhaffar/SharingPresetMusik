package com.sharingpresetmusik.controller;

import com.sharingpresetmusik.model.Preset;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

public class PresetController {

    @FXML private Button downloadPresetBtn;
    @FXML private Button demoPresetBtn;
    @FXML private Slider volumeSlider;
    @FXML private Label volumeLabel;
    @FXML private Label presetNameLabel;
    @FXML private Label synthModelLabel;
    @FXML private Label downloadCountLabel;
    @FXML private Label categoryLabel;
    @FXML private Label genreLabel;

    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private Preset preset;

    // Semua extension yang dikenali — kalau server tidak kasih Content-Disposition,
    // kita cek synth_model dari preset, baru fallback ke .preset
    private static final Set<String> KNOWN_EXTENSIONS = Set.of(
            ".vital", ".vstpreset", ".fxp", ".fxb", ".nmsv", ".adv", ".adg",
            ".aupreset", ".xpf", ".sf2", ".sfz", ".syx", ".mid",
            ".prst", ".tone", ".irs", ".tsl", ".zpe",
            ".patch", ".preset", ".mp3", ".wav", ".ogg", ".flac"
    );

    public void setPreset(Preset p) {
        this.preset = p;
        presetNameLabel.setText(p.getPreset_name());
        synthModelLabel.setText(p.getSynth_model() != null ? p.getSynth_model() : "-");
        downloadCountLabel.setText("⬇ " + p.getPreset_download() + " downloads");
        categoryLabel.setText(p.getCategory() != null ? p.getCategory() : "-");
        genreLabel.setText(p.getCategory() != null ? p.getCategory() : "-");
    }

    @FXML
    private void handleCancel() {
        SceneManager.goBack();
    }

    @FXML
    public void initialize() {
        downloadPresetBtn.setOnAction(e -> downloadPreset());

        demoPresetBtn.setOnAction(e -> {
            if (isPlaying) stopDemo();
            else playDemo();
        });

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double volume = newVal.doubleValue();
            volumeLabel.setText((int)(volume * 100) + "%");
            if (mediaPlayer != null) mediaPlayer.setVolume(volume);
        });
    }

    private void downloadPreset() {
        if (preset == null) return;
        String apiUrl = "http://127.0.0.1:8000/api/presets/" + preset.getId_preset() + "/download";
        downloadFromInternet(apiUrl);
    }

    private void downloadFromInternet(String fileUrl) {
        Thread downloadThread = new Thread(() -> {
            try {
                URL url = new URL(fileUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                String fileName = preset.getPreset_name().replaceAll("[^a-zA-Z0-9_\\-]", "_");
                String ext = resolveExtension(connection);
                fileName += ext;

                String downloadsPath = System.getProperty("user.home") + File.separator + "Downloads";
                Path destination = Paths.get(downloadsPath, fileName);

                try (InputStream inputStream = connection.getInputStream()) {
                    Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
                }

                javafx.application.Platform.runLater(() ->
                        showAlert("Download Berhasil", "File disimpan ke:\n" + destination));

            } catch (IOException e) {
                javafx.application.Platform.runLater(() ->
                        showAlert("Download Gagal", "Error: " + e.getMessage()));
                e.printStackTrace();
            }
        });

        downloadThread.setDaemon(true);
        downloadThread.start();
    }

    /**
     * Cari extension dengan urutan prioritas:
     * 1. Content-Disposition header dari server
     * 2. synth_model dari preset (kalau valid)
     * 3. Fallback: .preset
     */
    private String resolveExtension(HttpURLConnection connection) {
        // 1. Coba dari Content-Disposition
        String contentDisposition = connection.getHeaderField("Content-Disposition");
        if (contentDisposition != null && contentDisposition.contains(".")) {
            int dotIdx = contentDisposition.lastIndexOf(".");
            String ext = contentDisposition.substring(dotIdx).replaceAll("[^a-zA-Z0-9.]", "");
            if (!ext.isBlank()) return ext;
        }

        // 2. Coba dari synth_model preset
        if (preset.getSynth_model() != null) {
            String model = preset.getSynth_model().trim().toLowerCase();
            // synth_model bisa ".fxp" atau "fxp"
            String extCandidate = model.startsWith(".") ? model : "." + model;
            if (KNOWN_EXTENSIONS.contains(extCandidate)) return extCandidate;
        }

        // 3. Fallback
        return ".preset";
    }

    private void playDemo() {
        try {
            var resource = getClass().getResource("/demo/demo_preset.mp3");
            if (resource == null) {
                showAlert("Demo Preset", "File demo belum tersedia.");
                return;
            }

            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }

            mediaPlayer = new MediaPlayer(new Media(resource.toExternalForm()));
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
                demoPresetBtn.setText("Demo Preset");
                isPlaying = false;
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
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}