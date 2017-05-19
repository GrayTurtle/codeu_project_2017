package codeu.chat.client.simplegui;

import codeu.chat.client.ClientUser;
import java.io.File;
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
import javafx.scene.control.Alert.AlertType;

import codeu.chat.client.ClientContext;
import codeu.chat.client.Controller;
import codeu.chat.common.ConversationSummary;
import codeu.chat.common.User;
import codeu.chat.common.Message;
import codeu.chat.client.View;
import codeu.chat.util.Logger;
import codeu.chat.util.Uuid;

public final class ChatGuiFX extends Application {

    private final static Logger.Log LOG = Logger.newLog(ChatGuiFX.class);

    private static final double WINDOW_WIDTH = 1000;
    private static final double WINDOW_HEIGHT = 500;
    private static final String SIGNIN_ERROR_MESSAGE = "Your username or password does not match. Have you signed up yet?";
    private static final String SIGNUP_ERROR_MESSAGE = "Sorry, that username already exists. Please choose a different one.";
    private static final String BADCHAR_ERROR_MESSAGE = "Usernames and passwords can only be composed of letters and numbers, no special characters.";

    // both page vars
    //private ClientContext clientContext;

    /*
        **This is something to think about**
        // TODO: split the login & main screens into separate methods
        public void run(String[] args) {
            buildLoginScene();
            buildMainScene();
        }
    */

    // login page vars
    // Holds the scene that user is currently viewing
    private Stage thestage;

    // Scenes to hold all the elements for each page
    private Scene signInScene, mainScene;

    // Takes input for username and password for sign in/up
    private TextField userInput;
    private PasswordField passInput;

    // Displays error messages for when the user is not sign in or has not selected a convo
    private Label errorLabel;

    // variable that has the current state of the client (current user, conversation, etc)
    private static ClientContext clientContext;

    // title of current/selected conversation
    private Text chatTitle;

    // these hold the conversations to be displayed on the right panel
    private ObservableList<String> convoList;
    private ListView<String> conversations;

    // these hold the messages of a selected conversation to be displayed in the middle panel
    private ObservableList<TextFlow> messageList;
    private ListView<TextFlow> messages;

    // these hold the users to be displayed on the left panel
    private ObservableList<Text> usersList;
    private ListView<Text> users;

    // field for the input of messages into a conversation
    private TextField input;

    private Text userName;

    public void setContext(Controller controller, View view) {
    	clientContext = new ClientContext(controller, view);
    }

