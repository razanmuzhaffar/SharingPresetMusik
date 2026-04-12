package com.sharingpresetmusik.controller;

import com.sharingpresetmusik.component.CategorySection;
import com.sharingpresetmusik.model.Preset;
import com.sharingpresetmusik.storage.DatabaseManager;
import com.sharingpresetmusik.storage.PresetRepository;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class MainPageController implements Initializable {

    // ── FXML ──
    @FXML private AnchorPane heroPane;
    @FXML private TextField  searchField;
    @FXML private Label      usernameLabel;
    @FXML private Label      avatarLabel;
    @FXML private Label      totalCount;
    @FXML private VBox       contentArea;
    @FXML private Button     filterAll;
    @FXML private Button     filterNew;
    @FXML private Button     filterTop;

    // ── Dependencies ──
    private PresetRepository presetRepo;
    private String activeFilter = "All";

    // ── Logged-in user (set dari login page sebelumnya) ──
    private int currentUserId   = 1;
    private String currentUsername = "razan_";

    // ─────────────────────────────────────────
    //  Init
    // ─────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // DatabaseManager di-inject dari MainApp / login controller
        // Default fallback kalau langsung dibuka
        DatabaseManager db = new DatabaseManager("presetsharingmusik.db");
        db.initSchema();
        presetRepo = new PresetRepository(db);

        usernameLabel.setText(currentUsername);
        avatarLabel.setText(initials(currentUsername));

        loadAll();
    }

    // Dipanggil dari login controller setelah login sukses
    public void setUser(int userId, String username, DatabaseManager db) {
        this.currentUserId   = userId;
        this.currentUsername = username;
        this.presetRepo      = new PresetRepository(db);

        usernameLabel.setText(username);
        avatarLabel.setText(initials(username));
        loadAll();
    }

    // ─────────────────────────────────────────
    //  Load & filter
    // ─────────────────────────────────────────
    private void loadAll() {
        try {
            List<Preset> all = presetRepo.findAll();
            totalCount.setText(all.size() + " presets");
            renderByCategory(all);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void renderByCategory(List<Preset> presets) {
        contentArea.getChildren().clear();

        List<String> categories = List.of("Synthesizer", "Guitar Tones", "Bass");

        for (String cat : categories) {
            List<Preset> catPresets = presets.stream()
                    .filter(p -> cat.equalsIgnoreCase(p.getCategory()))
                    .toList();

            if (catPresets.isEmpty()) continue;

            // total dari DB (bukan dari filtered list)
            int total;
            try {
                total = presetRepo.findByCategory(cat).size();
            } catch (SQLException e) {
                total = catPresets.size();
            }

            CategorySection section = new CategorySection(cat, catPresets, total);
            section.setOnSeeAll(this::handleSeeAll);
            contentArea.getChildren().add(section);
        }
    }

    // ─────────────────────────────────────────
    //  FXML handlers
    // ─────────────────────────────────────────
    @FXML
    private void handleUpload() {
        // TODO: buka UploadPresetDialog
        System.out.println("Buka upload dialog");
    }

    @FXML
    private void handleUserMenu() {
        // TODO: buka profile / logout popup
        System.out.println("Buka user menu");
    }

    @FXML
    private void handleFilter(javafx.event.ActionEvent e) {
        Button clicked = (Button) e.getSource();

        filterAll.getStyleClass().setAll("btn-filter");
        filterNew.getStyleClass().setAll("btn-filter");
        filterTop.getStyleClass().setAll("btn-filter");
        clicked.getStyleClass().setAll("btn-filter-active");

        activeFilter = clicked.getText();
        applyFilter();
    }

    private void applyFilter() {
        try {
            String q = searchField.getText();

            List<Preset> result = switch (activeFilter) {
                case "New"       -> presetRepo.findAll().stream()
                        .sorted((a, b) -> b.getId_preset() - a.getId_preset())
                        .toList();
                case "Top rated" -> presetRepo.findAll().stream()
                        .sorted((a, b) -> b.getPreset_download() - a.getPreset_download())
                        .toList();
                default          -> presetRepo.findAll();
            };

            // search filter on top
            if (q != null && !q.isBlank()) {
                String ql = q.toLowerCase();
                result = result.stream()
                        .filter(p ->
                                p.getPreset_name().toLowerCase().contains(ql) ||
                                        (p.getSynth_model()  != null && p.getSynth_model().toLowerCase().contains(ql)) ||
                                        (p.getCategory()     != null && p.getCategory().toLowerCase().contains(ql))
                        ).toList();
            }

            totalCount.setText(result.size() + " presets");
            renderByCategory(result);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        applyFilter();
    }

    private void handleSeeAll(String category) {
        // TODO: navigasi ke halaman category penuh
        System.out.println("See all: " + category);
    }

    // ─────────────────────────────────────────
    //  Utils
    // ─────────────────────────────────────────
    private String initials(String username) {
        if (username == null || username.isBlank()) return "?";
        String clean = username.replaceAll("[^a-zA-Z0-9]", "");
        return clean.length() >= 2
                ? clean.substring(0, 2).toUpperCase()
                : clean.toUpperCase();
    }
}