package com.sharingpresetmusik.controller;

import com.sharingpresetmusik.auth.AkunService;
import com.sharingpresetmusik.preset.PresetService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PresetApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        AkunService akunService     = new AkunService();
        PresetService presetService = new PresetService();

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/Login.fxml")
        );
        Scene scene = new Scene(loader.load(), 400, 500);
        LoginController ctrl = loader.getController();
        ctrl.init(akunService, presetService);

        stage.setTitle("PresetMusik");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}