package com.sharingpresetmusik.controller;

import com.sharingpresetmusik.model.Akun;
import com.sharingpresetmusik.model.Preset;
import com.sharingpresetmusik.preset.PresetService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProfileController {

    @FXML private ImageView profileImage;
    @FXML private ImageView navProfileImage;
    @FXML private Label usernameLabel;
    @FXML private Label followersCount;
    @FXML private Label followingCount;
    @FXML private Label presetCount;
    @FXML private Button followButton;
    @FXML private FlowPane presetGrid;
    @FXML private HBox audioPlayerBar;
    @FXML private Label nowPlayingLabel;
    @FXML private Button stopButton;
    @FXML private Slider progressSlider;

    private Akun currentUser; // ✅ ganti User → Akun
    private PresetService presetService; // ✅ inject dari luar
    private final List<PresetCardController> cardControllers = new ArrayList<>();

    // ✅ Dipanggil dari luar (mis. pas pindah scene) untuk inject dependency
    public void init(Akun akun, PresetService service) {
        this.currentUser = akun;
        this.presetService = service;
        loadUserProfile();
        loadPresets();
    }

    private void loadUserProfile() {
        usernameLabel.setText(currentUser.getUsername());

        // Akun tidak punya followers/following/presetCount — tampilkan placeholder dulu
        followersCount.setText("0");
        followingCount.setText("0");

        try {
            List<Preset> presets = presetService.findByUser(currentUser.getId_user());
            presetCount.setText(String.valueOf(presets.size()));
        } catch (Exception e) {
            presetCount.setText("?");
            e.printStackTrace();
        }

        // Foto profil — Akun tidak punya field foto, skip dulu
        // profileImage dan navProfileImage dibiarkan default
    }

    private void loadPresets() {
        presetGrid.getChildren().clear();
        cardControllers.clear();

        try {
            List<Preset> presets = presetService.findByUser(currentUser.getId_user());

            for (Preset preset : presets) {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/preset-item.fxml")
                );
                VBox cardNode = loader.load();
                PresetCardController cardController = loader.getController();

                cardController.setPreset(preset);
                cardController.setOnPlayCallback(() -> {
                    stopAllExcept(cardController);
                    showAudioBar(preset.getPreset_name());
                });

                cardControllers.add(cardController);
                presetGrid.getChildren().add(cardNode);
            }

        } catch (Exception e) {
            System.err.println("Gagal load presets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        SceneManager.goBack();
    }

    @FXML
    private void handleStop() {
        stopAllPresets();
        hideAudioBar();
    }

    private void stopAllExcept(PresetCardController active) {
        for (PresetCardController c : cardControllers) {
            if (c != active) c.stopAudio();
        }
    }

    private void stopAllPresets() {
        cardControllers.forEach(PresetCardController::stopAudio);
    }

    private void showAudioBar(String title) {
        audioPlayerBar.setVisible(true);
        nowPlayingLabel.setText("▶  " + title);
    }

    private void hideAudioBar() {
        audioPlayerBar.setVisible(false);
        nowPlayingLabel.setText("Tidak ada yang diputar");
        progressSlider.setValue(0);
    }

    private String formatNumber(int number) {
        if (number >= 1_000_000) return String.format("%.1fM", number / 1_000_000.0);
        if (number >= 1000)      return String.format("%.1fK", number / 1000.0);
        return String.valueOf(number);
    }
}