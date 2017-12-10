package ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class Message extends VBox {
    public Message(String author, String date, String message) {
        getStylesheets().add(getClass().getResource("/css/message.css").toExternalForm());

        Label authorLabel = new Label(author);
        Label dateLabel = new Label(date);

        HBox topBar = new HBox();
        topBar.setSpacing(5);
        topBar.getChildren().addAll(authorLabel, dateLabel);
        topBar.getStyleClass().add("message-top-bar");

        Label messageLabel = new Label(message);
        //messageLabel.setTextAlignment(TextAlignment.JUSTIFY);
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("message-message-label");

        //VBox.setMargin(topBar, new Insets(0,10,0,10));
        //VBox.setMargin(messageLabel, new Insets(0,10,0,10));

        getChildren().addAll(topBar, messageLabel);
        getStyleClass().add("message");
    }
}
