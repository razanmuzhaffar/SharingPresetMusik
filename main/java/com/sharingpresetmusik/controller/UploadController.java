package uploadapp;

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

    // ── NAVBAR ──
    @FXML private TextField searchField;

    // ── FORM FIELDS ──
    @FXML private TextField namaPresetField;
    @FXML private ComboBox<String> genreCombo;
    @FXML private ComboBox<String> jenisInstrumentCombo;
    @FXML private ComboBox<String> tipePresetCombo;

    // ── DROP ZONE ──
    @FXML private StackPane dropZone;
    @FXML private Label dropLabel;

    // ── BUTTONS ──
    @FXML private Button uploadBtn;
    @FXML private Button cancelBtn;

    // State
    private File selectedFile = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        populateComboBoxes();
        setupSearchField();
    }

    // ════════════════════════════════════
    //  INIT
    // ════════════════════════════════════

    private void populateComboBoxes() {
        // Genre
        genreCombo.getItems().addAll(
            "Electronic", "Ambient", "Bass", "Cinematic",
            "Hip-Hop", "House", "Techno", "Pop", "Jazz", "Classical"
        );

        // Jenis Instrument
        jenisInstrumentCombo.getItems().addAll(
            "Synthesizer", "Bass", "Lead", "Pad",
            "Pluck", "Keys", "Drums", "FX", "Vocal", "Guitar"
        );

        // Tipe Preset
        tipePresetCombo.getItems().addAll(
            ".fxp", ".vstpreset", ".adv", ".nmsv",
            ".aupreset", ".syx", ".mid"
        );
    }

    private void setupSearchField() {
        // Pencarian preset — implementasi sesuai kebutuhan
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            // TODO: filter preset list dari database/service
        });
    }

    // ════════════════════════════════════
    //  DROP ZONE HANDLERS
    // ════════════════════════════════════

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
            List<File> files = db.getFiles();
            File file = files.get(0); // ambil file pertama
            processFile(file);
            success = true;
        }

        dropZone.getStyleClass().remove("drop-zone-active");
        event.setDropCompleted(success);
        event.consume();
    }

    @FXML
    private void handleDropZoneClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih File Preset atau Demo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Preset Files",
                "*.fxp", "*.vstpreset", "*.adv", "*.nmsv", "*.aupreset"),
            new FileChooser.ExtensionFilter("Audio Files",
                "*.mp3", "*.wav", "*.ogg", "*.flac"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Stage stage = (Stage) dropZone.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) processFile(file);
    }

    private void processFile(File file) {
        selectedFile = file;
        dropLabel.setText("✓  " + file.getName());
        dropZone.getStyleClass().remove("drop-zone-active");

        // Auto-isi tipe preset dari ekstensi file
        String ext = getExtension(file.getName());
        if (!ext.isEmpty()) {
            tipePresetCombo.getItems().stream()
                .filter(item -> item.equalsIgnoreCase(ext))
                .findFirst()
                .ifPresent(match -> tipePresetCombo.setValue(match));
        }
    }

    // ════════════════════════════════════
    //  BUTTON HANDLERS
    // ════════════════════════════════════

    @FXML
    private void handleUpload() {
        if (!validateForm()) return;

        String nama    = namaPresetField.getText().trim();
        String genre   = genreCombo.getValue();
        String jenis   = jenisInstrumentCombo.getValue();
        String tipe    = tipePresetCombo.getValue();

        System.out.println("=== Upload Preset ===");
        System.out.println("Nama    : " + nama);
        System.out.println("Genre   : " + genre);
        System.out.println("Jenis   : " + jenis);
        System.out.println("Tipe    : " + tipe);
        System.out.println("File    : " + (selectedFile != null ? selectedFile.getAbsolutePath() : "—"));

        // TODO: kirim ke service/repository
        // presetService.upload(nama, genre, jenis, tipe, selectedFile);

        showAlert(Alert.AlertType.INFORMATION, "Upload Berhasil",
            "Preset \"" + nama + "\" berhasil diupload!");
    }

    @FXML
    private void handleCancel() {
        // Reset form
        namaPresetField.clear();
        genreCombo.setValue(null);
        jenisInstrumentCombo.setValue(null);
        tipePresetCombo.setValue(null);
        selectedFile = null;
        dropLabel.setText("Drop File / Demo Preview");
    }

    // ════════════════════════════════════
    //  VALIDATION
    // ════════════════════════════════════

    private boolean validateForm() {
        if (namaPresetField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Form Tidak Lengkap",
                "Nama preset tidak boleh kosong.");
            namaPresetField.requestFocus();
            return false;
        }
        if (genreCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Form Tidak Lengkap",
                "Silakan pilih genre.");
            return false;
        }
        if (jenisInstrumentCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Form Tidak Lengkap",
                "Silakan pilih jenis instrument.");
            return false;
        }
        if (tipePresetCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Form Tidak Lengkap",
                "Silakan pilih tipe preset.");
            return false;
        }
        return true;
    }

    // ════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════

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
