package com.sharingpresetmusik.component;

import com.sharingpresetmusik.model.Preset;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.Random;

public class PresetCard extends VBox {

    private static final Color COLOR_SYNTH  = Color.web("#c8281e");
    private static final Color COLOR_GUITAR = Color.web("#1a5c3a");
    private static final Color COLOR_BASS   = Color.web("#1a2f5c");

    private final Preset preset;
    private boolean playing = false;
    private Button playBtn;

    public PresetCard(Preset preset) {
        this.preset = preset;
        build();
    }

    private void build() {
        getStyleClass().add("preset-card");
        setSpacing(8);
        setPrefWidth(0);
        setMaxWidth(Double.MAX_VALUE);

        // ── NAME ──
        HBox topRow = new HBox(6);
        topRow.setAlignment(Pos.TOP_LEFT);

        Label nameLabel = new Label(preset.getPreset_name());
        nameLabel.getStyleClass().add("preset-name");
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        nameLabel.setWrapText(true);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        topRow.getChildren().add(nameLabel);

        // ── SYNTH MODEL (sebagai subtitle) ──
        Label synthLabel = new Label(
                preset.getSynth_model() != null && !preset.getSynth_model().isBlank()
                        ? preset.getSynth_model()
                        : "—"
        );
        synthLabel.getStyleClass().add("preset-author");

        // ── WAVEFORM ──
        Canvas waveform = buildWaveform();

        // ── CATEGORY TAG ──
        HBox tagRow = new HBox(4);
        tagRow.setAlignment(Pos.CENTER_LEFT);
        if (preset.getCategory() != null && !preset.getCategory().isBlank()) {
            Button tagBtn = new Button(preset.getCategory().toLowerCase());
            tagBtn.getStyleClass().add("tag-chip");
            tagRow.getChildren().add(tagBtn);
        }

        // ── BOTTOM: download count + play + get ──
        HBox bottomRow = new HBox(6);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        Label dlCount = new Label("↓ " + preset.getPreset_download());
        dlCount.getStyleClass().add("preset-author");
        HBox.setHgrow(dlCount, Priority.ALWAYS);

        playBtn = new Button("▶");
        playBtn.getStyleClass().add("btn-play-mini");
        playBtn.setOnAction(e -> togglePlay());

        Button dlBtn = new Button("▼ Get");
        dlBtn.getStyleClass().add("btn-download");
        dlBtn.setOnAction(e -> onDownload());

        bottomRow.getChildren().addAll(dlCount, playBtn, dlBtn);

        getChildren().addAll(topRow, synthLabel, waveform, tagRow, bottomRow);
    }

    private Canvas buildWaveform() {
        Canvas canvas = new Canvas(180, 28);
        drawWave(canvas);
        canvas.widthProperty().addListener((obs, ov, nv) -> {
            if (nv.doubleValue() > 0) drawWave(canvas);
        });
        return canvas;
    }

    private void drawWave(Canvas canvas) {
        double w = canvas.getWidth(), h = canvas.getHeight();
        Color waveColor = switch (preset.getCategory() != null ? preset.getCategory() : "") {
            case "Guitar Tones" -> COLOR_GUITAR;
            case "Bass"         -> COLOR_BASS;
            default             -> COLOR_SYNTH;
        };

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);
        gc.setFill(Color.web("#ece9e3"));
        gc.fillRect(0, 0, w, h);

        int bars = 42;
        double barW = w / bars;
        Random rng = new Random(preset.getId_preset());

        for (int i = 0; i < bars; i++) {
            double barH  = rng.nextDouble() * 0.72 * h + 0.1 * h;
            double barY  = (h - barH) / 2;
            double opacity = 0.35 + rng.nextDouble() * 0.55;
            gc.setFill(Color.color(
                    waveColor.getRed(),
                    waveColor.getGreen(),
                    waveColor.getBlue(),
                    opacity));
            gc.fillRect(i * barW, barY, barW - 1, barH);
        }

        gc.setStroke(waveColor);
        gc.setLineWidth(1.5);
        gc.setGlobalAlpha(0.9);
        gc.strokeLine(0, h / 2, w * 0.35, h / 2);
        gc.setGlobalAlpha(1.0);
    }

    private void togglePlay() {
        playing = !playing;
        playBtn.setText(playing ? "■" : "▶");
    }

    private void onDownload() {
        // TODO: trigger download via FileStorageService + increment counter
        System.out.println("Download: " + preset.getPreset_name()
                + " | file_url: " + preset.getFile_url());
    }

    public Preset getPreset() { return preset; }
}