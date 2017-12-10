package ui;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Message extends VBox {
    public Message(String author, String date, String message) {
        getStylesheets().add("/css/message.css");

        Label authorLabel = new Label(author);
        authorLabel.getStyleClass().add("message-info-author");

        Label dateLabel = new Label(date);
        dateLabel.getStyleClass().add("message-info-date");

        HBox topBar = new HBox();
        topBar.getChildren().addAll(authorLabel, dateLabel);
        topBar.getStyleClass().add("message-info");

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("message-message");

        getChildren().addAll(topBar, messageLabel);
        getStyleClass().add("message");
    }
}
