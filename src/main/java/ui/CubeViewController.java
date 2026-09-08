package ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import controller.AppController;
import controller.StatsService;
import controller.TimeFormatter;
import db.CubeDB;
import db.SessionDB;
import db.SettingsDB;
import db.SolveDB;
import model.Cube;
import model.Session;
import model.Settings;
import model.Solve;
import model.StatSpec;

public class CubeViewController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private ListView<Cube> cubeListView;

    @FXML
    private VBox detailPane;

    private final CubeDB cubeDB = new CubeDB();
    private final SessionDB sessionDB = new SessionDB();
    private final SolveDB solveDB = new SolveDB();
    private final SettingsDB settingsDB = new SettingsDB();
    private final StatsService statsService = new StatsService();
    private AppController appController;
    private Settings settings;
    private Cube selectedCube;

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    @FXML
    private void initialize() {
        settings = settingsDB.get();
        cubeListView.setCellFactory(list -> new CubeCell());
        cubeListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> setSelectedCube(newValue));

        // Clicking anywhere that isn't re-selecting a cube (the toolbar, the header, blank space)
        // drops back to the intro. See TimerViewController's identical filter for why row
        // re-selection specifically has to be excluded.
        rootPane.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (selectedCube == null) {
                return;
            }
            Node target = event.getTarget() instanceof Node node ? node : null;
            if (ViewUtils.isDescendant(cubeListView, target)) {
                return;
            }
            cubeListView.getSelectionModel().clearSelection();
        });

        refresh();
        showIntro();
    }

    @FXML
    private void handleAddCube() {
        cubeListView.getSelectionModel().clearSelection();
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
                    reselectById(cube.getId());
                });
    }

    private void deleteCube(Cube cube) {
        confirmThenRun("Delete \"" + cube.getName() + "\" and all of its sessions and solves?", () -> {
            cubeDB.delete(cube.getId());
            refresh();
            cubeListView.getSelectionModel().clearSelection();
        });
    }

    private void reselectById(int id) {
        for (Cube cube : cubeListView.getItems()) {
            if (cube.getId() == id) {
                cubeListView.getSelectionModel().select(cube);
                return;
            }
        }
        cubeListView.getSelectionModel().clearSelection();
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

    private void setSelectedCube(Cube cube) {
        selectedCube = cube;
        if (cube == null) {
            showIntro();
        } else {
            showDetail(cube);
        }
    }

    // General orientation for a first-time (or cube-less) visitor — shown until something is
    // selected, matching the Timer screen's live-view-by-default pattern.
    private void showIntro() {
        detailPane.getChildren().clear();

        Label title = new Label("Welcome to JCube");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");

        Label body = new Label(
                "JCube is a speedcubing timer and practice tracker.\n\n"
                + "Each Cube represents a puzzle you practice, like a 3x3. Inside a cube, you create "
                + "Sessions to organize your practice — for example, one per day or one per method — "
                + "and every session tracks each individual solve: its time, scramble, and any +2 or "
                + "DNF penalty, along with rolling stats like ao5, ao12, mean, and personal best.\n\n"
                + "Select a cube on the left to see its overall stats here, or double-click it to open "
                + "its sessions. Use \"Add Cube\" to create a new one, and right-click any cube to "
                + "rename or delete it. Visit Settings to customize the timer's mechanics, the stats "
                + "shown, decimal precision, and the app's color scheme.");
        body.setWrapText(true);
        body.setMaxWidth(520);
        body.getStyleClass().add("label-secondary");

        VBox intro = new VBox(16, title, body);
        intro.setAlignment(Pos.CENTER);
        intro.setMaxWidth(560);
        detailPane.getChildren().add(intro);
    }

    private void showDetail(Cube cube) {
        detailPane.getChildren().clear();

        List<Session> sessions = sessionDB.findByCubeId(cube.getId());
        List<Solve> allSolves = new ArrayList<>();
        for (Session session : sessions) {
            allSolves.addAll(solveDB.findBySessionId(session.getId()));
        }

        Optional<Long> bestSingle = statsService.personalBest(allSolves);
        Optional<Long> bestAo5 = statsService.bestOf(StatSpec.average(5), allSolves);
        Optional<Long> bestAo12 = statsService.bestOf(StatSpec.average(12), allSolves);

        Label nameLabel = new Label(cube.getName());
        nameLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");

        HBox statsRow = new HBox(28,
                statBlock("Sessions", String.valueOf(sessions.size())),
                statBlock("Total Solves", String.valueOf(allSolves.size())),
                statBlock("Best Single", formatStat(bestSingle)),
                statBlock("Best ao5", formatStat(bestAo5)),
                statBlock("Best ao12", formatStat(bestAo12)));
        statsRow.setAlignment(Pos.CENTER);

        detailPane.getChildren().addAll(nameLabel, statsRow);
    }

    private VBox statBlock(String label, String value) {
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        Label captionLabel = new Label(label);
        captionLabel.getStyleClass().add("label-secondary");
        VBox box = new VBox(4, valueLabel, captionLabel);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private String formatStat(Optional<Long> value) {
        return value.map(ms -> TimeFormatter.format(ms, settings.getDecimalPlaces())).orElse("-");
    }

    private class CubeCell extends ListCell<Cube> {
        CubeCell() {
            setOnMouseClicked(event -> {
                if (getItem() != null && event.getClickCount() == 2) {
                    appController.showSessions(getItem());
                }
            });
        }

        @Override
        protected void updateItem(Cube cube, boolean empty) {
            super.updateItem(cube, empty);
            if (empty || cube == null) {
                setText(null);
                setContextMenu(null);
            } else {
                setText(cube.getName());

                MenuItem rename = new MenuItem("Rename");
                rename.setOnAction(e -> renameCube(cube));
                MenuItem delete = new MenuItem("Delete");
                delete.setOnAction(e -> deleteCube(cube));
                setContextMenu(new ContextMenu(rename, delete));
            }
        }
    }
}
