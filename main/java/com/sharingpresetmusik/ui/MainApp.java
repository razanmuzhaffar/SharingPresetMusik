package com.sharingpresetmusik.ui;

import com.sharingpresetmusik.controller.MainPageController;
import com.sharingpresetmusik.storage.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Inisialisasi database
        DatabaseManager db = new DatabaseManager("sharingpresetmusik.db");
        db.initSchema();

        // Load FXML (asumsikan ada MainPage.fxml di resources)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainPage.fxml"));
        Scene scene = new Scene(loader.load());

        // Set controller dengan database
        MainPageController controller = loader.getController();
        controller.setUser(1, "testuser", db); // Dummy user, ganti sesuai kebutuhan

        primaryStage.setTitle("Sharing Preset Musik");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
