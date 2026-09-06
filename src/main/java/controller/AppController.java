package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class AppController {

    @FXML
    private StackPane contentArea;

    @FXML
    private void initialize() {
        showPlaceholder("Cube Select View — coming in Step 4");
    }

    @FXML
    private void showCubes() {
        showPlaceholder("Cube Select View — coming in Step 4");
    }

    @FXML
    private void showSettings() {
        showPlaceholder("Settings View — coming in Step 7");
    }

    private void showPlaceholder(String text) {
        contentArea.getChildren().setAll(new Label(text));
    }
}
