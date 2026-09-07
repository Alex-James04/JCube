package ui;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import controller.AppController;
import db.SessionDB;
import db.SettingsDB;
import model.Cube;
import model.Session;
import model.Settings;

public class SessionViewController {

    @FXML
    private Label headerLabel;

    @FXML
    private ListView<Session> sessionListView;

    private final SessionDB sessionDB = new SessionDB();
    private final SettingsDB settingsDB = new SettingsDB();
    private AppController appController;
    private Cube cube;
    private Settings settings;

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    public void setCube(Cube cube) {
        this.cube = cube;
        headerLabel.setText("Sessions — " + cube.getName());
        refresh();
    }

    @FXML
    private void initialize() {
        settings = settingsDB.get();
        sessionListView.setCellFactory(list -> new SessionCell());
    }

    @FXML
    private void handleAddSession() {
        NameDialogController.prompt(sessionListView.getScene().getWindow(), "New Session", "Session name:", "")
                .ifPresent(name -> {
                    sessionDB.insert(new Session(cube.getId(), name));
                    refresh();
                });
    }

    @FXML
    private void handleBack() {
        appController.showCubeSelect();
    }

    private void refresh() {
        List<Session> sessions = sessionDB.findByCubeId(cube.getId());
        sessionListView.setItems(FXCollections.observableArrayList(sessions));
    }

    private void renameSession(Session session) {
        NameDialogController.prompt(sessionListView.getScene().getWindow(), "Rename Session", "Session name:", session.getName())
                .ifPresent(name -> {
                    sessionDB.updateName(session.getId(), name);
                    refresh();
                });
    }

    private void deleteSession(Session session) {
        confirmThenRun("Delete \"" + session.getName() + "\" and all of its solves?", () -> {
            sessionDB.delete(session.getId());
            refresh();
        });
    }

    private void confirmThenRun(String message, Runnable action) {
        if (!settings.isConfirmDeletes()) {
            action.run();
            return;
        }
        Alert alert = new Alert(AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.showAndWait().filter(ButtonType.YES::equals).ifPresent(response -> action.run());
    }

    private class SessionCell extends ListCell<Session> {
        private final Label nameLabel = new Label();
        private final Button renameButton = new Button("Rename");
        private final Button deleteButton = new Button("Delete");
        private final HBox root;

        SessionCell() {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            root = new HBox(8, nameLabel, spacer, renameButton, deleteButton);
            root.setAlignment(Pos.CENTER_LEFT);

            renameButton.setOnAction(e -> renameSession(getItem()));
            renameButton.addEventFilter(MouseEvent.MOUSE_CLICKED, Event::consume);
            deleteButton.setOnAction(e -> deleteSession(getItem()));
            deleteButton.addEventFilter(MouseEvent.MOUSE_CLICKED, Event::consume);

            setOnMouseClicked(event -> {
                if (getItem() != null) {
                    appController.showTimer(getItem());
                }
            });
        }

        @Override
        protected void updateItem(Session session, boolean empty) {
            super.updateItem(session, empty);
            if (empty || session == null) {
                setGraphic(null);
            } else {
                nameLabel.setText(session.getName());
                setGraphic(root);
            }
        }
    }
}
