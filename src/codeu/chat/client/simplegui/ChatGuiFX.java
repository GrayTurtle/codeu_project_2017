package codeu.chat.client.simplegui;

import codeu.chat.client.ClientUser;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.scene.text.*;
import javafx.collections.*;

import codeu.chat.client.ClientContext;
import codeu.chat.client.Controller;
import codeu.chat.client.View;
import codeu.chat.util.Logger;

public final class ChatGuiFX extends Application {

    private final static Logger.Log LOG = Logger.newLog(ChatGuiFX.class);


    private static final double WINDOW_WIDTH = 1000;
    private static final double WINDOW_HEIGHT = 500;
    private static final String SIGNIN_ERROR_MESSAGE = "Your username or password does not match. Have you signed up yet?";
    private static final String SIGNUP_ERROR_MESSAGE = "Sorry, that username already exists. Please choose a different one.";
    private static final String BADCHAR_ERROR_MESSAGE = "Usernames and passwords can only be composed of letters and numbers, no special characters.";


    // both page vars
    private ClientContext clientContext;


    // login page vars
    private Stage thestage;                         // Holds the scene that user is currently viewing
    private Scene signInScene, mainScene;           // Scenes to hold all the elements for each page
    private TextField userInput;
    private PasswordField passInput;                // Takes input for username and password

    // There was merge conflict here. I have no idea why
    private Label errorLabel;                       // Displays error messages
    private static ClientContext clientContext;          

    // main page vars
    private HBox hboxClient;
    private HBox hboxInput;
    private VBox userVBox;
    private VBox chatVBox;
    private VBox convosVBox;
    private BorderPane container;
    private Button sendButton;
    private Button updateButton;
    private Button addConvoButton;
    private Text userTitle;
    private Text chatTitle;
    private Text convosTitle;
    private TextFlow userTf;
    private TextFlow chatTf;
    private TextFlow convosTf;
    private TextField input;

    /*public void run(String[] args) {
        try {
            // launches gui
            Application.launch(ChatGuiFX.class, args);
        } catch (Exception ex) {
            System.out.println("ERROR: Exception in ChatGuiFX.run. Check log for details.");
            LOG.error(ex, "Exception in ChatGuiFX.run");
            System.exit(1);
        }

    }*/
    
    public static void setContext(Controller controller, View view) {
    	clientContext = new ClientContext(controller, view);
    }

    public void launch(Controller controller, View view) {
    	setContext(controller, view);
        Application.launch(ChatGuiFX.class);
    }

    // Test commit- commit only, no pull request yet

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Sign in page

        this.thestage = primaryStage;                           // Initialize the main stage

        BorderPane signInPane = new BorderPane();               // Initialize panes
        FlowPane signInLabelPane = new FlowPane();
        HBox inputMasterBox = new HBox();
        inputMasterBox.setSpacing(5);
        VBox inputVBox = new VBox();
        HBox usernameHBox = new HBox();
        HBox passHBox = new HBox();
        VBox buttonBox = new VBox();
        buttonBox.setPrefWidth(80);

        // Set Pane alignments

        signInLabelPane.setAlignment(Pos.BOTTOM_CENTER);
        inputMasterBox.setAlignment(Pos.CENTER);
        inputVBox.setAlignment(Pos.CENTER);
        usernameHBox.setAlignment(Pos.CENTER);
        passHBox.setAlignment(Pos.CENTER);
        buttonBox.setAlignment(Pos.CENTER);


        // Set up labels
        Label signInLabel = new Label("Sign-in screen");
        Label userLabel = new Label("Username:");
        Label passLabel = new Label("Password:");
        errorLabel = new Label("");

        signInLabel.setFont(Font.font(20));
        userLabel.setFont(Font.font(15));
        passLabel.setFont(Font.font(15));
        errorLabel.setFont(Font.font(15));
        errorLabel.setTextFill(Color.web("#FF0000"));

        // Set up buttons
        Button signInButton = new Button("Sign in");
        Button signUpButton = new Button("Sign up");
        signInButton.setMinWidth(buttonBox.getPrefWidth());
        signUpButton.setMinWidth(buttonBox.getPrefWidth());
        signInButton.setOnAction((event)-> signInButtonClicked(event));       // Initialize event handlers
        signUpButton.setOnAction((event) -> signUpButtonClicked(event));      //

        // Set up password fields
        userInput = new TextField();
        passInput = new PasswordField();
        userInput.setPromptText("Username");
        passInput.setPromptText("Password");
        userInput.setAlignment(Pos.CENTER);
        passInput.setAlignment(Pos.CENTER);


        signInLabelPane.getChildren().add(signInLabel);         // Add labels to their respective panes

        usernameHBox.getChildren().add(userLabel);
        usernameHBox.getChildren().add(userInput);          // Set up HBoxes to hold username and password labels/inputs
        passHBox.getChildren().add(passLabel);
        passHBox.getChildren().add(passInput);

