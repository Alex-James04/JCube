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
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import db.SettingsDB;
import model.InspectionMode;
import model.Settings;
import model.SpacebarMode;
import model.StatSpec;

public class SettingsViewController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private ChoiceBox<String> themeChoice;

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

    private final SettingsDB settingsDB = new SettingsDB();
    private Settings settings;

    @FXML
    private void initialize() {
        settings = settingsDB.get();

        themeChoice.setItems(FXCollections.observableArrayList("dark", "light"));
        themeChoice.setValue(settings.getTheme());
        themeChoice.valueProperty().addListener((obs, oldValue, newValue) -> {
            settings.setTheme(newValue);
            settingsDB.update(settings);
            MainWindow.applyTheme(rootPane.getScene(), newValue);
        });

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

        statsListView.setCellFactory(list -> new StatCell());
        refreshStatsList();
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
