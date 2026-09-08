package ui;

import java.io.IOException;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import db.ColorSchemeDB;

public class NameDialogController {

    @FXML
    private Label promptLabel;

    @FXML
    private TextField nameField;

    private Stage stage;
    private String result;

    @FXML
    private void handleOk() {
        String value = nameField.getText() == null ? "" : nameField.getText().trim();
        if (!value.isEmpty()) {
            result = value;
            stage.close();
        }
    }

    @FXML
    private void handleCancel() {
        result = null;
        stage.close();
    }

    public static Optional<String> prompt(Window owner, String title, String promptText, String initialValue) {
        try {
            FXMLLoader loader = new FXMLLoader(NameDialogController.class.getResource("/fxml/NameDialogView.fxml"));
            Parent root = loader.load();
            NameDialogController controller = loader.getController();
            controller.promptLabel.setText(promptText);
            controller.nameField.setText(initialValue);

            Stage stage = new Stage();
            controller.stage = stage;
            stage.setTitle(title);
            stage.initOwner(owner);
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root);
            MainWindow.applyColorScheme(scene, new ColorSchemeDB().get());
            stage.setScene(scene);

            stage.showAndWait();
            return Optional.ofNullable(controller.result);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load NameDialogView", e);
        }
    }
}
