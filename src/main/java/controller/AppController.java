package controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import model.Cube;
import model.Session;
import ui.CubeViewController;
import ui.SessionViewController;

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
        showPlaceholder("Settings View — coming in Step 7");
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

    public void showTimerPlaceholder(Session session) {
        showPlaceholder("Timer View — coming in Step 6 (session: " + session.getName() + ")");
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

    private void showPlaceholder(String text) {
        contentArea.getChildren().setAll(new Label(text));
    }
}
