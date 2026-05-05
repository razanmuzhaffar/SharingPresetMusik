package com.sharingpresetmusik.controller;

import com.sharingpresetmusik.model.Akun;
import com.sharingpresetmusik.model.Preset;
import com.sharingpresetmusik.preset.PresetService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class UploadController implements Initializable {

    @FXML private TextField namaPresetField;
    @FXML private ComboBox<String> genreCombo;
    @FXML private ComboBox<String> jenisInstrumentCombo;
    @FXML private ComboBox<String> tipePresetCombo;
    @FXML private StackPane dropZone;
    @FXML private Label dropLabel;
    @FXML private Button uploadBtn;
    @FXML private Button cancelBtn;

    // Drop zone untuk file demo audio
    @FXML private StackPane demoDropZone;
    @FXML private Label demoDropLabel;

    private File selectedFile     = null;
    private File selectedDemoFile = null;  // opsional

    private Akun currentUser;
    private PresetService presetService;
    private String token;

    private static final List<String> PRESET_EXTENSIONS = List.of(
            ".vital", ".vstpreset", ".fxp", ".fxb", ".nmsv",
            ".adv", ".adg", ".aupreset", ".xpf", ".sf2", ".sfz",
            ".syx", ".mid", ".prst", ".tone", ".irs", ".tsl",
            ".zpe", ".patch", ".preset"
    );

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        populateComboBoxes();
    }

    public void init(Akun akun, PresetService service, String token) {
        this.currentUser   = akun;
        this.presetService = service;
        this.token         = token;
    }

    private void populateComboBoxes() {
        genreCombo.getItems().addAll(
                "Electronic", "Ambient", "Bass", "Cinematic",
                "Hip-Hop", "House", "Techno", "Pop", "Jazz", "Classical"
        );
        jenisInstrumentCombo.getItems().addAll(
                "Synthesizer", "Bass", "Lead", "Pad",
                "Pluck", "Keys", "Drums", "FX", "Vocal", "Guitar"
        );
        tipePresetCombo.getItems().addAll(PRESET_EXTENSIONS);
    }

    // ─── Drop zone preset ────────────────────────────────────────────────────────

    @FXML
    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
            dropZone.getStyleClass().add("drop-zone-active");
        }
        event.consume();
    }

    @FXML
    private void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            processPresetFile(db.getFiles().get(0));
            success = true;
        }
        dropZone.getStyleClass().remove("drop-zone-active");
        event.setDropCompleted(success);
        event.consume();
    }

    @FXML
    private void handleDropZoneClick() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Pilih File Preset");

        String[] presetGlobs = PRESET_EXTENSIONS.stream()
                .map(ext -> "*" + ext)
                .toArray(String[]::new);

        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Semua Preset yang Didukung", presetGlobs),
                new FileChooser.ExtensionFilter("Synthesizer Presets",
                        "*.vital", "*.vstpreset", "*.fxp", "*.fxb", "*.nmsv",
                        "*.adv", "*.adg", "*.aupreset", "*.sf2", "*.sfz"),
                new FileChooser.ExtensionFilter("Guitar / Efek Presets",
                        "*.prst", "*.tone", "*.irs", "*.tsl", "*.zpe"),
                new FileChooser.ExtensionFilter("SysEx / MIDI", "*.syx", "*.mid"),
                new FileChooser.ExtensionFilter("Semua File", "*.*")
        );

        Stage stage = (Stage) dropZone.getScene().getWindow();
        File file = fc.showOpenDialog(stage);
        if (file != null) processPresetFile(file);
    }

    private void processPresetFile(File file) {
        selectedFile = file;
        dropLabel.setText("✓  " + file.getName());
        dropZone.getStyleClass().remove("drop-zone-active");

        // Auto-select tipe preset dari extension file
        String ext = getExtension(file.getName());
        if (!ext.isEmpty()) {
            tipePresetCombo.getItems().stream()
                    .filter(item -> item.equalsIgnoreCase(ext))
                    .findFirst()
                    .ifPresent(tipePresetCombo::setValue);
        }
    }

    // ─── Drop zone demo audio ────────────────────────────────────────────────────

    @FXML
    private void handleDemoDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
            demoDropZone.getStyleClass().add("drop-zone-active");
        }
        event.consume();
    }

    @FXML
    private void handleDemoDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            processDemoFile(db.getFiles().get(0));
            success = true;
        }
        demoDropZone.getStyleClass().remove("drop-zone-active");
        event.setDropCompleted(success);
        event.consume();
    }

    @FXML
    private void handleDemoDropZoneClick() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Pilih File Demo Audio");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Audio Demo", "*.mp3", "*.wav", "*.ogg", "*.flac"),
                new FileChooser.ExtensionFilter("Semua File", "*.*")
        );

        Stage stage = (Stage) demoDropZone.getScene().getWindow();
        File file = fc.showOpenDialog(stage);
        if (file != null) processDemoFile(file);
    }

    private void processDemoFile(File file) {
        selectedDemoFile = file;
        demoDropLabel.setText("✓  " + file.getName());
        demoDropZone.getStyleClass().remove("drop-zone-active");
    }

    // ─── Upload ──────────────────────────────────────────────────────────────────

    @FXML
    private void handleUpload() {
        if (!validateForm()) return;
        if (selectedFile == null) {
            showAlert(Alert.AlertType.WARNING, "File Kosong", "Pilih file preset dulu.");
            return;
        }

        String nama       = namaPresetField.getText().trim();
        String genre      = genreCombo.getValue();
        String instrument = jenisInstrumentCombo.getValue();
        String tipe       = tipePresetCombo.getValue();

        try {
            Preset preset = new Preset();
            preset.setPreset_name(nama);
            preset.setCategory(instrument);  // category = jenis instrument (untuk filter)
            preset.setGenre(genre);          // genre disimpan terpisah
            preset.setSynth_model(tipe);

            presetService.uploadPreset(
                    preset,
                    selectedFile.toPath(),
                    selectedDemoFile != null ? selectedDemoFile.toPath() : null,
                    token
            );

            showAlert(Alert.AlertType.INFORMATION, "Upload Berhasil",
                    "Preset \"" + nama + "\" berhasil diupload!");
            SceneManager.goBack();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Upload Gagal", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        SceneManager.goBack();
    }

    // ─── Validasi ────────────────────────────────────────────────────────────────

    private boolean validateForm() {
        if (namaPresetField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Form Tidak Lengkap", "Nama preset tidak boleh kosong.");
            namaPresetField.requestFocus();
            return false;
        }
        if (genreCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Form Tidak Lengkap", "Silakan pilih genre.");
            return false;
        }
        if (jenisInstrumentCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Form Tidak Lengkap", "Silakan pilih jenis instrument.");
            return false;
        }
        if (tipePresetCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Form Tidak Lengkap", "Silakan pilih tipe preset.");
            return false;
        }
        return true;
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot >= 0) ? filename.substring(dot) : "";
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}