package com.sharingpresetmusik.controller;

import com.sharingpresetmusik.auth.AkunService;
import com.sharingpresetmusik.model.Akun;
import com.sharingpresetmusik.preset.PresetService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainPageController {

    @FXML private StackPane contentArea;
    @FXML private TextField searchField;
    @FXML private Label profileInitial;

    private Akun currentUser;
    private PresetService presetService;
    private AkunService akunService;

    public void init(Akun akun, PresetService presetService, AkunService akunService) {
        this.currentUser = akun;
        this.presetService = presetService;
        this.akunService = akunService;
        SceneManager.setContentArea(contentArea);

        // Set inisial dari username
        if (akun != null && akun.getUsername() != null && !akun.getUsername().isEmpty()) {
            profileInitial.setText(
                    String.valueOf(akun.getUsername().charAt(0)).toUpperCase()
            );
        }

        try {
            FXMLLoader loader = SceneManager.loadWithLoader("SearchPage.fxml");
            SearchPageController ctrl = loader.getController();
            ctrl.init(presetService);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogoClick() {
        try {
            FXMLLoader loader = SceneManager.loadWithLoader("SearchPage.fxml");
            SearchPageController ctrl = loader.getController();
            ctrl.init(presetService);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Login.fxml")
            );
            Scene scene = new Scene(loader.load(), 400, 500);
            LoginController ctrl = loader.getController();
            ctrl.init(akunService, presetService);

            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(scene);
            SceneManager.clearHistory();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpload() {
        try {
            FXMLLoader loader = SceneManager.loadWithLoader("UploadPage.fxml");
            UploadController ctrl = loader.getController();
            ctrl.init(currentUser, presetService, akunService.getToken());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        try {
            FXMLLoader loader = SceneManager.loadWithLoader("SearchPage.fxml");
            SearchPageController ctrl = loader.getController();
            ctrl.init(presetService);
            ctrl.search(searchField.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProfile() {
        try {
            FXMLLoader loader = SceneManager.loadWithLoader("profile-view.fxml");
            ProfileController ctrl = loader.getController();
            ctrl.init(currentUser, presetService);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}