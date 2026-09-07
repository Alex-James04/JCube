package ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import controller.AppController;
import controller.StatsService;
import controller.TimeController;
import db.CubeDB;
import db.SettingsDB;
import db.SolveDB;
import model.Cube;
import model.InspectionMode;
import model.Penalty;
import model.ScrambleGenerator;
import model.Session;
import model.Settings;
import model.Solve;
import model.StatSpec;

public class TimerViewController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private Label headerLabel;

    @FXML
    private Label scrambleLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private HBox statsBox;

    @FXML
    private Button plus2Button;

    @FXML
    private Button dnfButton;

    @FXML
    private ListView<Solve> historyListView;

    private final SolveDB solveDB = new SolveDB();
    private final CubeDB cubeDB = new CubeDB();
    private final StatsService statsService = new StatsService();

    private final EventHandler<KeyEvent> keyPressedHandler = this::handleKeyPressed;
    private final EventHandler<KeyEvent> keyReleasedHandler = this::handleKeyReleased;

    private final AnimationTimer ticker = new AnimationTimer() {
        @Override
        public void handle(long now) {
            long nowMs = System.currentTimeMillis();
            timeController.tick(nowMs);
            updateTimeLabel();
        }
    };

    private AppController appController;
    private Session session;
    private Settings settings;
    private TimeController timeController;

    private String currentScramble = "";
    private String lastResultText = "0.00";
    private Solve lastSolve;
    private boolean spaceDown;

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    public void setSession(Session session) {
        this.session = session;
        headerLabel.setText("Timer — " + session.getName());
        currentScramble = ScrambleGenerator.generateScramble();
        scrambleLabel.setText(currentScramble);
        refresh();
    }

    @FXML
    private void initialize() {
        settings = new SettingsDB().get();
        timeController = new TimeController(settings.getSpacebarMode(), settings.getInspectionMode(),
                this::onStateChanged, this::onSolveCompleted);

        plus2Button.setDisable(true);
        dnfButton.setDisable(true);
        historyListView.setCellFactory(list -> new HistoryCell());

        scrambleLabel.setVisible(settings.isShowScramble());
        scrambleLabel.setManaged(settings.isShowScramble());

        // The Scene is shared and long-lived across view swaps, so key filters and the ticker
        // must be attached/detached as this view enters/leaves it, or repeated visits pile up
        // duplicate handlers and leaked AnimationTimers still reacting after navigating away.
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) {
                oldScene.removeEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
                oldScene.removeEventFilter(KeyEvent.KEY_RELEASED, keyReleasedHandler);
            }
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
                newScene.addEventFilter(KeyEvent.KEY_RELEASED, keyReleasedHandler);
            } else {
                ticker.stop();
            }
        });

        ticker.start();
    }

    @FXML
    private void handleBack() {
        Cube cube = cubeDB.findById(session.getCubeId()).orElseThrow();
        appController.showSessions(cube);
    }

    @FXML
    private void handlePlus2() {
        if (lastSolve != null) {
            togglePenalty(lastSolve, Penalty.PLUS2);
        }
    }

    @FXML
    private void handleDnf() {
        if (lastSolve != null) {
            togglePenalty(lastSolve, Penalty.DNF);
        }
    }

    // The OS repeats KEY_PRESSED continuously while a key is held; without this de-bounce, each
    // repeat would reset the WCA hold-start timestamp and the hold-to-arm threshold could never
    // be reached.
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() != KeyCode.SPACE) {
            return;
        }
        event.consume();
        if (!spaceDown) {
            spaceDown = true;
            timeController.onSpacebarPressed(System.currentTimeMillis());
        }
    }

    private void handleKeyReleased(KeyEvent event) {
        if (event.getCode() != KeyCode.SPACE) {
            return;
        }
        event.consume();
        spaceDown = false;
        timeController.onSpacebarReleased(System.currentTimeMillis());
    }

    // Takes the target Solve explicitly (rather than always acting on lastSolve) so history rows
    // can toggle penalties on any past solve, not just the one just completed.
    private void togglePenalty(Solve solve, Penalty penalty) {
        Penalty newPenalty = solve.getPenalty() == penalty ? Penalty.NONE : penalty;
        solve.setPenalty(newPenalty);
        solveDB.updatePenalty(solve.getId(), newPenalty);
        if (lastSolve != null && solve.getId() == lastSolve.getId()) {
            lastSolve = solve;
            lastResultText = formatMs(solve.getEffectiveTimeMs());
            timeLabel.setText(lastResultText);
        }
        refresh();
    }

    private void deleteSolve(Solve solve) {
        Alert alert = new Alert(AlertType.CONFIRMATION, "Delete this solve?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.showAndWait().filter(ButtonType.YES::equals).ifPresent(response -> {
            solveDB.delete(solve.getId());
            if (lastSolve != null && solve.getId() == lastSolve.getId()) {
                lastSolve = null;
                lastResultText = "0.00";
                timeLabel.setText(lastResultText);
                plus2Button.setDisable(true);
                dnfButton.setDisable(true);
            }
            refresh();
        });
    }

    private void onSolveCompleted(long timeMs, Penalty penalty) {
        Solve solve = new Solve(session.getId(), timeMs, currentScramble);
        solve.setPenalty(penalty);
        solveDB.insert(solve);
        lastSolve = solve;
        lastResultText = formatMs(solve.getEffectiveTimeMs());

        currentScramble = ScrambleGenerator.generateScramble();
        scrambleLabel.setText(currentScramble);
        refresh();
    }

    private void onStateChanged(TimeController.State newState) {
        boolean stopped = newState == TimeController.State.STOPPED;
        plus2Button.setDisable(!stopped);
        dnfButton.setDisable(!stopped);
    }

    private void refresh() {
        List<Solve> solves = solveDB.findBySessionId(session.getId());
        refreshStatsRow(solves);
        refreshHistoryList(solves);
    }

    private void refreshStatsRow(List<Solve> solves) {
        statsBox.getChildren().clear();
        for (StatSpec spec : settings.getStatSpecs()) {
            Optional<Long> value = statsService.compute(spec, solves);
            statsBox.getChildren().add(new Label(spec.label() + ": " + formatStat(value)));
        }
    }

    // solves is oldest-first (as SolveDB returns it); the history list shows newest-first, but
    // each row's displayed solve number is still its true chronological position.
    private void refreshHistoryList(List<Solve> solves) {
        List<Solve> newestFirst = new ArrayList<>(solves);
        Collections.reverse(newestFirst);
        historyListView.setItems(FXCollections.observableArrayList(newestFirst));
    }

    private void updateTimeLabel() {
        long now = System.currentTimeMillis();
        switch (timeController.getState()) {
            case INSPECTION -> timeLabel.setText(inspectionDisplay(timeController.elapsedInspectionMs(now)));
            case RUNNING -> timeLabel.setText(formatMs(timeController.elapsedRunMs(now)));
            case IDLE, STOPPED -> timeLabel.setText(lastResultText);
        }
    }

    private String inspectionDisplay(long elapsedMs) {
        if (settings.getInspectionMode() != InspectionMode.WCA) {
            return formatMs(elapsedMs);
        }
        if (elapsedMs <= TimeController.INSPECTION_PLUS2_LIMIT_MS) {
            long remainingSeconds = (TimeController.INSPECTION_PLUS2_LIMIT_MS - elapsedMs + 999) / 1000;
            return String.valueOf(Math.max(remainingSeconds, 0));
        }
        return "+2";
    }

    private static String formatMs(long ms) {
        if (ms == Long.MAX_VALUE) {
            return "DNF";
        }
        long centis = Math.round(ms / 10.0);
        long minutes = centis / 6000;
        long seconds = (centis % 6000) / 100;
        long hundredths = centis % 100;
        if (minutes > 0) {
            return String.format("%d:%02d.%02d", minutes, seconds, hundredths);
        }
        return String.format("%d.%02d", seconds, hundredths);
    }

    private static String formatStat(Optional<Long> value) {
        return value.map(TimerViewController::formatMs).orElse("-");
    }

    private class HistoryCell extends ListCell<Solve> {
        private final Label infoLabel = new Label();
        private final Button rowPlus2Button = new Button("+2");
        private final Button rowDnfButton = new Button("DNF");
        private final Button rowDeleteButton = new Button("Delete");
        private final HBox root;

        HistoryCell() {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            root = new HBox(8, infoLabel, spacer, rowPlus2Button, rowDnfButton, rowDeleteButton);
            root.setAlignment(Pos.CENTER_LEFT);

            rowPlus2Button.setOnAction(e -> togglePenalty(getItem(), Penalty.PLUS2));
            rowPlus2Button.addEventFilter(MouseEvent.MOUSE_CLICKED, Event::consume);
            rowDnfButton.setOnAction(e -> togglePenalty(getItem(), Penalty.DNF));
            rowDnfButton.addEventFilter(MouseEvent.MOUSE_CLICKED, Event::consume);
            rowDeleteButton.setOnAction(e -> deleteSolve(getItem()));
            rowDeleteButton.addEventFilter(MouseEvent.MOUSE_CLICKED, Event::consume);
        }

        @Override
        protected void updateItem(Solve solve, boolean empty) {
            super.updateItem(solve, empty);
            if (empty || solve == null) {
                setGraphic(null);
            } else {
                int number = getListView().getItems().size() - getIndex();
                infoLabel.setText("#" + number + "  " + formatMs(solve.getEffectiveTimeMs()));
                setGraphic(root);
            }
        }
    }
}
