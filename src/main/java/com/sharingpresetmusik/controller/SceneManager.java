package com.sharingpresetmusik.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class SceneManager {

    private static StackPane contentArea;
    private static final Deque<Node> history = new ArrayDeque<>();

    public static void setContentArea(StackPane pane) {
        contentArea = pane;
    }

    public static void clearHistory() {
        history.clear();
    }

    public static void navigateTo(String fxmlName) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneManager.class.getResource("/" + fxmlName)
            );
            Node view = loader.load();

            // Simpan halaman sekarang ke history sebelum pindah
            if (!contentArea.getChildren().isEmpty()) {
                history.push(contentArea.getChildren().get(0));
            }

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Gagal navigasi ke " + fxmlName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static FXMLLoader loadWithLoader(String fxmlName) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                SceneManager.class.getResource("/" + fxmlName)
        );
        Node view = loader.load();

        if (!contentArea.getChildren().isEmpty()) {
            history.push(contentArea.getChildren().get(0));
        }

        contentArea.getChildren().setAll(view);
        return loader;
    }

    public static void goBack() {
        if (!history.isEmpty()) {
            Node previous = history.pop();
            contentArea.getChildren().setAll(previous);
        }
        // Kalau history kosong, tidak melakukan apa-apa (sudah di halaman pertama)
    }

    public static boolean canGoBack() {
        return !history.isEmpty();
    }
}