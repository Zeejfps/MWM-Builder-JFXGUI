package mwmjfxgui;

import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class FlowTerminal {

    private static final Font MONOSPACE_FONT = Font.loadFont(
            MwmGuiApp.class.getResourceAsStream("/fonts/RobotoMono-Medium.ttf"), 12);

    private final TextFlow flow;
    private final ScrollPane scrollPane;

    public FlowTerminal(TextFlow flow, ScrollPane scrollPane) {
        this.flow = flow;
        this.scrollPane = scrollPane;
    }

    public void println(String string) {
        print(string+"\n");
    }

    public void print(String string) {
        Text text = new Text(string);
        text.setFill(Color.GREEN);
        text.setFont(MONOSPACE_FONT);
        flow.getChildren().add(text);
        scrollPane.layout();
        scrollPane.setVvalue(1.0);
    }

}
