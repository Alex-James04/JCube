import javafx.application.Application;
import javafx.stage.Stage;

import ui.MainWindow;

public class JCubeApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        MainWindow.show(stage);
    }
}
