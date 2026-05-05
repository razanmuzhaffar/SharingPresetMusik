package com.sharingpresetmusik.controller;

import com.sharingpresetmusik.model.Preset;
import com.sharingpresetmusik.preset.PresetService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SearchPageController implements Initializable {

    @FXML private HBox filterInstrumentBar;
    @FXML private HBox filterFormatBar;
    @FXML private FlowPane presetGrid;

    private PresetService presetService;
    private List<Preset> allPresets = new ArrayList<>();
    private List<PresetCardController> activeCards = new ArrayList<>();

    private static final String[] INSTRUMENT_FILTERS = {
            "Semua", "Synthesizer", "Guitar", "Bass",
            "Lead", "Pad", "Pluck", "Keys", "FX", "Drums"
    };

    private static final String[] FORMAT_FILTERS = {
            "Semua Format",
            ".vital", ".fxp / .fxb", ".vstpreset", ".nmsv", ".adv / .adg",
            ".aupreset", ".sf2 / .sfz", ".syx",
            ".prst", ".tone", ".tsl", ".zpe",
            ".patch / .preset"
    };

    private String activeInstrumentFilter = "Semua";
    private String activeFormatFilter     = "Semua Format";

    public void init(PresetService service) {
        this.presetService = service;
        loadPresets();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupInstrumentFilters();
        setupFormatFilters();
    }

    private void setupInstrumentFilters() {
        ToggleGroup group = new ToggleGroup();

        for (String filter : INSTRUMENT_FILTERS) {
            ToggleButton btn = new ToggleButton(filter);
            btn.setToggleGroup(group);
            btn.getStyleClass().add("btn-filter");

            btn.selectedProperty().addListener((obs, oldVal, selected) -> {
                if (selected) {
                    btn.getStyleClass().setAll("btn-filter-active");
                    activeInstrumentFilter = filter;
                    applyFilters();
                } else {
                    btn.getStyleClass().setAll("btn-filter");
                }
            });

            filterInstrumentBar.getChildren().add(btn);
        }

        ((ToggleButton) filterInstrumentBar.getChildren().get(0)).setSelected(true);
    }

    private void setupFormatFilters() {
        ToggleGroup group = new ToggleGroup();

        for (String filter : FORMAT_FILTERS) {
            ToggleButton btn = new ToggleButton(filter);
            btn.setToggleGroup(group);
            btn.getStyleClass().add("btn-filter");

            btn.selectedProperty().addListener((obs, oldVal, selected) -> {
                if (selected) {
                    btn.getStyleClass().setAll("btn-filter-active");
                    activeFormatFilter = filter;
                    applyFilters();
                } else {
                    btn.getStyleClass().setAll("btn-filter");
                }
            });

            filterFormatBar.getChildren().add(btn);
        }

        ((ToggleButton) filterFormatBar.getChildren().get(0)).setSelected(true);
    }

    private void applyFilters() {
        presetGrid.getChildren().clear();
        activeCards.clear();

        if (allPresets == null) return;

        for (Preset preset : allPresets) {
            if (matchesInstrument(preset) && matchesFormat(preset)) {
                addCard(preset);
            }
        }
    }

    private boolean matchesInstrument(Preset preset) {
        if (activeInstrumentFilter.equals("Semua")) return true;
        if (preset.getCategory() == null) return false;
        // category sekarang berisi jenis instrument (Synthesizer, Guitar, dll)
        // setelah fix di UploadController
        return preset.getCategory().equalsIgnoreCase(activeInstrumentFilter);
    }

    private boolean matchesFormat(Preset preset) {
        if (activeFormatFilter.equals("Semua Format")) return true;
        if (preset.getSynth_model() == null) return false;

        String model = preset.getSynth_model().toLowerCase().trim();

        // Filter bisa berisi grup seperti ".fxp / .fxb" — pecah per "/"
        String[] parts = activeFormatFilter.split("/");
        for (String part : parts) {
            String ext = part.trim().toLowerCase();
            if (model.equals(ext)) return true;
        }
        return false;
    }

    private void loadPresets() {
        presetGrid.getChildren().clear();
        activeCards.clear();

        if (presetService == null) return;

        try {
            allPresets = presetService.findAll();
            applyFilters();
        } catch (Exception e) {
            System.err.println("Gagal load presets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addCard(Preset preset) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/preset-item.fxml")
            );
            VBox card = loader.load();

            PresetCardController ctrl = loader.getController();
            ctrl.setPreset(preset);
            ctrl.setOnPlayCallback(() -> stopOtherCards(ctrl));

            card.setOnMouseClicked(e -> {
                if (!(e.getTarget() instanceof Button)) {
                    openDetail(preset);
                }
            });

            activeCards.add(ctrl);
            presetGrid.getChildren().add(card);

        } catch (IOException e) {
            System.err.println("Gagal load preset-item.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void stopOtherCards(PresetCardController current) {
        for (PresetCardController ctrl : activeCards) {
            if (ctrl != current && ctrl.isPlaying()) ctrl.stopAudio();
        }
    }

    private void openDetail(Preset preset) {
        try {
            FXMLLoader loader = SceneManager.loadWithLoader("Preset.fxml");
            PresetController ctrl = loader.getController();
            ctrl.setPreset(preset);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void search(String keyword) {
        presetGrid.getChildren().clear();
        activeCards.clear();

        if (keyword == null || keyword.isBlank()) {
            applyFilters();
            return;
        }

        String lower = keyword.toLowerCase();
        for (Preset preset : allPresets) {
            boolean nameMatch = preset.getPreset_name().toLowerCase().contains(lower);
            if (nameMatch && matchesInstrument(preset) && matchesFormat(preset)) {
                addCard(preset);
            }
        }
    }

    public void dispose() {
        for (PresetCardController ctrl : activeCards) ctrl.dispose();
        activeCards.clear();
    }
}