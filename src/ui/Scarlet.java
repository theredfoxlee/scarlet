package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import networking.Client;
import networking.LoginAuthorization;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Scarlet extends Application {
    // ------------SCENE-CONSTANTS---------------
    private final double prefWidth = 500.0;
    private final double prefHeight = 350.0;
    private final double minWidth = 350.0;
    private final double minHeight = 300.0;

    private final KeyCombination newLineKey = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.SHIFT_DOWN);
    // ------------------------------------------

    // ------------NECESSARY-HANDLES-------------
    private VBox messagesPane;        // for adding messages
    private TextField loginTextField; // for reading login name
    private Client client;            // for performing I/O operations with server
    // ------------------------------------------

    // --------------ABOUT-SERVER----------------
    private final String host = "localhost";
    private final Integer port = 6666;
    // ------------------------------------------

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // --------APPLICATION-STARTING-POINT--------
        Scene mainScene = buildMainScene();
        Scene loginScene = setLoginScene(primaryStage, mainScene);

        primaryStage.setMinWidth(minWidth);
        primaryStage.setMinHeight(minHeight);

        primaryStage.setScene(loginScene);
        primaryStage.show();
        // ------------------------------------------
    }

    @Override
    public void stop() throws Exception {
        client.close();
        super.stop();
    }

    public void establishConnection(String login,String password) {
        //if client.authorize
        System.out.println(login);
        System.out.println(password);
        client = new Client(host, port, this);
//
        client.send(new Message(login,getCurrentDate(),password));
        client.send(new Message(login, getCurrentDate(), "HANDSHAKE"));

    }


    private Scene buildMainScene() {
        BorderPane borderPane = new BorderPane();
        borderPane.setPrefWidth(prefWidth);
        borderPane.setPrefHeight(prefHeight);
        borderPane.getStyleClass().add("main-scene");

        messagesPane = new VBox();
        messagesPane.setSpacing(10);
        messagesPane.getStyleClass().add("message-pane");

        ScrollPane scrollableMessagePane = new ScrollPane();
        scrollableMessagePane.setContent(messagesPane);
        scrollableMessagePane.setFitToWidth(true);

        messagesPane.heightProperty().addListener((observable,oldValue,newValue) -> {scrollableMessagePane.setVvalue((Double)newValue );});

        TextArea textArea = new TextArea();
        textArea.setPrefHeight(25);
        textArea.setOnKeyPressed(e -> {
            if (newLineKey.match(e)) {
                textArea.appendText("\n");
            } else if (e.getCode().equals(KeyCode.ENTER)) {
                if (!textArea.getText().isEmpty()) {
                    if (!client.isClosed()) {
                        client.send(new Message(loginTextField.getText(), getCurrentDate(), textArea.getText()));
                        textArea.clear();
                    }
                }
            }
        });

        borderPane.setCenter(scrollableMessagePane);
        borderPane.setBottom(textArea);

        Scene mainScene = new Scene(borderPane);
        mainScene.getStylesheets().clear();
        mainScene.getStylesheets().add(getClass().getResource("/css/scarlet.css").toExternalForm());

        return mainScene;
    }

    private Scene setLoginScene(Stage stage,Scene mainScene){

        GridPane gridPane = new GridPane();
        gridPane.setMinSize(minWidth,minHeight);
        gridPane.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());

        gridPane.setAlignment(Pos.CENTER);
        gridPane.setPadding(new Insets(10,10,10,10));
        //gaps between items
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        loginTextField = new TextField("Set your Login");
        GridPane.setConstraints(loginTextField,1,0);

        Label loginLabel = new Label("Login:");
        GridPane.setConstraints(loginLabel,0,0);

        PasswordField passwordTextField = new PasswordField();
        GridPane.setConstraints(passwordTextField,1,1);

        Label passwordLabel = new Label("Password:");
        GridPane.setConstraints(passwordLabel,0,1);

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("login-button");
        GridPane.setConstraints(loginButton,0,3);

        Button addButton = new Button("Add");
        addButton.getStyleClass().add("login-button");
        GridPane.setConstraints(addButton,1,3);

        // temporary
        loginButton.setOnAction(e -> {

            stage.setScene(mainScene);
            establishConnection(loginTextField.getText(),passwordTextField.getText());

        });

        // TEMPORARY COMMENTED
        /*LoginAuthorization authorization = new LoginAuthorization();

        logingButton.setOnAction(e -> {
            if(authorization.autorize(login.getText(),password.getText())){
                stage.setScene(mainScene);
                this.name = login.getText();
            }
            else{
                logingButton.getStyleClass().add("login-wrong");
            }

        });*/


        gridPane.getChildren().addAll(loginTextField, loginLabel, passwordTextField, passwordLabel, loginButton, addButton);

        Scene loginScene = new Scene(gridPane);
        return loginScene;
    }

    public void addMessage(Message message) {
        Platform.runLater( () -> messagesPane.getChildren().add(message));
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
    }
}

