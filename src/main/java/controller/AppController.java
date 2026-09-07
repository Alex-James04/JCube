package controller;

import java.io.IOException;

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

    @FXML
    private StackPane contentArea;

    @FXML
    private void initialize() {
        showCubeSelect();
    }

    @FXML
    private void showCubes() {
        showCubeSelect();
    }

    @FXML
    private void showSettings() {
        loadView("/fxml/SettingsView.fxml");
    }

    public void showCubeSelect() {
        CubeViewController controller = (CubeViewController) loadView("/fxml/CubeSelectView.fxml");
        controller.setAppController(this);
    }

    public void showSessions(Cube cube) {
        SessionViewController controller = (SessionViewController) loadView("/fxml/SessionView.fxml");
        controller.setAppController(this);
        controller.setCube(cube);
    }

    public void showTimer(Session session) {
        TimerViewController controller = (TimerViewController) loadView("/fxml/TimerView.fxml");
        controller.setAppController(this);
        controller.setSession(session);
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
