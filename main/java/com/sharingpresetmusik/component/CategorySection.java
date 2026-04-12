package com.sharingpresetmusik.component;

import com.sharingpresetmusik.model.Preset;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.List;
import java.util.function.Consumer;

public class CategorySection extends VBox {

    private static final int MAX_PREVIEW = 3;

    private final String category;
    private final List<Preset> presets;
    private final int total;
    private Consumer<String> onSeeAll;

    public CategorySection(String category, List<Preset> presets, int total) {
        this.category = category;
        this.presets  = presets;
        this.total    = total;
        build();
    }

    private void build() {
        setSpacing(14);
        setPadding(new javafx.geometry.Insets(0, 0, 32, 0));
        getChildren().addAll(buildHeader(), buildGrid());
    }

    private HBox buildHeader() {
        HBox header = new HBox(8);
        header.getStyleClass().add("category-header");
        header.setAlignment(Pos.CENTER_LEFT);

        Region dot = new Region();
        dot.setMinSize(7, 7);
        dot.setMaxSize(7, 7);
        dot.getStyleClass().add(dotClass());

        Label nameLabel = new Label(category.toUpperCase());
        nameLabel.getStyleClass().add("category-name");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Label countLabel = new Label(total + " presets");
        countLabel.getStyleClass().add("category-count");

        Button seeAllBtn = new Button("See all →");
        seeAllBtn.getStyleClass().add("btn-see-all");
        seeAllBtn.setOnAction(e -> { if (onSeeAll != null) onSeeAll.accept(category); });

        header.getChildren().addAll(dot, nameLabel, countLabel, seeAllBtn);
        return header;
    }

    private GridPane buildGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(33.33);
            col.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(col);
        }

        List<Preset> slice = presets.subList(0, Math.min(MAX_PREVIEW, presets.size()));
        for (int i = 0; i < slice.size(); i++) {
            grid.add(new PresetCard(slice.get(i)), i, 0);
        }

        return grid;
    }

    private String dotClass() {
        return switch (category) {
            case "Guitar Tones" -> "category-dot-guitar";
            case "Bass"         -> "category-dot-bass";
            default             -> "category-dot-synth";
        };
    }

    public void setOnSeeAll(Consumer<String> handler) {
        this.onSeeAll = handler;
    }
}