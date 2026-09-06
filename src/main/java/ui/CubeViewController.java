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
import db.CubeDB;
import model.Cube;

public class CubeViewController {

    @FXML
    private ListView<Cube> cubeListView;

    private final CubeDB cubeDB = new CubeDB();
    private AppController appController;

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    @FXML
    private void initialize() {
        cubeListView.setCellFactory(list -> new CubeCell());
        refresh();
    }

    @FXML
    private void handleAddCube() {
        NameDialogController.prompt(cubeListView.getScene().getWindow(), "New Cube", "Cube name:", "")
                .ifPresent(name -> {
                    cubeDB.insert(new Cube(name));
                    refresh();
                });
    }

    private void refresh() {
        List<Cube> cubes = cubeDB.findAll();
        cubeListView.setItems(FXCollections.observableArrayList(cubes));
    }

    private void renameCube(Cube cube) {
        NameDialogController.prompt(cubeListView.getScene().getWindow(), "Rename Cube", "Cube name:", cube.getName())
                .ifPresent(name -> {
                    cubeDB.updateName(cube.getId(), name);
                    refresh();
                });
    }

    private void deleteCube(Cube cube) {
        Alert alert = new Alert(AlertType.CONFIRMATION,
                "Delete \"" + cube.getName() + "\" and all of its sessions and solves?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.showAndWait().filter(ButtonType.YES::equals).ifPresent(response -> {
            cubeDB.delete(cube.getId());
            refresh();
        });
    }

    private class CubeCell extends ListCell<Cube> {
        private final Label nameLabel = new Label();
        private final Button renameButton = new Button("Rename");
        private final Button deleteButton = new Button("Delete");
        private final HBox root;

        CubeCell() {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            root = new HBox(8, nameLabel, spacer, renameButton, deleteButton);
            root.setAlignment(Pos.CENTER_LEFT);

            renameButton.setOnAction(e -> renameCube(getItem()));
            renameButton.addEventFilter(MouseEvent.MOUSE_CLICKED, Event::consume);
            deleteButton.setOnAction(e -> deleteCube(getItem()));
            deleteButton.addEventFilter(MouseEvent.MOUSE_CLICKED, Event::consume);

            setOnMouseClicked(event -> {
                if (getItem() != null) {
                    appController.showSessions(getItem());
                }
            });
        }

        @Override
        protected void updateItem(Cube cube, boolean empty) {
            super.updateItem(cube, empty);
            if (empty || cube == null) {
                setGraphic(null);
            } else {
                nameLabel.setText(cube.getName());
                setGraphic(root);
            }
        }
    }
}
