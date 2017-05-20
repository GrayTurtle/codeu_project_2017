package codeu.chat.client.simplegui;

import codeu.chat.client.ClientContext;
import codeu.chat.client.ClientUser;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Created by Gabe on 5/19/17.
 */
public class SignInPage {

    private static final String SIGNIN_ERROR_MESSAGE = "Your username or password does not match. Have you signed up yet?";
    private static final String SIGNUP_ERROR_MESSAGE = "Sorry, that username already exists. Please choose a different one.";
    private static final String BADCHAR_ERROR_MESSAGE = "Usernames and passwords can only be composed of letters and numbers, no special characters.";

    private ChatGuiFX chatGuiFX;        // Handles different data and transition between scenes
    private Scene signInScene;

    // Takes input for username and password for sign in/up
    private TextField userInput;
    private PasswordField passInput;

    // Displays error messages for when the user can't sign in (and why)
    private Label errorLabel;


    public SignInPage(ChatGuiFX chatGuiFX) {

        this.chatGuiFX = chatGuiFX;

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

        signInScene = new Scene(signInPane, ChatGuiFX.WINDOW_WIDTH, ChatGuiFX.WINDOW_HEIGHT);
    }

    /**
     * When the sign in button is clicked, it takes the username & password from the
     * input fields and checks if they are valid inputs. If they are, sign in the user
     * and change to main chat
     */
    private void signInButtonClicked(ActionEvent e) {
        String username = userInput.getText();
        String password = passInput.getText();

        if (ClientUser.isValidInput(username) && ClientUser.isValidInput(password)) {
            if (ChatGuiFX.getClientContext().user.signInUser(username, password)) {

                // Populates main page with data and switches to that scene
                chatGuiFX.switchToMainPage();
            }
            else {
                errorLabel.setText(SIGNIN_ERROR_MESSAGE);
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
            if (ChatGuiFX.getClientContext().user.addUser(username, password)) {

                // Populates main page with data and switches to that scene
                chatGuiFX.switchToMainPage();
            }
            else {
                errorLabel.setText(SIGNUP_ERROR_MESSAGE);
            }
        }
        else {
            errorLabel.setText(BADCHAR_ERROR_MESSAGE);
        }
    }

    /**
     * Gets the collection of UI elements making up the SignIn Page
     * @return
     */
    public Scene getSignInScene() {
        return signInScene;
    }
}
