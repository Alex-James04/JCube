package ui;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import db.ColorSchemeDB;
import db.SettingsDB;
import model.ColorScheme;
import model.InspectionMode;
import model.Settings;
import model.SpacebarMode;
import model.StatSpec;

public class SettingsViewController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private CheckBox showScrambleCheck;

    @FXML
    private CheckBox confirmDeletesCheck;

    @FXML
    private ChoiceBox<SpacebarMode> spacebarModeChoice;

    @FXML
    private ChoiceBox<InspectionMode> inspectionModeChoice;

    @FXML
    private ListView<StatSpec> statsListView;

    @FXML
    private Spinner<Integer> decimalPlacesSpinner;

    @FXML
    private ColorPicker backgroundPicker;

    @FXML
    private ColorPicker surfacePicker;

    @FXML
    private ColorPicker textPrimaryPicker;

    @FXML
    private ColorPicker textSecondaryPicker;

    @FXML
    private ColorPicker accentPicker;

    @FXML
    private ColorPicker buttonPicker;

    @FXML
    private ColorPicker buttonHoverPicker;

    @FXML
    private ColorPicker dangerPicker;

    @FXML
    private ColorPicker borderPicker;

    private final SettingsDB settingsDB = new SettingsDB();
    private final ColorSchemeDB colorSchemeDB = new ColorSchemeDB();
    private Settings settings;
    private ColorScheme colorScheme;

    // Guards against the picker-population loop (used by presets and initial load) re-triggering
    // the per-picker listeners and causing redundant persist+apply churn while setting all 9 at once.
    private boolean suppressColorListeners;

    @FXML
    private void initialize() {
        settings = settingsDB.get();
        colorScheme = colorSchemeDB.get();

        showScrambleCheck.setSelected(settings.isShowScramble());
        showScrambleCheck.selectedProperty().addListener((obs, oldValue, newValue) -> {
            settings.setShowScramble(newValue);
            settingsDB.update(settings);
        });

        confirmDeletesCheck.setSelected(settings.isConfirmDeletes());
        confirmDeletesCheck.selectedProperty().addListener((obs, oldValue, newValue) -> {
            settings.setConfirmDeletes(newValue);
            settingsDB.update(settings);
        });

        spacebarModeChoice.setItems(FXCollections.observableArrayList(SpacebarMode.values()));
        spacebarModeChoice.setValue(settings.getSpacebarMode());
        spacebarModeChoice.valueProperty().addListener((obs, oldValue, newValue) -> {
            settings.setSpacebarMode(newValue);
            settingsDB.update(settings);
        });

        inspectionModeChoice.setItems(FXCollections.observableArrayList(InspectionMode.values()));
        inspectionModeChoice.setValue(settings.getInspectionMode());
        inspectionModeChoice.valueProperty().addListener((obs, oldValue, newValue) -> {
            settings.setInspectionMode(newValue);
            settingsDB.update(settings);
        });

        decimalPlacesSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 3, settings.getDecimalPlaces()));
        decimalPlacesSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            settings.setDecimalPlaces(newValue);
            settingsDB.update(settings);
        });

        statsListView.setCellFactory(list -> new StatCell());
        refreshStatsList();

        wireColorPicker(backgroundPicker, colorScheme.getBackground(), ColorScheme::setBackground);
        wireColorPicker(surfacePicker, colorScheme.getSurface(), ColorScheme::setSurface);
        wireColorPicker(textPrimaryPicker, colorScheme.getTextPrimary(), ColorScheme::setTextPrimary);
        wireColorPicker(textSecondaryPicker, colorScheme.getTextSecondary(), ColorScheme::setTextSecondary);
        wireColorPicker(accentPicker, colorScheme.getAccent(), ColorScheme::setAccent);
        wireColorPicker(buttonPicker, colorScheme.getButton(), ColorScheme::setButton);
        wireColorPicker(buttonHoverPicker, colorScheme.getButtonHover(), ColorScheme::setButtonHover);
        wireColorPicker(dangerPicker, colorScheme.getDanger(), ColorScheme::setDanger);
        wireColorPicker(borderPicker, colorScheme.getBorder(), ColorScheme::setBorder);
    }

    private interface ColorSetter {
        void set(ColorScheme scheme, String hex);
    }

    private void wireColorPicker(ColorPicker picker, String initialHex, ColorSetter setter) {
        picker.setValue(Color.web(initialHex));
        picker.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (suppressColorListeners) {
                return;
            }
            setter.set(colorScheme, toHex(newValue));
            colorSchemeDB.update(colorScheme);
            MainWindow.applyColorScheme(rootPane.getScene(), colorScheme);
        });
    }

    private static String toHex(Color color) {
        int r = (int) Math.round(color.getRed() * 255);
        int g = (int) Math.round(color.getGreen() * 255);
        int b = (int) Math.round(color.getBlue() * 255);
        return String.format("#%02x%02x%02x", r, g, b);
    }

    @FXML
    private void handleDarkPreset() {
        applyPreset(ColorScheme.darkPreset());
    }

    @FXML
    private void handleLightPreset() {
        applyPreset(ColorScheme.lightPreset());
    }

    private void applyPreset(ColorScheme preset) {
        colorScheme = preset;
        colorSchemeDB.update(colorScheme);

        suppressColorListeners = true;
        backgroundPicker.setValue(Color.web(colorScheme.getBackground()));
        surfacePicker.setValue(Color.web(colorScheme.getSurface()));
        textPrimaryPicker.setValue(Color.web(colorScheme.getTextPrimary()));
        textSecondaryPicker.setValue(Color.web(colorScheme.getTextSecondary()));
        accentPicker.setValue(Color.web(colorScheme.getAccent()));
        buttonPicker.setValue(Color.web(colorScheme.getButton()));
        buttonHoverPicker.setValue(Color.web(colorScheme.getButtonHover()));
        dangerPicker.setValue(Color.web(colorScheme.getDanger()));
        borderPicker.setValue(Color.web(colorScheme.getBorder()));
        suppressColorListeners = false;

        MainWindow.applyColorScheme(rootPane.getScene(), colorScheme);
    }

    @FXML
    private void handleAddStat() {
        AddStatDialogController.prompt(statsListView.getScene().getWindow()).ifPresent(spec -> {
            List<StatSpec> specs = new ArrayList<>(settings.getStatSpecs());
            if (!specs.contains(spec)) {
                specs.add(spec);
                settings.setStatSpecs(specs);
                settingsDB.update(settings);
                refreshStatsList();
            }
        });
    }

    private void removeStat(StatSpec spec) {
        List<StatSpec> specs = new ArrayList<>(settings.getStatSpecs());
        specs.remove(spec);
        settings.setStatSpecs(specs);
        settingsDB.update(settings);
        refreshStatsList();
    }

    private void refreshStatsList() {
        statsListView.setItems(FXCollections.observableArrayList(settings.getStatSpecs()));
    }

    private class StatCell extends ListCell<StatSpec> {
        private final Label nameLabel = new Label();
        private final Button removeButton = new Button("Remove");
        private final HBox root;

        StatCell() {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            root = new HBox(8, nameLabel, spacer, removeButton);
            root.setAlignment(Pos.CENTER_LEFT);

            removeButton.setOnAction(e -> removeStat(getItem()));
            removeButton.addEventFilter(MouseEvent.MOUSE_CLICKED, Event::consume);
        }

        @Override
        protected void updateItem(StatSpec spec, boolean empty) {
            super.updateItem(spec, empty);
            if (empty || spec == null) {
                setGraphic(null);
            } else {
                nameLabel.setText(spec.label());
                setGraphic(root);
            }
        }
    }
}
