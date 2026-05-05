package com.sharingpresetmusik.controller;

import com.sharingpresetmusik.auth.AkunService;
import com.sharingpresetmusik.preset.PresetService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private AkunService akunService;
    private PresetService presetService;

    public void init(AkunService akunService, PresetService presetService) {
        this.akunService = akunService;
        this.presetService = presetService;
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        try {
            akunService.register(username, username, email, password);
            goToLogin();
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void handleGoLogin() {
        goToLogin();
    }

    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Login.fxml")
            );
            Scene scene = new Scene(loader.load(), 400, 500);
            LoginController ctrl = loader.getController();
            ctrl.init(akunService, presetService);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}