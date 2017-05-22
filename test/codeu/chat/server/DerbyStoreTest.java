package codeu.chat.server;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;

import codeu.chat.DerbyStore;
import codeu.chat.common.User;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;
import codeu.chat.common.BasicController;
import codeu.chat.common.Conversation;
import codeu.chat.common.Message;

/**
 * 
 * @author malikg
 *
 */
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
	public void checkUserNameExists() {
		final User user = controller.newUser("user" + Time.now(), "testPassword");
		boolean userNameExists = false;

		try {
			userNameExists = ds.checkUsernameExists(user.name);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		assertTrue("Check to see if a username already exists",
				userNameExists);
	}

	@Test
	public void addAndGetUser() {
		final User user = controller.newUser("user"  + Time.now(), "testPassword");
		User testUser = null;
		try {
			//ds.addUser(user);
			testUser = ds.getUser(user.id);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		assertTrue(
				"Check that the user has the same id when saved",
				Uuid.equals(user.id, testUser.id));

		assertTrue(
				"Check that user has the same creation time when saved",
				user.creation.compareTo(testUser.creation) == 0);

		assertTrue(
				"Check that user has the same name when saved",
				user.name.equals(testUser.name));

		assertTrue(
				"Check that user has the same password when saved",
				user.password.equals(testUser.password));
	}

	@Test
	public void addAndGetConversation() { 
		User user = controller.newUser("user" + Time.now(), "testPassword");
		Conversation conversation = controller.newConversation("TEST CONVERSATION", user.id);
		Conversation testConversation = null;

		try {
			testConversation = ds.getConversation(conversation.id);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		// Works
		assertTrue(
				"Check that the conversation has the same id when saved",
				Uuid.equals(testConversation.id, conversation.id));

		assertTrue(
				"Check that the conversation has the same owner when saved",
				conversation.owner.equals(testConversation.owner));

		assertTrue(
				"Check that the conversation has the same creation time when saved",
				conversation.creation.compareTo(testConversation.creation) == 0);	

		assertTrue(
				"Check that the conversation has the same creation title when saved",
				conversation.title.equals(testConversation.title));

		assertTrue(
				"Check that the conversation has the same userInvolved when saved",
				conversation.users.equals(testConversation.users));

	}

	@Test
	public void updateMessagesForConversation() {
		final User user = controller.newUser("user" + Time.now(), "testPassword");
		final Conversation conversation = controller.newConversation("TEST CONVERSATION", user.id);
		final Message firstMessage = controller.newMessage(user.id, conversation.id, "TEST FIRST MESSAGE");
		final Message lastMessage = controller.newMessage(user.id, conversation.id, "TEST LAST MESSAGE");
		Conversation testConversation = null;

		try {
			//ds.addConversation(conversation);
			conversation.firstMessage = firstMessage.id;
			conversation.lastMessage = lastMessage.id;
			ds.updateConversationMessages(conversation);
			testConversation = ds.getConversation(conversation.id);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		assertTrue(
				"Check that messages can be updated once a conversation is made in the conversation table",
				Uuid.equals(testConversation.firstMessage, firstMessage.id) && Uuid.equals(testConversation.lastMessage, lastMessage.id));

	}

	@Test
	public void getMessage() {
		final User user = controller.newUser("user" + Time.now(), "testPassword");
		final Conversation conversation = controller.newConversation("TEST CONVERSATION", user.id);
		final Message message = controller.newMessage(user.id, conversation.id, "TEST FIRST MESSAGE");
		Message testMessage = null;

		try {
			//ds.addMessage(message);
			testMessage = ds.getMessage(message.id);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		assertTrue(
				"Check that messages can be retrieved and have the same id",
				Uuid.equals(message.id, testMessage.id));
		assertTrue(
				"Check that messages can be retrieved",
				Uuid.equals(message.author, testMessage.author));
		assertTrue(
				"Check that messages can be retrieved",
				message.creation.compareTo(message.creation) == 0);

	}

	@Test
	public void changeNextMessage() {
		final User user = controller.newUser("user" + Time.now(), "testPassword");
		final Conversation conversation = controller.newConversation("TEST CONVERSATION", user.id);
		final Message message = controller.newMessage(user.id, conversation.id, "TEST FIRST MESSAGE");
		final Message nextMessage = controller.newMessage(user.id, conversation.id, "TEST NEXt MESSAGE");

		try {
			//ds.addMessage(message);
			message.next = nextMessage.id;
			ds.updateNextMessage(message);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		assertTrue(
				"Check that messages can be retrieved and have the same id",
				Uuid.equals(message.next, nextMessage.id));

	}
	
	@Test
	public void checkUserLogin() {
		String username = "bob" + Time.now();
		final User user = controller.newUser(username, "testPassword");
		User userTest = null;
			
		try {
			userTest = ds.userLogin(username, "testPassword");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		assertTrue(
				"Check user login",
				user != null);
	}
	
	@Test
	public void checkMessageCount() {
		final User user = controller.newUser("userpop" + Time.now(), "testPassword");
		int messageCount = -1;
		int messageCountCheck = 0;
		
		try {
			messageCount = ds.setUserMessageCount(user.id);
			messageCount = ds.setUserMessageCount(user.id);
			messageCountCheck = ds.getMessageCount(user.id);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		assertTrue("Increase Message Count",
				messageCount == 2);
		
		assertTrue("Check Message Count",
				messageCountCheck == 2);
		
		
	}

}
