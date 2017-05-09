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
			 "Check that user has a valid reference",
			 testUser == null);
		 
	}
	
	
}
