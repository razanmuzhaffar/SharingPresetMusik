package com.example.presetmusik;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.net.URL;
import java.util.ResourceBundle;

public class SearchPageController implements Initializable {

    @FXML private HBox filterBar;
    @FXML private FlowPane presetGrid;
    @FXML private TextField searchBar;


    String[] presets = {
            "Warm Pad", "Bass Drop", "Lead Synth",
            "Acoustic Guitar", "Electric Bass", "Arp Keys",
            "Drum Kit", "Choir Voices", "String Ensemble"
    };

    String[] filters = {"Semua", "Synthesizer", "Guitar", "Bass"};

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupFilters();
        loadPresets();
    }

    private void setupFilters() {
        ToggleGroup group = new ToggleGroup();
        for (String filter : filters) {
            ToggleButton btn = new ToggleButton(filter);
            btn.setToggleGroup(group);
            btn.setStyle("-fx-background-color: #3c3c3c; -fx-text-fill: white; -fx-cursor: hand;");
            btn.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) btn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: black; -fx-cursor: hand;");
                else btn.setStyle("-fx-background-color: #3c3c3c; -fx-text-fill: white; -fx-cursor: hand;");
            });
            filterBar.getChildren().add(btn);
        }
        ((ToggleButton) filterBar.getChildren().get(0)).setSelected(true);
    }

    private void loadPresets() {
        presetGrid.getChildren().clear();
        for (String preset : presets) {
            VBox card = new VBox();
            card.setPrefSize(150, 120);
            card.setAlignment(Pos.CENTER);
            card.setStyle("-fx-background-color: #2e2e2e; -fx-border-color: #444; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");

            Label name = new Label(preset);
            name.setStyle("-fx-text-fill: white; -fx-font-size: 13;");
            card.getChildren().add(name);

            card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #3e3e3e; -fx-border-color: #888; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;"));
            card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #2e2e2e; -fx-border-color: #444; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;"));

            presetGrid.getChildren().add(card);
        }
    }
}
