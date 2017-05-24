package codeu.chat.client.simplegui;

import javafx.application.Application;
import javafx.stage.Stage;

import codeu.chat.client.ClientContext;
import codeu.chat.client.Controller;
import codeu.chat.client.View;
import codeu.chat.util.Logger;

public final class ChatGuiFX extends Application {

    private final static Logger.Log LOG = Logger.newLog(ChatGuiFX.class);

    protected static final double WINDOW_WIDTH = 1000;
    protected static final double WINDOW_HEIGHT = 500;

    // Holds the scene that user is currently viewing
    private Stage thestage;

    public MainChatPage mainPage;

    // variable that has the current state of the client (current user, conversation, etc)
    private static ClientContext clientContext;
    
    // variable that will keep reference to the view,
    // so that the view can refer to the mainpage GUI
    private static View clientView;

    public void setContext(Controller controller, View view) {
    	clientContext = new ClientContext(controller, view);
    	clientView = view;
    }

    public void launch(Controller controller, View view) {
    	setContext(controller, view);
    	LOG.info("Launching application");
        Application.launch(ChatGuiFX.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Initialize the main stage
        this.thestage = primaryStage;

        // Instantiate classes for different pages
        SignInPage signInPage = new SignInPage(this, ChatGuiFX.clientContext);
        this.mainPage = new MainChatPage(ChatGuiFX.clientContext, clientView);
        // Start up with the sign in page
        thestage.setScene(signInPage.getSignInScene());
        thestage.show();
    }

    /**
     * Populates the main page with data and switches to that scene
     *
     */
    public void switchToMainPage() {
        mainPage.populate();
        thestage.setScene(mainPage.getMainChatScene());
    }
}
