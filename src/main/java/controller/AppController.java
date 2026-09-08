package controller;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import model.Cube;
import model.Session;
import ui.CubeViewController;
import ui.SessionViewController;
import ui.TimerViewController;

public class AppController {

    // Each entry knows how to redisplay itself without pushing anything further onto the stack —
    // used only by the global Back arrow, so "back" never grows the history and can't loop.
    private sealed interface ViewState {
        void restore(AppController app);
    }

    private record CubesState() implements ViewState {
        public void restore(AppController app) { app.loadCubeSelect(); }
    }

    private record SessionsState(Cube cube) implements ViewState {
        public void restore(AppController app) { app.loadSessions(cube); }
    }

    private record TimerState(Session session) implements ViewState {
        public void restore(AppController app) { app.loadTimer(session); }
    }

    private record SettingsState() implements ViewState {
        public void restore(AppController app) { app.loadSettings(); }
    }

    @FXML
    private StackPane contentArea;

    private final Deque<ViewState> backStack = new ArrayDeque<>();
    private ViewState currentState;

    @FXML
    private void initialize() {
        currentState = new CubesState();
        loadCubeSelect();
    }

    @FXML
    private void showHome() {
        navigateTo(new CubesState());
    }

    @FXML
    private void showSettings() {
        navigateTo(new SettingsState());
    }

    @FXML
    private void handleBack() {
        if (backStack.isEmpty()) {
            return;
        }
        currentState = backStack.pop();
        currentState.restore(this);
    }

    public void showCubeSelect() {
        navigateTo(new CubesState());
    }

    public void showSessions(Cube cube) {
        navigateTo(new SessionsState(cube));
    }

    public void showTimer(Session session) {
        navigateTo(new TimerState(session));
    }

    // Pushes whatever's currently showing onto the back stack, then displays the new state —
    // every forward navigation (tabs, drill-down clicks) goes through this so Back always has
    // somewhere to return to; handleBack() itself calls restore() directly and must never re-enter here.
    private void navigateTo(ViewState newState) {
        backStack.push(currentState);
        currentState = newState;
        newState.restore(this);
    }

    private void loadCubeSelect() {
        CubeViewController controller = (CubeViewController) loadView("/fxml/CubeSelectView.fxml");
        controller.setAppController(this);
    }

    private void loadSessions(Cube cube) {
        SessionViewController controller = (SessionViewController) loadView("/fxml/SessionView.fxml");
        controller.setAppController(this);
        controller.setCube(cube);
    }

    private void loadTimer(Session session) {
        TimerViewController controller = (TimerViewController) loadView("/fxml/TimerView.fxml");
        controller.setAppController(this);
        controller.setSession(session);
    }

    private void loadSettings() {
        loadView("/fxml/SettingsView.fxml");
    }

    private Object loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
            return loader.getController();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load view: " + fxmlPath, e);
        }
    }
}
