package ui;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import db.SettingsDB;
import model.Settings;

public class MainWindow {

    private static final double DEFAULT_WIDTH = 900;
    private static final double DEFAULT_HEIGHT = 600;
    private static final double MIN_WIDTH = 700;
    private static final double MIN_HEIGHT = 500;

    public static void show(Stage stage) throws IOException {
        URL fxmlUrl = MainWindow.class.getResource("/fxml/MainWindow.fxml");
        Parent root = new FXMLLoader(fxmlUrl).load();

        Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        applyTheme(scene);

        stage.setTitle("JCube");
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setScene(scene);
        stage.show();
    }

    private static void applyTheme(Scene scene) {
        Settings settings = new SettingsDB().get();
        String stylesheet = "dark".equalsIgnoreCase(settings.getTheme())
                ? "/css/dark.css"
                : "/css/light.css";
        scene.getStylesheets().add(MainWindow.class.getResource(stylesheet).toExternalForm());
    }
}
