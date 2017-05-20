package codeu.chat.client.simplegui;

import codeu.chat.common.ConversationSummary;
import codeu.chat.common.Message;
import codeu.chat.common.User;
import codeu.chat.util.Uuid;
import codeu.chat.util.store.Store;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.Map;

/**
 * Created by Gabe on 5/19/17.
 */
public class MainChatPage {

    private Scene mainChatScene;

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

    public MainChatPage() {

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

        mainChatScene = new Scene(container, ChatGuiFX.WINDOW_WIDTH, ChatGuiFX.WINDOW_HEIGHT);
    }

    /**
     * When the add conversation button is pressed, it pops up a dialog for the user to
     * input a new conversation. If the conversation name is valid, add the conversation
     * to the list of conversations to be displayed.
     */
    private void addConversation(ActionEvent e) {
        if (ChatGuiFX.getClientContext().user.hasCurrent()) {
            // popup for the user to add a conversation
            TextInputDialog dialog = new TextInputDialog("Name your conversation!");
            dialog.setTitle(" ");
            dialog.setHeaderText("Create your conversation");

            // get input and add the name of the convo
            String name = dialog.showAndWait().get();

            Map<ConversationSummary, String> summariesSortedByCreationTime = ChatGuiFX.getClientContext().conversation.getSummariesByCreationTime();

            if (summariesSortedByCreationTime.containsValue(name)) {
                displayAlert("There is already a conversation with that name!");
                return;
            }

            if (!name.isEmpty() && name.length() > 0) {
                ChatGuiFX.getClientContext().conversation.startConversation(name, ChatGuiFX.getClientContext().user.getCurrent().id);
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
            ChatGuiFX.getClientContext().conversation.setCurrent(selectedConvo);
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
        Map<ConversationSummary, String> summariesSortedByCreationTime = ChatGuiFX.getClientContext().conversation.getSummariesByCreationTime();
        Store<String, ConversationSummary> summariesSortedByTitle = ChatGuiFX.getClientContext().conversation.getConversationSummariesStore();

        /**
         * This search for a ConversationSummary happens
         * in constant time since summariesSortedByTitle.at()
         * return an iterable with only one item, which is the
         * value of the given key.
         */
        if (summariesSortedByCreationTime.containsValue(title)) {
            for (ConversationSummary cs : summariesSortedByTitle.at(title)) {
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
        if (!ChatGuiFX.getClientContext().user.hasCurrent()) {
            // if the user is not signed in
            displayAlert("You're not signed in!");

        } else if (!ChatGuiFX.getClientContext().conversation.hasCurrent()) {
            // if the user did not select or add a conversation
            displayAlert("Add or click a conversation on the right!");

        } else {
            String messageText = input.getText();
            input.setText("");
            Text inputText = new Text(messageText);
            TextFlow inputFlow = new TextFlow(inputText);
            messageList.addAll(inputFlow);

            if (!messageText.isEmpty() && messageText.length() > 0) {
                Uuid currentUserId = ChatGuiFX.getClientContext().user.getCurrent().id;
                // add message to current conversation
                ChatGuiFX.getClientContext().message.addMessage(currentUserId,
                        ChatGuiFX.getClientContext().conversation.getCurrentId(), messageText);

                // increase user message count
                ChatGuiFX.getClientContext().user.increaseMessageCount(currentUserId);

                // populate the list of messages with the current conversation's updated messages
                fillMessagesList(ChatGuiFX.getClientContext().conversation.getCurrent());

                //reorder conversations list
                fillConversationsList(conversations);
            }
        }
    }

    /**
     * Displays an error message
     * @param warningMessage  the error message to be displayed
     */
    private void displayAlert(String warningMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(warningMessage);
        alert.showAndWait();
    }


    /**
     * Populates the list of conversations to be displayed
     * @param conversations  the current conversations that will be cleared & updated
     */
    private void fillConversationsList(ListView<String> conversations) {
        ChatGuiFX.getClientContext().conversation.updateAllConversations(false);
        conversations.getItems().clear();

        for (final ConversationSummary conv : ChatGuiFX.getClientContext().conversation.getSummariesByCreationTime().keySet()) {
            convoList.add(conv.title);
        }
    }

    /**
     * Populates the list of messages to be displayed and also checks if the sender needs to colored
     * @param conversation  the contents of the current conversation that the method uses to
     *                      to get usernames, time of creation, etc.
     */
    private void fillMessagesList(ConversationSummary conversation) {
        messages.getItems().clear();

        for (final Message m : ChatGuiFX.getClientContext().message.getConversationContents(conversation)) {
            // Display author name if available.  Otherwise display the author UUID.
            final String authorName = ChatGuiFX.getClientContext().user.getName(m.author);
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
        ChatGuiFX.getClientContext().user.updateUsers();
        users.getItems().clear();

        User currentUser = ChatGuiFX.getClientContext().user.getCurrent();
        for (final User u : ChatGuiFX.getClientContext().user.getUsers()) {
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
        ChatGuiFX.getClientContext().message.updateMessages(true);
        fillMessagesList(ChatGuiFX.getClientContext().conversation.getCurrent());
    }

    // TODO: add more colors
    /**
     * Colorizes user based on how many messages has been sent
     * @param userID  the ID of the user to have their name colorized
     */
    private void colorizeUsername(Uuid userID) {
        int messageCount = ChatGuiFX.getClientContext().user.getMessageCount(userID);
        if (messageCount >= 10 && messageCount < 20) {
            userName.setFill(Color.RED);
        } else if (messageCount >= 20 && messageCount < 30) {
            userName.setFill(Color.BLUE);
        } else if (messageCount >= 30 && messageCount < 40) {
            userName.setStyle("-fx-fill: linear-gradient(from 0% 0% to 100% 200%, repeat, aqua 0%, red 50%)");
        }
    }

    /**
     *
     * @return
     */
    public Scene getMainChatScene() {
        return mainChatScene;
    }

    /**
     * Populates the main chat page with data
     */
    public void populate() {

        fillMessagesList(ChatGuiFX.getClientContext().conversation.getCurrent());
        fillConversationsList(conversations);
        fillUserList(users);
    }
}