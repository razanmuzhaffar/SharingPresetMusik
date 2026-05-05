package com.sharingpresetmusik.controller;

import com.sharingpresetmusik.auth.AkunService;
import com.sharingpresetmusik.model.Akun;
import com.sharingpresetmusik.preset.PresetService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private AkunService akunService;
    private PresetService presetService;

    public void init(AkunService akunService, PresetService presetService) {
        this.akunService = akunService;
        this.presetService = presetService;
    }

    @FXML
    private void handleLogin() {
        String email = usernameField.getText().trim();
        String password = passwordField.getText();

        try {
            Akun akun = akunService.login(email, password);
            goToMainPage(akun);
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void handleGoRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Register.fxml")
            );
            Scene scene = new Scene(loader.load(), 400, 560);
            RegisterController ctrl = loader.getController();
            ctrl.init(akunService, presetService);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goToMainPage(Akun akun) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/MainPage.fxml")
            );
            Scene scene = new Scene(loader.load(), 900, 700);
            MainPageController ctrl = loader.getController();
            ctrl.init(akun, presetService, akunService); // ✅ pass akunService

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}