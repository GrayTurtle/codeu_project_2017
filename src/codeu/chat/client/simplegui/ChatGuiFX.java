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
import javafx.scene.input.MouseEvent;

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

    // list of conversations
    private ObservableList<String> convoList;      // list of conversations
    private ListView<String> conversations;        // holds the list of conversations
    Text chatTitle;                                // title of conversation

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

        HBox hboxClient = new HBox();                           // holds the client
        HBox hboxInput = new HBox();                            // holds the input box
        VBox userVBox = new VBox();                             // holds the list of users
        VBox chatVBox = new VBox();                             // holds chat box
        VBox convosVBox = new VBox();                           // holds the conversations
        BorderPane container = new BorderPane();                // contains the boxes
        Button sendButton = new Button("Send");                 // button for sending a message
        Button updateButton = new Button("Update");             // button for updating the client
        Button addConvoButton = new Button("Add Conversation"); // button for adding conversation
        Text userTitle = new Text("Users");
        chatTitle = new Text("Conversation");                   // changed based on which is select
        Text convosTitle = new Text("Conversations");
        TextFlow userTf = new TextFlow(userTitle);
        TextFlow chatTf = new TextFlow(chatTitle);
        TextFlow convosTf = new TextFlow(convosTitle);
        TextField input = new TextField();

        // list of users
        ObservableList<String> usersList = FXCollections.observableArrayList();
        ListView<String> users = new ListView<String>(usersList);

        // list of conversations
        convoList = FXCollections.observableArrayList();
        conversations = new ListView<String>(convoList);

        // list of messages
        ObservableList<String> messageList = FXCollections.observableArrayList();
        ListView<String> messages = new ListView<String>(messageList);

        // add listener for when user presses send and add text to the messageList
        sendButton.setOnAction(e -> messageList.addAll(input.getText()));
        // add listener for when user presses add conversation & add to the conversation list
        addConvoButton.setOnAction(e -> addConversation(e));
        // add listener to the list of conversations to select a conversations
        conversations.setOnMouseClicked(e -> selectConversation(e));

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

    private void signInButtonClicked(ActionEvent e) {
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

    private void addConversation(ActionEvent e) {
        // popup for the user to add a conversation
        TextInputDialog dialog = new TextInputDialog("Name your conversation!");
        dialog.setTitle(" "); // make the title blank
        dialog.setHeaderText("Create your conversation");

        // get input and add the name of the convo
        String name = dialog.showAndWait().get();

        // TODO: check for duplicate conversations & handle them

        if (name != null && name.length() > 0) {
            // TODO: users need to be added to the server first? So I can add the conversation to that user???
            //clientContext.conversation.startConversation(name, clientContext.user.getCurrent().id);
            convoList.addAll(name);
        }
    }

    private void selectConversation(MouseEvent e) {
        // set the conversation title
        chatTitle.setText(conversations.getSelectionModel().getSelectedItem());
    }
}
