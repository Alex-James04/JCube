package ui;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import db.ColorSchemeDB;
import model.ColorScheme;

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
        ColorScheme scheme = new ColorSchemeDB().get();
        applyColorScheme(scene, scheme);
    }

    // Also used to hot-swap the color scheme from the Settings screen, and to keep dialog windows
    // consistent with whichever scheme is currently active.
    public static void applyColorScheme(Scene scene, ColorScheme scheme) {
        String css = buildCss(scheme);
        String base64 = Base64.getEncoder().encodeToString(css.getBytes(StandardCharsets.UTF_8));
        String dataUri = "data:text/css;base64," + base64;
        scene.getStylesheets().clear();
        scene.getStylesheets().add(dataUri);
    }

    // Named {{placeholder}} substitution rather than positional String.formatted(%s...) — a
    // mismatched %s/argument count previously crashed the app on launch, and every selector added
    // since (choice-box, context-menu, spinner, ...) made that miscount easier to reintroduce.
    private static String buildCss(ColorScheme s) {
        String template = """
                .root {
                    -fx-background-color: {{background}};
                    -fx-text-fill: {{textPrimary}};
                    -fx-font-family: "Segoe UI", sans-serif;
                }
                .label {
                    -fx-text-fill: {{textPrimary}};
                }
                .label-secondary {
                    -fx-text-fill: {{textSecondary}};
                }
                /* CheckBox's own Modena rule sets -fx-text-fill from -fx-text-background-color, a
                   derived variable computed from Modena's own default -fx-background — since we
                   never redefine that semantic variable (only -fx-background-color literally),
                   checkbox text stayed dark/low-contrast regardless of .root's override above.
                   Targeting .check-box directly bypasses that indirection. */
                .check-box {
                    -fx-text-fill: {{textPrimary}};
                }
                .button {
                    -fx-background-color: {{button}};
                    -fx-text-fill: {{textPrimary}};
                }
                .button:hover {
                    -fx-background-color: {{buttonHover}};
                }
                .danger-button {
                    -fx-background-color: {{danger}};
                    -fx-text-fill: white;
                }
                .list-view {
                    -fx-background-color: {{surface}};
                    -fx-control-inner-background: {{surface}};
                }
                .list-cell {
                    -fx-background-color: {{surface}};
                    -fx-text-fill: {{textPrimary}};
                }
                .list-cell:filled:selected {
                    -fx-background-color: {{accent}};
                }
                .split-pane {
                    -fx-background-color: {{background}};
                }
                .split-pane-divider {
                    -fx-background-color: {{border}};
                }
                .app-title {
                    -fx-text-fill: {{accent}};
                    -fx-font-weight: bold;
                    -fx-font-size: 28px;
                }
                .top-nav {
                    -fx-background-color: {{surface}};
                }
                .bottom-bar {
                    -fx-background-color: {{surface}};
                }
                .scroll-pane, .scroll-pane > .viewport {
                    -fx-background-color: transparent;
                }
                /* ChoiceBox (Spacebar mechanics / Inspection timing) and its dropdown popup, which
                   is implemented as a ContextMenu — without these the dropdown keeps Modena's light
                   default skin and its text is unreadable against a dark scheme. */
                .choice-box {
                    -fx-background-color: {{button}};
                    -fx-mark-color: {{textPrimary}};
                }
                .choice-box .label {
                    -fx-text-fill: {{textPrimary}};
                }
                .combo-box-base {
                    -fx-background-color: {{button}};
                }
                .combo-box-base .arrow-button .arrow {
                    -fx-background-color: {{textPrimary}};
                }
                .context-menu {
                    -fx-background-color: {{surface}};
                }
                .menu-item .label {
                    -fx-text-fill: {{textPrimary}};
                }
                .menu-item:focused {
                    -fx-background-color: {{accent}};
                }
                .color-picker .color-picker-label {
                    -fx-text-fill: {{textPrimary}};
                }
                .spinner .text-field {
                    -fx-background-color: {{button}};
                    -fx-text-fill: {{textPrimary}};
                }
                """;

        Map<String, String> values = new LinkedHashMap<>();
        values.put("background", s.getBackground());
        values.put("surface", s.getSurface());
        values.put("textPrimary", s.getTextPrimary());
        values.put("textSecondary", s.getTextSecondary());
        values.put("accent", s.getAccent());
        values.put("button", s.getButton());
        values.put("buttonHover", s.getButtonHover());
        values.put("danger", s.getDanger());
        values.put("border", s.getBorder());

        String css = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            css = css.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return css;
    }
}
