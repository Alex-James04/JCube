package ui;

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
import db.SessionDB;
import db.SettingsDB;
import db.SolveDB;
import model.Cube;
import model.Session;
import model.Settings;
import model.Solve;
import model.StatSpec;

public class SessionViewController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private Label headerLabel;

    @FXML
    private ListView<Session> sessionListView;

    @FXML
    private VBox detailPane;

    private final SessionDB sessionDB = new SessionDB();
    private final SolveDB solveDB = new SolveDB();
    private final SettingsDB settingsDB = new SettingsDB();
    private final StatsService statsService = new StatsService();
    private AppController appController;
    private Cube cube;
    private Settings settings;
    private Session selectedSession;

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    public void setCube(Cube cube) {
        this.cube = cube;
        headerLabel.setText("Sessions — " + cube.getName());
        refresh();
        showIntro();
    }

    @FXML
    private void initialize() {
        settings = settingsDB.get();
        sessionListView.setCellFactory(list -> new SessionCell());
        sessionListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> setSelectedSession(newValue));

        // Clicking anywhere that isn't re-selecting a session (the toolbar, the header, blank
        // space) drops back to the intro. See TimerViewController's identical filter for why row
        // re-selection specifically has to be excluded.
        rootPane.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (selectedSession == null) {
                return;
            }
            Node target = event.getTarget() instanceof Node node ? node : null;
            if (ViewUtils.isDescendant(sessionListView, target)) {
                return;
            }
            sessionListView.getSelectionModel().clearSelection();
        });
    }

    @FXML
    private void handleAddSession() {
        sessionListView.getSelectionModel().clearSelection();
        NameDialogController.prompt(sessionListView.getScene().getWindow(), "New Session", "Session name:", "")
                .ifPresent(name -> {
                    sessionDB.insert(new Session(cube.getId(), name));
                    refresh();
                });
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
                    reselectById(session.getId());
                });
    }

    private void deleteSession(Session session) {
        confirmThenRun("Delete \"" + session.getName() + "\" and all of its solves?", () -> {
            sessionDB.delete(session.getId());
            refresh();
            sessionListView.getSelectionModel().clearSelection();
        });
    }

    private void reselectById(int id) {
        for (Session session : sessionListView.getItems()) {
            if (session.getId() == id) {
                sessionListView.getSelectionModel().select(session);
                return;
            }
        }
        sessionListView.getSelectionModel().clearSelection();
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

    private void setSelectedSession(Session session) {
        selectedSession = session;
        if (session == null) {
            showIntro();
        } else {
            showDetail(session);
        }
    }

    // Session-specific orientation, shown until something is selected — explains what a session
    // is and how to navigate from here, mirroring the Cubes screen's general app intro.
    private void showIntro() {
        detailPane.getChildren().clear();

        Label title = new Label("Sessions — " + cube.getName());
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");

        Label body = new Label(
                "A session groups solves together — for example, one per practice day, or one per "
                + "method or event you're working on.\n\n"
                + "Select a session on the left to see its stats here: best single, ao5, ao12, mean, "
                + "and solve count. Double-click a session to open the timer and start solving.\n\n"
                + "Use \"Add Session\" to create a new one for this cube, and right-click any session "
                + "to rename or delete it.");
        body.setWrapText(true);
        body.setMaxWidth(520);
        body.getStyleClass().add("label-secondary");

        VBox intro = new VBox(16, title, body);
        intro.setAlignment(Pos.CENTER);
        intro.setMaxWidth(560);
        detailPane.getChildren().add(intro);
    }

    private void showDetail(Session session) {
        detailPane.getChildren().clear();

        List<Solve> solves = solveDB.findBySessionId(session.getId());
        Optional<Long> best = statsService.personalBest(solves);
        Optional<Long> ao5 = statsService.bestOf(StatSpec.average(5), solves);
        Optional<Long> ao12 = statsService.bestOf(StatSpec.average(12), solves);
        Optional<Long> mean = statsService.mean(solves);

        Label nameLabel = new Label(session.getName());
        nameLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");

        Label createdLabel = new Label(
                "Created " + (session.getCreatedAt() == null ? "-" : session.getCreatedAt().toString()));
        createdLabel.getStyleClass().add("label-secondary");

        HBox statsRow = new HBox(28,
                statBlock("Solves", String.valueOf(solves.size())),
                statBlock("Best Single", formatStat(best)),
                statBlock("Best ao5", formatStat(ao5)),
                statBlock("Best ao12", formatStat(ao12)),
                statBlock("Mean", formatStat(mean)));
        statsRow.setAlignment(Pos.CENTER);

        detailPane.getChildren().addAll(nameLabel, createdLabel, statsRow);
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

    private class SessionCell extends ListCell<Session> {
        SessionCell() {
            setOnMouseClicked(event -> {
                if (getItem() != null && event.getClickCount() == 2) {
                    appController.showTimer(getItem());
                }
            });
        }

        @Override
        protected void updateItem(Session session, boolean empty) {
            super.updateItem(session, empty);
            if (empty || session == null) {
                setText(null);
                setContextMenu(null);
            } else {
                setText(session.getName());

                MenuItem rename = new MenuItem("Rename");
                rename.setOnAction(e -> renameSession(session));
                MenuItem delete = new MenuItem("Delete");
                delete.setOnAction(e -> deleteSession(session));
                setContextMenu(new ContextMenu(rename, delete));
            }
        }
    }
}
