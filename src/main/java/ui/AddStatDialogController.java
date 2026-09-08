package ui;

import java.io.IOException;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import db.ColorSchemeDB;
import model.StatSpec;

public class AddStatDialogController {

    private static final String AVERAGE_OPTION = "Average (aoN)";
    private static final String MEAN_OPTION = "Mean";
    private static final String PB_OPTION = "PB";

    @FXML
    private ChoiceBox<String> typeChoice;

    @FXML
    private Label windowSizeLabel;

    @FXML
    private TextField windowSizeField;

    @FXML
    private Label errorLabel;

    private Stage stage;
    private StatSpec result;

    @FXML
    private void initialize() {
        typeChoice.getItems().addAll(AVERAGE_OPTION, MEAN_OPTION, PB_OPTION);
        typeChoice.setValue(AVERAGE_OPTION);
        typeChoice.valueProperty().addListener((obs, oldValue, newValue) -> updateWindowSizeVisibility());
        updateWindowSizeVisibility();
    }

    private void updateWindowSizeVisibility() {
        boolean isAverage = AVERAGE_OPTION.equals(typeChoice.getValue());
        windowSizeLabel.setVisible(isAverage);
        windowSizeLabel.setManaged(isAverage);
        windowSizeField.setVisible(isAverage);
        windowSizeField.setManaged(isAverage);
    }

    @FXML
    private void handleAdd() {
        errorLabel.setText("");
        try {
            result = switch (typeChoice.getValue()) {
                case MEAN_OPTION -> StatSpec.mean();
                case PB_OPTION -> StatSpec.pb();
                default -> StatSpec.average(Integer.parseInt(windowSizeField.getText().trim()));
            };
            stage.close();
        } catch (NumberFormatException e) {
            errorLabel.setText("Window size must be a whole number.");
        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        result = null;
        stage.close();
    }

    public static Optional<StatSpec> prompt(Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(AddStatDialogController.class.getResource("/fxml/AddStatDialogView.fxml"));
            Parent root = loader.load();
            AddStatDialogController controller = loader.getController();

            Stage stage = new Stage();
            controller.stage = stage;
            stage.setTitle("Add Stat");
            stage.initOwner(owner);
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root);
            MainWindow.applyColorScheme(scene, new ColorSchemeDB().get());
            stage.setScene(scene);

            stage.showAndWait();
            return Optional.ofNullable(controller.result);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load AddStatDialogView", e);
        }
    }
}