        inputVBox.getChildren().add(usernameHBox);
        inputVBox.getChildren().add(passHBox);              // Add those HBoxes to a VBox to stack them on top of each other

        buttonBox.getChildren().add(signInButton);
        buttonBox.getChildren().add(signUpButton);          // Add buttons to a VBox to one on top of the other

        inputMasterBox.getChildren().add(inputVBox);
        inputMasterBox.getChildren().add(buttonBox);     // Add that VBox and buttons to the inputMasterBox

        signInPane.setTop(signInLabelPane);
        signInPane.setCenter(inputMasterBox);                 // Add labels and input box to the pane
        signInPane.setBottom(errorLabel);


        signInScene = new Scene(signInPane, WINDOW_WIDTH, WINDOW_HEIGHT);



        // Main Page

        hboxClient = new HBox();
        hboxInput = new HBox();
        userVBox = new VBox();
        chatVBox = new VBox();
        convosVBox = new VBox();
        container = new BorderPane();
        sendButton = new Button("Send");
        updateButton = new Button("Update");
        addConvoButton = new Button("Add Conversation");
        userTitle = new Text("Users");
        chatTitle = new Text("Conversation"); // changed based on name?
        convosTitle = new Text("Conversations");
        userTf = new TextFlow(userTitle);
        chatTf = new TextFlow(chatTitle);
        convosTf = new TextFlow(convosTitle);
        input = new TextField();

        // list of users
        ObservableList<String> usersList = FXCollections.observableArrayList();
        ListView<String> users = new ListView<String>(usersList);

        // list of conversations
        ObservableList<String> convoList = FXCollections.observableArrayList();
        ListView<String> conversations = new ListView<String>(convoList);

        // list of messages
        ObservableList<String> messageList = FXCollections.observableArrayList();
        ListView<String> messages = new ListView<String>(messageList);

        // add listener for when user presses send and add text to the messageList
        sendButton.setOnAction(e -> messageList.addAll(input.getText()));

        // set dimensions and add components
        VBox.setVgrow(users, Priority.ALWAYS);
        VBox.setVgrow(conversations, Priority.ALWAYS);
        VBox.setVgrow(messages, Priority.ALWAYS);
        HBox.setHgrow(input, Priority.ALWAYS);
        HBox.setHgrow(userVBox, Priority.ALWAYS);
        HBox.setHgrow(chatVBox, Priority.ALWAYS);
        HBox.setHgrow(convosVBox, Priority.ALWAYS);

        sendButton.setMinHeight(40);
        updateButton.setMinHeight(40);
        input.setMinHeight(40);
        addConvoButton.setMinHeight(40);
        addConvoButton.setMaxWidth(Double.MAX_VALUE);
        userTf.setMaxWidth(Double.MAX_VALUE);
        userTf.setMinHeight(30);
        chatTf.setMaxWidth(Double.MAX_VALUE);
        chatTf.setMinHeight(30);
        convosTf.setMaxWidth(Double.MAX_VALUE);
        convosTf.setMinHeight(30);
        userVBox.setMaxWidth(150);
        chatVBox.setMaxWidth(Double.MAX_VALUE);
        convosVBox.setMaxWidth(150);

        hboxInput.getChildren().addAll(input, sendButton, updateButton);
        userVBox.getChildren().addAll(userTf, users);
        chatVBox.getChildren().addAll(chatTf, messages, hboxInput);
        convosVBox.getChildren().addAll(convosTf, conversations, addConvoButton);
        hboxClient.getChildren().addAll(userVBox, chatVBox, convosVBox);
        container.setCenter(hboxClient);

        mainScene = new Scene(container, WINDOW_WIDTH, WINDOW_HEIGHT);
        thestage.setScene(signInScene);
        thestage.show();
    }

    private void signInButtonClicked(ActionEvent e)
    {
        String username = userInput.getText();
        String password = passInput.getText();
        
         // TODO: I'm placing signInUser outside of the if statement since it always return false. 
         //		  The function isValidInputs() needs to be looked at and changed.
        clientContext.user.signInUser(username, password);

        if (ClientUser.isValidInput(username) && ClientUser.isValidInput(password)) {


            //clientContext.user.signInUser(username, password);
            thestage.setScene(mainScene);
        }
        else {

            errorLabel.setText(BADCHAR_ERROR_MESSAGE);
        }
    }

    private void signUpButtonClicked(ActionEvent e) {
        String username = userInput.getText();
        String password = passInput.getText();
        
        if (ClientUser.isValidInput(username) && ClientUser.isValidInput(password)) {
            clientContext.user.addUser(username, password);
            thestage.setScene(mainScene);
        }
        else {
            errorLabel.setText(BADCHAR_ERROR_MESSAGE);
        }
    }
}
