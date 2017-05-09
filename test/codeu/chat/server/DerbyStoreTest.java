package codeu.chat.server;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

import codeu.chat.DerbyStore;
import codeu.chat.common.User;
import codeu.chat.util.Uuid;
import codeu.chat.common.BasicController;
import codeu.chat.common.Conversation;
import codeu.chat.common.Message;

public class DerbyStoreTest {
	
	private DerbyStore ds;
	private Model model;
	private BasicController controller;
		
	@Before
	public void doBefore() {
		ds = new DerbyStore();
		model = new Model();
	    controller = new Controller(Uuid.NULL, model);
	}
	
	@Test
	public void addAndGetUser() {
		 final User user = controller.newUser("user");
		 User testUser = null;
		 try {
			 ds.addUser(user);
			 testUser = ds.getUser(user.id);
		 }
		 catch (Exception ex) {
			 ex.printStackTrace();
		 }
		 
		 assertFalse(
			 "Check that user can be added and removed successfully",
			 testUser == null);	 
	}
	
	@Test
	public void addAndGetConversation() { 
		final User user = controller.newUser("user");
		final Conversation conversation = controller.newConversation("TEST CONVERSATION", user.id);
		Conversation testConvo = null;
		
		try {
			 ds.addConversation(conversation);
			 testConvo = ds.getConversation(conversation.id);
		 }
		 catch (Exception ex) {
			 ex.printStackTrace();
		 }
		
		assertFalse(
				 "Check that conversation can be added and removed successfully",
				 testConvo == null);	
		
	}
	
	// TODO: Add a way to get messages based on conversations and test it
	// 		 Add a method to update the users involved for a conversation
	
	
	
	
	
	
}
