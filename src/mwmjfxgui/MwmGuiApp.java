package mwmjfxgui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MwmGuiApp extends Application {

    public static final String APPLICATION_TITLE = "MWM Builder JFXGUI";

    public static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        MwmGuiApp.primaryStage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/mwmgui.fxml"));
        primaryStage.setTitle(APPLICATION_TITLE);
        Scene scene = new Scene(root, 720, 480);
        scene.getStylesheets().add("/css/styles.css");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