    public void launch(Controller controller, View view) {
    	setContext(controller, view);
        Application.launch(ChatGuiFX.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Sign in page
        // Initialize the main stage
        this.thestage = primaryStage;
        // Initialize panes
        BorderPane signInPane = new BorderPane();
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

        // Initialize event handlers
        signInButton.setOnAction(e-> signInButtonClicked(e));
        signUpButton.setOnAction(e -> signUpButtonClicked(e));

        // Set up password fields
        userInput = new TextField();
        passInput = new PasswordField();
        userInput.setPromptText("Username");
        passInput.setPromptText("Password");
        userInput.setAlignment(Pos.CENTER);
        passInput.setAlignment(Pos.CENTER);

         // Add labels to their respective panes
        signInLabelPane.getChildren().add(signInLabel);

        // Set up HBoxes to hold username and password labels/inputs
        passHBox.getChildren().add(passLabel);
        usernameHBox.getChildren().add(userLabel);
        usernameHBox.getChildren().add(userInput);
        passHBox.getChildren().add(passInput);

         // Add those HBoxes to a VBox to stack them on top of each other
        inputVBox.getChildren().add(usernameHBox);
        inputVBox.getChildren().add(passHBox);

        // Add buttons to a VBox to one on top of the other
        buttonBox.getChildren().add(signInButton);
        buttonBox.getChildren().add(signUpButton);
        inputMasterBox.getChildren().add(inputVBox);
         // Add that VBox and buttons to the inputMasterBox
        inputMasterBox.getChildren().add(buttonBox);

        signInPane.setTop(signInLabelPane);
        signInPane.setBottom(errorLabel);
        // Add labels and input box to the pane
        signInPane.setCenter(inputMasterBox);

        signInScene = new Scene(signInPane, WINDOW_WIDTH, WINDOW_HEIGHT);

        // Main Page
        // holds the client
        HBox hboxClient = new HBox();
        // holds the input box
        HBox hboxInput = new HBox();
         // holds the list of users
        VBox userVBox = new VBox();
         // holds chat box
        VBox chatVBox = new VBox();
        // holds the conversations
        VBox convosVBox = new VBox();
          // contains the boxes
        BorderPane container = new BorderPane();
         // button for sending a message
        Button sendButton = new Button("Send");
        // button for updating the client
        Button updateButton = new Button("Update");
        // button for adding conversation
        Button addConvoButton = new Button("Add Conversation");
        Text userTitle = new Text("Users");
         // changed based on which is select
        chatTitle = new Text("Conversation");
        Text convosTitle = new Text("Conversations");
        TextFlow userTf = new TextFlow(userTitle);
        TextFlow chatTf = new TextFlow(chatTitle);
        TextFlow convosTf = new TextFlow(convosTitle);
        input = new TextField();

        // initialize the contents of each panel (user, conversations, & messages)
        usersList = FXCollections.observableArrayList();
        users = new ListView<Text>(usersList);
        convoList = FXCollections.observableArrayList();
        conversations = new ListView<String>(convoList);
        messageList = FXCollections.observableArrayList();
        messages = new ListView<TextFlow>(messageList);

        // add listener for when user presses add conversation & add to the conversation list
        addConvoButton.setOnAction(e -> addConversation(e));
        // add listener to the list of conversations to select a conversations
        conversations.setOnMouseClicked(e -> selectConversation(e));
        // add listener to the send button to send messages to the conversation
        sendButton.setOnAction(e -> sendMessage(e));
        // add listener to the update button to update the gui
        updateButton.setOnAction(e -> updateGUI(e));

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

    /*
        Sign in
    */

    /**
    * When the sign in button is clicked, it takes the username & password from the
    * input fields and checks if they are valid inputs. If they are, sign in the user
    * and change to main chat
    */
    private void signInButtonClicked(ActionEvent e) {
        String username = userInput.getText();
        String password = passInput.getText();

        if (ClientUser.isValidInput(username) && ClientUser.isValidInput(password)) {
            if (clientContext.user.signInUser(username, password)) {
            	 // populate the users, conversations, messages panels from the past signins
            	fillMessagesList(clientContext.conversation.getCurrent());
                fillConversationsList(conversations);
                fillUserList(users);
            	thestage.setScene(mainScene);
            }
        }
        else {
            errorLabel.setText(BADCHAR_ERROR_MESSAGE);
        }
    }

    /**
    * When the sign up button is clicked, it takes the username & password from the
    * input fields and checks if they are valid inputs. If they are, add the user
    * and change to main chat
    */
    private void signUpButtonClicked(ActionEvent e) {
        String username = userInput.getText();
        String password = passInput.getText();

        if (ClientUser.isValidInput(username) && ClientUser.isValidInput(password)) {
            if (clientContext.user.addUser(username, password)) {
            	 // populate the users, conversations, messages panels from the past signins
            	fillMessagesList(clientContext.conversation.getCurrent());
            	fillConversationsList(conversations);
            	fillUserList(users);
            	thestage.setScene(mainScene);
            }
        }
        else {
            errorLabel.setText(BADCHAR_ERROR_MESSAGE);
        }
    }

    /*
        Main Chat
    */

    /**
    * When the add conversation button is pressed, it pops up a dialog for the user to
    * input a new conversation. If the conversation name is valid, add the conversation
    * to the list of conversations to be displayed.
    */
    private void addConversation(ActionEvent e) {
        if (clientContext.user.hasCurrent()) {
            // popup for the user to add a conversation
            TextInputDialog dialog = new TextInputDialog("Name your conversation!");
            dialog.setTitle(" ");
            dialog.setHeaderText("Create your conversation");

            // get input and add the name of the convo
            String name = dialog.showAndWait().get();

            // TODO: check for duplicate conversations & handle them
            for (final ConversationSummary cs : clientContext.conversation.getConversationSummaries()) {
            	if (cs.title.equals(name)) {
            		displayAlert("There is already a conversation with that name!");
            		return;
            	}
            }

            if (!name.isEmpty() && name.length() > 0) {
                clientContext.conversation.startConversation(name, clientContext.user.getCurrent().id);
                convoList.add(name);
            }
        } else {
            // user is not signed in
            displayAlert("You're not signed in!");
        }
    }

    /**
    * When the user clicks on a conversation in the right panel, it gets its index and name to
    * fetch its contents, which is a ConversationSummary object. Then it sets the current conversation
    * to the selected conversation.
    */
    private void selectConversation(MouseEvent e) {
        // set the conversation title
        int index = conversations.getSelectionModel().getSelectedIndex();
        String data = conversations.getSelectionModel().getSelectedItem();
        // get contents of conversation
        ConversationSummary selectedConvo = lookupByTitle(data, index);
        // set new conversation
        if (selectedConvo != null) {
	        clientContext.conversation.setCurrent(selectedConvo);
	        updateCurrentConversation(selectedConvo);
        }
    }

    /**
    * Finds the ConversationSummary (the contents of the conversation) that corresponds to
    * the selected conversation
    * @param title  name of the selected conversation
    * @param index  index of selected conversation in the list of conversations
    */
    private ConversationSummary lookupByTitle(String title, int index) {
        for (final ConversationSummary cs : clientContext.conversation.getConversationSummaries()) {
        	// An exception was thrown when localIndex was used and tested in the following if statement.
        	// Removing it seemed to fix the issue.
            if (cs.title.equals(title)) {
                return cs;
            }
        }
        return null;
    }

    /**
    * Sets the conversation title to be selected conversation & fills the middle panel
    * with the selected conversation's messages
    * @param selectedConvo  the selected conversation
    */
    private void updateCurrentConversation(ConversationSummary selectedConvo) {
        chatTitle.setText(selectedConvo.title);
        // fills the message panel with the messages of the selected conversation
        fillMessagesList(selectedConvo);
    }

    /**
    * When the user presses the send button, it gives user's unique message to the
    * current conversation & adds to the list of messages to be displayed.
    * If the user is not signed in or has not selected a conversation, an error
    * message pops up.
    */
    private void sendMessage(ActionEvent e) {
        if (!clientContext.user.hasCurrent()) {
            // if the user is not signed in
            displayAlert("You're not signed in!");

        } else if (!clientContext.conversation.hasCurrent()) {
            // if the user did not select or add a conversation
            displayAlert("Add or click a conversation on the right!");

        } else {
            String messageText = input.getText();
            Text inputText = new Text(messageText);
            TextFlow inputFlow = new TextFlow(inputText);
            messageList.addAll(inputFlow);
            if (!messageText.isEmpty() && messageText.length() > 0) {
            	Uuid currentUserId = clientContext.user.getCurrent().id;
                // add message to current conversation
                clientContext.message.addMessage(currentUserId,
                    clientContext.conversation.getCurrentId(), messageText);

                // increase user message count
                clientContext.user.increaseMessageCount(currentUserId);

                // populate the list of messages with the current conversation's updated messages
                fillMessagesList(clientContext.conversation.getCurrent());
            }
        }
    }

    /**
    * Displays an error message
    * @param warningMessage  the error message to be displayed
    */
    private void displayAlert(String warningMessage) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setHeaderText(warningMessage);
        alert.showAndWait();
    }


    /**
    * Populates the list of conversations to be displayed
    * @param conversations  the current conversations that will be cleared & updated
    */
    private void fillConversationsList(ListView<String> conversations) {
        clientContext.conversation.updateAllConversations(false);
        conversations.getItems().clear();

        for (final ConversationSummary conv : clientContext.conversation.getConversationSummaries()) {
            convoList.addAll(conv.title);
        }
    }

    /**
    * Populates the list of messages to be displayed and also checks if the sender needs to colored
    * @param conversation  the contents of the current conversation that the method uses to
    *                      to get usernames, time of creation, etc.
    */
    private void fillMessagesList(ConversationSummary conversation) {
        messages.getItems().clear();

        for (final Message m : clientContext.message.getConversationContents(conversation)) {
            // Display author name if available.  Otherwise display the author UUID.
            final String authorName = clientContext.user.getName(m.author);
            userName = new Text(authorName + ": ");

            colorizeUsername(m.author);

            final String displayString = String.format("[%s]: %s", m.creation, m.content);

            Text displayStringText = new Text(displayString);
            TextFlow displayStringFlow = new TextFlow(userName, displayStringText);
            messageList.addAll(displayStringFlow);
      }
    }

    /**
    * Populates the list of users to be displayed and also checks if their names needs to colored
    * @param users  the current list of users that will be cleared & updated
    */
    private void fillUserList(ListView<Text> users) {
        clientContext.user.updateUsers();
        users.getItems().clear();

        User currentUser = clientContext.user.getCurrent();
        for (final User u : clientContext.user.getUsers()) {
        	if (!Uuid.equals(u.id, currentUser.id)) {
                userName = new Text(u.name);
                colorizeUsername(u.id);
        		usersList.add(userName);
            }
        }
    }

    // TODO: set up a loop to where this updates every half sec or so
    /**
    * When the user presses the update button, update the GUI by clearing & and filling
    * all the lists that will be displayed with updated content
    */
    private void updateGUI(ActionEvent e) {
        fillUserList(users);
        fillConversationsList(conversations);
        clientContext.message.updateMessages(true);
        fillMessagesList(clientContext.conversation.getCurrent());
    }

    // TODO: add more colors
    /**
    * Colorizes user based on how many messages has been sent
    * @param userID  the ID of the user to have their name colorized
    */
    private void colorizeUsername(Uuid userID) {
        int messageCount = clientContext.user.getMessageCount(userID);
        if (messageCount >= 10 && messageCount < 20) {
            userName.setFill(Color.RED);
        } else if (messageCount >= 20 && messageCount < 30) {
            userName.setFill(Color.BLUE);
        } else if (messageCount >= 30 && messageCount < 40) {
            userName.setStyle("-fx-fill: linear-gradient(from 0% 0% to 100% 200%, repeat, aqua 0%, red 50%)");
        }
    }
}
