package com.example.sharingpresetmusic;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.io.InputStream;
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

    private User currentUser;
    private final List<PresetCardController> cardControllers = new ArrayList<>();
    private boolean isFollowing = false;

    @FXML
    public void initialize() {
        loadUserProfile();
        loadPresets();
    }

    private void loadUserProfile() {
        currentUser = DummyData.getCurrentUser();

        usernameLabel.setText(currentUser.getUsername());
        followersCount.setText(formatNumber(currentUser.getFollowersCount()));
        followingCount.setText(formatNumber(currentUser.getFollowingCount()));
        presetCount.setText(String.valueOf(currentUser.getPresetUploadedCount()));

        // Load foto profil — semua resource ada langsung di resources/com/example/sharingpresetmusic/
        String imgFile = currentUser.getProfileImagePath();
        InputStream stream = getClass().getResourceAsStream(imgFile);
        if (stream != null) {
            Image image = new Image(stream);
            profileImage.setImage(image);
            navProfileImage.setImage(image);
        }
        // Kalau stream null (foto belum ada), ImageView dibiarkan kosong — tidak crash
    }

    private void loadPresets() {
        List<Preset> presets = DummyData.getUserPresets(currentUser.getUsername());
        presetGrid.getChildren().clear();
        cardControllers.clear();

        for (Preset preset : presets) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("preset-item.fxml")
                );
                VBox cardNode = loader.load();
                PresetCardController cardController = loader.getController();

                cardController.setPreset(preset);
                cardController.setOnPlayCallback(() -> {
                    stopAllExcept(cardController);
                    showAudioBar(preset.getTitle());
                });

                cardControllers.add(cardController);
                presetGrid.getChildren().add(cardNode);

            } catch (IOException e) {
                System.err.println("Gagal load preset-view.fxml: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleFollow() {
        isFollowing = !isFollowing;
        if (isFollowing) {
            followButton.setText("Following ✓");
            followButton.getStyleClass().add("following");
            int n = currentUser.getFollowersCount() + 1;
            currentUser.setFollowersCount(n);
            followersCount.setText(formatNumber(n));
        } else {
            followButton.setText("Follow");
            followButton.getStyleClass().remove("following");
            int n = currentUser.getFollowersCount() - 1;
            currentUser.setFollowersCount(n);
            followersCount.setText(formatNumber(n));
        }
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
