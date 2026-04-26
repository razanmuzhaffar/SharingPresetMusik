package com.example.presetmusik;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PresetApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Load FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/presetmusik/Preset.fxml"));
        Scene scene = new Scene(loader.load(), 950, 700);

        // Load CSS
        String cssPath = "/com/example/presetmusik/styleMoses.css";
        System.out.println("Mencari CSS di: " + cssPath);
        System.out.println("URL: " + getClass().getResource(cssPath));

        if (getClass().getResource(cssPath) != null) {
            scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
            System.out.println("✅ CSS berhasil diload!");
        } else {
            System.out.println("❌ CSS tidak ditemukan!");
        }

        stage.setTitle("Preset Musik - Sharing Platform");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}