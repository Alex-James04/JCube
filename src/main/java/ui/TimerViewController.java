package ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import controller.AppController;
import controller.SolveCsvService;
import controller.StatsService;
import controller.TimeController;
import controller.TimeFormatter;
import db.SettingsDB;
import db.SolveDB;
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

    @FXML
    private VBox timerPane;

    @FXML
    private Label viewingLabel;

    @FXML
    private Button deleteButton;

    private final SolveDB solveDB = new SolveDB();
    private final StatsService statsService = new StatsService();
    private final SolveCsvService csvService = new SolveCsvService();

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

    // Non-null while the timer's main pane is showing a past solve's details instead of the live
    // timer (single-click a history row); cleared on deselect, on starting a new solve, or on a
    // click outside the history list / timer pane.
    private Solve selectedSolve;
    private List<Solve> lastLoadedSolves = new ArrayList<>();

    // Per-spec rolling series (oldest-first, aligned with settings.getStatSpecs()), recomputed
    // once per refresh() so HistoryCell can look up "value of this stat as of this solve" without
    // recomputing anything per-cell.
    private Map<StatSpec, List<Optional<Long>>> rollingSeriesBySpec = new HashMap<>();

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
        historyListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> setSelectedSolve(newValue));

        // Clicking anywhere that isn't re-selecting a history row drops back to the live timer
        // view — clicks on the toolbar/label/scramble/stats area all fall through to here.
        // Row re-selection is excluded because ListCell selects on mouse-press, which runs before
        // this MOUSE_CLICKED filter; without the exclusion this would immediately undo a click
        // that just selected a different row. The action buttons (+2/DNF/Delete) don't need a
        // similar exclusion — their onAction fires on mouse-release, strictly before this filter
        // runs, and they explicitly clear the selection themselves afterward anyway.
        rootPane.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (selectedSolve == null) {
                return;
            }
            Node target = event.getTarget() instanceof Node node ? node : null;
            if (ViewUtils.isDescendant(historyListView, target)) {
                return;
            }
            historyListView.getSelectionModel().clearSelection();
        });

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
    private void handleExport() {
        historyListView.getSelectionModel().clearSelection();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Solves");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        chooser.setInitialFileName(session.getName() + ".csv");
        File file = chooser.showSaveDialog(rootPane.getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            String content = csvService.export(solveDB.findBySessionId(session.getId()));
            Files.writeString(file.toPath(), content);
        } catch (IOException e) {
            showError("Failed to export: " + e.getMessage());
        }
    }

    @FXML
    private void handleImport() {
        historyListView.getSelectionModel().clearSelection();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Solves");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV/text files", "*.csv", "*.txt"));
        File file = chooser.showOpenDialog(rootPane.getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            String content = Files.readString(file.toPath());
            List<Solve> imported = csvService.parse(content, session.getId());
            for (Solve solve : imported) {
                solveDB.insert(solve);
            }
            refresh();
        } catch (IOException e) {
            showError("Failed to read file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            showError("Failed to parse file: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    // While viewing a past solve these act on it, then return to the live view (clearing the
    // selection explicitly here rather than relying on the click-away filter's timing); otherwise
    // they act on the just-completed live solve, exactly as before.
    @FXML
    private void handlePlus2() {
        Solve target = selectedSolve != null ? selectedSolve : lastSolve;
        if (target != null) {
            togglePenalty(target, Penalty.PLUS2);
        }
        historyListView.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleDnf() {
        Solve target = selectedSolve != null ? selectedSolve : lastSolve;
        if (target != null) {
            togglePenalty(target, Penalty.DNF);
        }
        historyListView.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleDeleteSelected() {
        if (selectedSolve != null) {
            deleteSolve(selectedSolve);
        }
        historyListView.getSelectionModel().clearSelection();
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
            lastResultText = formatSolveTime(solve);
            timeLabel.setText(lastResultText);
        }
        refresh();
    }

    private void deleteSolve(Solve solve) {
        confirmThenRun("Delete this solve?", () -> {
            solveDB.delete(solve.getId());
            if (lastSolve != null && solve.getId() == lastSolve.getId()) {
                lastSolve = null;
                lastResultText = "0.00";
            }
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

    private void onSolveCompleted(long timeMs, Penalty penalty) {
        Solve solve = new Solve(session.getId(), timeMs, currentScramble);
        solve.setPenalty(penalty);
        solveDB.insert(solve);
        lastSolve = solve;
        lastResultText = formatSolveTime(solve);

        currentScramble = ScrambleGenerator.generateScramble();
        scrambleLabel.setText(currentScramble);
        refresh();
    }

    private void onStateChanged(TimeController.State newState) {
        // Starting a new solve always returns to the live view — otherwise the timer would keep
        // running behind a frozen "past solve" display the user forgot to click away from.
        if ((newState == TimeController.State.INSPECTION || newState == TimeController.State.RUNNING)
                && !historyListView.getSelectionModel().isEmpty()) {
            historyListView.getSelectionModel().clearSelection();
        }
        updateActionButtonsEnabled();
    }

    private void refresh() {
        List<Solve> solves = solveDB.findBySessionId(session.getId());
        lastLoadedSolves = solves;
        rollingSeriesBySpec = new HashMap<>();
        for (StatSpec spec : settings.getStatSpecs()) {
            rollingSeriesBySpec.put(spec, statsService.rollingSeries(spec, solves));
        }
        refreshHistoryList(solves);
        // refreshHistoryList's reselection only re-renders the timer pane when the selection
        // actually changes; this covers the live-view case (e.g. current/best stats after a new
        // solve) where the selection stays empty throughout.
        renderTimerPane();
    }

    // solves is oldest-first (as SolveDB returns it); the history list shows newest-first, but
    // each row's displayed solve number is still its true chronological position.
    private void refreshHistoryList(List<Solve> solves) {
        List<Solve> newestFirst = new ArrayList<>(solves);
        Collections.reverse(newestFirst);

        int previousId = selectedSolve != null ? selectedSolve.getId() : -1;
        historyListView.setItems(FXCollections.observableArrayList(newestFirst));
        if (previousId != -1) {
            for (Solve solve : newestFirst) {
                if (solve.getId() == previousId) {
                    historyListView.getSelectionModel().select(solve);
                    break;
                }
            }
        }
    }

    private void setSelectedSolve(Solve solve) {
        selectedSolve = solve;
        renderTimerPane();
    }

    // The timer pane always shows exactly one thing: either the live timer/current stats, or
    // whichever past solve is selected in the history list — never both, so nothing is duplicated
    // on screen.
    private void renderTimerPane() {
        if (selectedSolve != null) {
            renderSelectedSolve(selectedSolve);
        } else {
            renderLiveView();
        }
        updateActionButtonsEnabled();
    }

    private void updateActionButtonsEnabled() {
        if (selectedSolve != null) {
            plus2Button.setDisable(false);
            dnfButton.setDisable(false);
        } else {
            boolean stopped = timeController.getState() == TimeController.State.STOPPED;
            plus2Button.setDisable(!stopped);
            dnfButton.setDisable(!stopped);
        }
        deleteButton.setVisible(selectedSolve != null);
        deleteButton.setManaged(selectedSolve != null);
    }

    private void renderLiveView() {
        viewingLabel.setVisible(false);
        viewingLabel.setManaged(false);

        scrambleLabel.setText(currentScramble);
        scrambleLabel.setVisible(settings.isShowScramble());
        scrambleLabel.setManaged(settings.isShowScramble());

        updateTimeLabel();
        refreshStatsRow(lastLoadedSolves);
    }

    private void refreshStatsRow(List<Solve> solves) {
        statsBox.getChildren().clear();
        for (StatSpec spec : settings.getStatSpecs()) {
            Optional<Long> current = statsService.compute(spec, solves);
            Optional<Long> best = statsService.bestOf(spec, solves);

            Label currentLabel = new Label(spec.label() + ": " + formatStat(current));
            currentLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            Label bestLabel = new Label("best: " + formatStat(best));
            bestLabel.getStyleClass().add("label-secondary");

            VBox statBox = new VBox(2, currentLabel, bestLabel);
            statBox.setAlignment(Pos.CENTER);
            statsBox.getChildren().add(statBox);
        }
    }

    // Fills the same scramble/time/stats area the live view uses (rather than a separate section)
    // with this past solve's details, including its scramble regardless of the "show scramble"
    // setting — that setting exists to avoid look-ahead on the *upcoming* solve, not to hide
    // history you're deliberately reviewing.
    private void renderSelectedSolve(Solve solve) {
        int number = historyListView.getItems().size() - historyListView.getItems().indexOf(solve);

        // The solve number lives here, in the small secondary line, rather than prefixing the
        // giant time label — the time is the thing worth seeing at a glance; the number is just
        // context.
        viewingLabel.setText("Viewing solve #" + number + " — click elsewhere to return to the timer");
        viewingLabel.setVisible(true);
        viewingLabel.setManaged(true);

        scrambleLabel.setText(solve.getScramble() == null || solve.getScramble().isBlank()
                ? "No scramble recorded"
                : solve.getScramble());
        scrambleLabel.setVisible(true);
        scrambleLabel.setManaged(true);

        timeLabel.setText(formatSolveTime(solve));

        statsBox.getChildren().clear();
        int seriesIndex = number - 1;
        for (StatSpec spec : settings.getStatSpecs()) {
            List<Optional<Long>> series = rollingSeriesBySpec.get(spec);
            Optional<Long> value = (series != null && seriesIndex >= 0 && seriesIndex < series.size())
                    ? series.get(seriesIndex)
                    : Optional.empty();
            Label statLabel = new Label(spec.label() + ": " + formatStat(value));
            statLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            statsBox.getChildren().add(statLabel);
        }
    }

    private void updateTimeLabel() {
        if (selectedSolve != null) {
            return;
        }
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

    private String formatMs(long ms) {
        return TimeFormatter.format(ms, settings.getDecimalPlaces());
    }

    private String formatStat(Optional<Long> value) {
        return value.map(this::formatMs).orElse("-");
    }

    // Aggregate stats (ao5/mean/...) can legitimately show a bare "DNF" when the whole average is
    // wiped out, but a single solve's own recorded time should stay visible even when it's been
    // marked +2 or DNF — otherwise there's no way to tell what actually happened, or to notice a
    // DNF short of re-toggling it. +2 shows the already-penalized total (matching WCA convention);
    // DNF shows the raw time it would have counted as, since its "effective" time is meaningless.
    private String formatSolveTime(Solve solve) {
        return switch (solve.getPenalty()) {
            case NONE -> formatMs(solve.getTimeMs());
            case PLUS2 -> formatMs(solve.getEffectiveTimeMs()) + " (+2)";
            case DNF -> formatMs(solve.getTimeMs()) + " (DNF)";
        };
    }

    private class HistoryCell extends ListCell<Solve> {
        private final Label numberLabel = new Label();
        private final Label cellTimeLabel = new Label();
        private final HBox statsRow = new HBox(12);
        private final HBox root;

        HistoryCell() {
            // The number is context, not the point — small and secondary. The time is what a
            // solver actually scans the list for, so it gets the size and weight.
            numberLabel.setStyle("-fx-font-size: 13px;");
            numberLabel.getStyleClass().add("label-secondary");
            cellTimeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            statsRow.setAlignment(Pos.CENTER_RIGHT);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox infoBox = new HBox(6, numberLabel, cellTimeLabel);
            infoBox.setAlignment(Pos.BASELINE_LEFT);

            root = new HBox(12, infoBox, spacer, statsRow);
            root.setAlignment(Pos.CENTER_LEFT);
        }

        @Override
        protected void updateItem(Solve solve, boolean empty) {
            super.updateItem(solve, empty);
            if (empty || solve == null) {
                setGraphic(null);
                setContextMenu(null);
            } else {
                int number = getListView().getItems().size() - getIndex();
                numberLabel.setText("#" + number);
                cellTimeLabel.setText(formatSolveTime(solve));

                statsRow.getChildren().clear();
                // rollingSeriesBySpec is oldest-first, so the chronological index (number - 1)
                // looks up this solve's stat value as of when it was solved.
                int seriesIndex = number - 1;
                for (StatSpec spec : settings.getStatSpecs()) {
                    List<Optional<Long>> series = rollingSeriesBySpec.get(spec);
                    Optional<Long> value = (series != null && seriesIndex >= 0 && seriesIndex < series.size())
                            ? series.get(seriesIndex)
                            : Optional.empty();
                    Label statLabel = new Label(spec.label() + ": " + formatStat(value));
                    statLabel.getStyleClass().add("label-secondary");
                    statsRow.getChildren().add(statLabel);
                }

                MenuItem plus2Item = new MenuItem("+2");
                plus2Item.setOnAction(e -> togglePenalty(solve, Penalty.PLUS2));
                MenuItem dnfItem = new MenuItem("DNF");
                dnfItem.setOnAction(e -> togglePenalty(solve, Penalty.DNF));
                MenuItem deleteItem = new MenuItem("Delete");
                deleteItem.setOnAction(e -> deleteSolve(solve));
                setContextMenu(new ContextMenu(plus2Item, dnfItem, deleteItem));

                setGraphic(root);
            }
        }
    }
}
