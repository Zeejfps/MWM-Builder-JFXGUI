package mwmjfxgui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class AppController implements Initializable {

    private static final Font FONTAWESOME_FONT = Font.loadFont(
            MwmGuiApp.class.getResourceAsStream("/fonts/fontawesome-webfont.ttf"), 12);

    private static final String SRC_PATH_KEY = "srcPath";
    private static final String DST_PATH_KEY = "dstPath";
    private static final String MWM_PATH_KEY = "mwmPath";

    @FXML
    private TextField srcPathField;

    @FXML
    private TextField dstPathField;

    @FXML
    private TextFlow textFlow;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Button settingsButton;

    @FXML
    private Button buildButton;

    @FXML
    private CheckBox compareBuildDatesCheckbox;

    @FXML
    private CheckBox exportXMLCheckbox;

    @FXML
    private CheckBox forceRebuildCheckbox;

    @FXML
    private CheckBox boundariesCheckbox;

    private String mwmBuilderPath;
    private FlowTerminal terminal;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Preferences prefs = Preferences.userRoot();

        String srcPath = prefs.get(SRC_PATH_KEY, "");
        srcPathField.textProperty().setValue(srcPath);

        String dstPath = prefs.get(DST_PATH_KEY, "");
        dstPathField.textProperty().setValue(dstPath);

        terminal = new FlowTerminal(textFlow, scrollPane);

        settingsButton.setFont(FONTAWESOME_FONT);
        settingsButton.setText("\uF013");

        String mwmPath = prefs.get(MWM_PATH_KEY, "");
        mwmBuilderPath = mwmPath;
        if (mwmBuilderPath.isEmpty()) {
            onSettingsButtonClicked();
        }
    }

    @FXML
    protected void onBrowseSourceButtonClicked() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDir = directoryChooser.showDialog(MwmGuiApp.primaryStage);
        if (selectedDir != null) {
            String path = selectedDir.getAbsolutePath();
            srcPathField.textProperty().setValue(path);
            Preferences.userRoot().put(SRC_PATH_KEY, path);
        }
    }

    @FXML
    protected void onBrowseDestinationButtonClicked() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDir = directoryChooser.showDialog(MwmGuiApp.primaryStage);
        if (selectedDir != null) {
            String path = selectedDir.getAbsolutePath();
            dstPathField.textProperty().setValue(path);
            Preferences.userRoot().put(DST_PATH_KEY, path);
        }
    }

    @FXML
    protected void onSettingsButtonClicked() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select MWM Builder Directory");
        File selectedDir = directoryChooser.showDialog(MwmGuiApp.primaryStage);
        if (selectedDir != null) {
            String path = selectedDir.getAbsolutePath();
            mwmBuilderPath = path;
            Preferences.userRoot().put(MWM_PATH_KEY, path);
        }
    }

    @FXML
    protected void onBuildButtonClicked() {
        if (mwmBuilderPath == null || mwmBuilderPath.isEmpty()) {
            terminal.println(": ERROR:\n     No MWM Builder Path Set!");
            return;
        }

        buildButton.setDisable(true);
        List<String> commands = new ArrayList<>();

        // Run the EXE
        commands.add(mwmBuilderPath + "\\MwmBuilder.exe");

        // Set the src dir
        commands.add("/s:" + srcPathField.getText());
        // Set the dst dir
        commands.add("/o:" + dstPathField.getText());

        // /g - Don't compare app build date to files
        // NOTE: we are checking its NOT selected
        if (!compareBuildDatesCheckbox.isSelected()) {
            commands.add("/g");
        }

        // /e - Force XML export
        if(exportXMLCheckbox.isSelected()) {
            commands.add("/e");
        }

        // /f - Rebuild files even when up-to-date
        if (forceRebuildCheckbox.isSelected()) {
            commands.add("/f");
        }

        // /checkOpenBoundaries - Warn if model contains open boundaries
        if (boundariesCheckbox.isSelected()) {
            commands.add("/checkOpenBoundaries");
        }

        ProcessBuilder pb = new ProcessBuilder(commands);

        try {
            Process process = pb.start();

            BufferedReader stdIn = new BufferedReader(new
                    InputStreamReader(process.getInputStream()));
            BufferedReader stdErr = new BufferedReader(new
                    InputStreamReader(process.getErrorStream()));

            new Thread(() -> {
                processInputStream(process, stdIn);
                Platform.runLater(() -> {
                    terminal.println("Process terminated.\n");
                    buildButton.setDisable(false);
                });
            }).start();

            new Thread(() -> {
                processInputStream(process, stdErr);
            }).start();

        } catch (IOException e) {
            terminal.println(": ERROR:");
            terminal.println("     " + e.getMessage());
            terminal.println("     Check your MWM Builder path is set!\n");
            buildButton.setDisable(false);
        }
    }

    private void processInputStream(Process process, BufferedReader reader) {
        try {
            while (process.isAlive()) {
                final String line = reader.readLine();
                if (line != null) {
                    Platform.runLater(() -> terminal.println(line));
                }
            }
        } catch (IOException e) {
            terminal.println(e.getMessage());
        }
    }

    public void onQuitMenuClicked(ActionEvent actionEvent) {
        Platform.exit();
    }
}
