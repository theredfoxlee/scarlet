package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Scarlet extends Application{
    // ------------SCENE-CONSTANTS---------------
    private final double prefWidth = 500.0;
    private final double prefHeight = 350.0;
    private final double minWidth = 350.0;
    private final double minHeight = 300.0;

    private final KeyCombination toggleKey = new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN);
    private final KeyCombination newLineKey = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.SHIFT_DOWN);
    // ------------------------------------------

    private VBox messagesPane;
    private TextArea textArea;

    private Communication communication;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // --------APPLICATION-STARTING-POINT--------
        Scene mainScene = buildMainScene();
        Scene sideScene = buildSideScene();
        Scene logingScene = setloginScene(primaryStage,mainScene);

        bindToToggle(primaryStage, mainScene, sideScene, toggleKey);

        primaryStage.setMinWidth(minWidth);
        primaryStage.setMinHeight(minHeight);

        primaryStage.setScene(logingScene);
        primaryStage.show();

        // ----CONNECTION----

        communication = new Client("localhost", 6666);
        //communication = new Server(6666);
        Thread thread = new Thread(() -> { communication.go(); });
        thread.setDaemon(true);
        thread.start();

        // ------------------------------------------
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

        textArea = new TextArea();
        textArea.setPrefHeight(25);
        textArea.setOnKeyPressed(e -> {
            if (newLineKey.match(e)) {
                textArea.appendText("\n");
            } else if (e.getCode().equals(KeyCode.ENTER)) {
                if (!textArea.getText().isEmpty()) {
                    if (communication.isReady()) {
                        communication.send(new Message("Kamil", getCurrentDate(),textArea.getText()));
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

    private Scene buildSideScene() {
        VBox vBox = new VBox();
        vBox.setPrefWidth(prefWidth);
        vBox.setPrefHeight(prefHeight);

        Scene sideScene =  new Scene(vBox);

        sideScene.getStylesheets().clear();
        sideScene.getStylesheets().add(getClass().getResource("/css/scarlet.css").toExternalForm());

        return sideScene;
    }
    //method used for setting loging scene
    private Scene setloginScene(Stage stage,Scene mainScene){

        GridPane gridPane = new GridPane();
        gridPane.setMinSize(minWidth,minHeight);
        gridPane.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());

        gridPane.setAlignment(Pos.CENTER);
        gridPane.setPadding(new Insets(10,10,10,10));
        //gaps between items
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        TextField login =new TextField("Set your Login");
        GridPane.setConstraints(login,1,0);

        Label loginLabel = new Label("Login:");
        GridPane.setConstraints(loginLabel,0,0);

        PasswordField password = new PasswordField();
        GridPane.setConstraints(password,1,1);

        Label passwordLabel = new Label("Password:");
        GridPane.setConstraints(passwordLabel,0,1);

        Button logingButton = new Button("Login");
        logingButton.getStyleClass().add("login-button");
        GridPane.setConstraints(logingButton,0,3);

        Button addButton = new Button("Add");
        addButton.getStyleClass().add("login-button");
        GridPane.setConstraints(addButton,1,3);


        Login_authorize authorization = new Login_authorize();

        logingButton.setOnAction(e -> {
            if(authorization.autorize(login.getText(),password.getText())){
                stage.setScene(mainScene);
            }
            else{
                logingButton.getStyleClass().add("login-wrong");
            }

        });


        gridPane.getChildren().addAll(login,loginLabel,password,passwordLabel,logingButton,addButton);

        Scene logingScene = new Scene(gridPane);
        return logingScene;
    }

    private void bindToToggle(Stage stage, Scene scene_1, Scene scene_2, KeyCombination toggleKey) {
        scene_1.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (toggleKey.match(e)) stage.setScene(scene_2);
        });
        scene_2.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (toggleKey.match(e)) stage.setScene(scene_1);
        });
    }

    private void addMessage(Message message) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                messagesPane.getChildren().add(message);
            }
        });
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
    }

    private abstract class Communication {
        protected Socket socket;

        protected ObjectInputStream inputStream;
        protected ObjectOutputStream outputStream;

        protected boolean isFine = true;
        protected boolean activated = false;

        public abstract void go();

        public void send(Message message) {
            try {
                outputStream.writeObject(message.getAuthor());
                outputStream.writeObject(message.getDate());
                outputStream.writeObject(message.getMessage());
                outputStream.flush();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
            addMessage(message);
        }

        public boolean isActive() {
            return activated;
        }

        public boolean isReady() {
            return isFine && activated;
        }

        public void disable() {
            isFine = false;
            activated = false;
        }

        protected void initStreams() throws IOException {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        }

        protected void chat() throws IOException, ClassNotFoundException {
            while (isFine) {
                String author = (String) inputStream.readObject();
                String date = (String) inputStream.readObject();
                String message = (String) inputStream.readObject();

                addMessage(new Message(author, date, message));
            }
        }

        protected void close() throws IOException {
            inputStream.close();
            outputStream.close();
            socket.close();
        }
    }

    private class Server extends Communication {
        private int port;

        public Server(int port) {
            this.port = port;
        }

        public void go() {
            startServer();
        }

        public void startServer() {
            try {
                this.initSocket();
                this.initStreams();

                activated = true;

                this.chat();
                this.close();
            } catch (Exception e) {
                isFine = false;
                activated = false;
                System.err.println(e.getMessage());
            }
        }

        public void initSocket() throws IOException {
            ServerSocket serverSocket = new ServerSocket(port);
            socket = serverSocket.accept();
        }

        public void stopServer() {
            isFine = false;
        }
    }

    private class Client extends Communication {
        private String ip;
        private int port;

        public Client(String ip, int port) {
            this.ip = ip;
            this.port = port;
            this.activated = false;
        }

        public void go() {
            connect();
        }

        public void connect() {
            try {
                this.initSocket();
                this.initStreams();

                activated = true;

                this.chat();
                this.close();
            } catch (Exception e) {
                isFine = false;
                activated = false;
                System.err.println(e.getMessage());
            }
        }

        public void initSocket() throws IOException {
            socket = new Socket(ip, port);
        }
    }
}

