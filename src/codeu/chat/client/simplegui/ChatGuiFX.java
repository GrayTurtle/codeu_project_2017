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
    
    private static View view2;

    public void setContext(Controller controller, View view) {
    	clientContext = new ClientContext(controller, view);
    	view2 = view;
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
        this.mainPage = new MainChatPage(ChatGuiFX.clientContext, view2);
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
    
    /*public MainChatPage getMainPage() {
    	while (view.mainChatPage == null) {
    		System.out.println("NULL BOI");
    	}
    	return view.mainChatPage;
    }*/
}
