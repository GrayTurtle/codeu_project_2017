package codeu.chat.common;

import codeu.chat.util.store.Store;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import codeu.chat.util.Uuid;

public interface DerbyDatabaseInteractions {
	
	// USER
	public Store<Uuid, User> getAllUsers();
	
	public User getUser(Uuid userid) throws SQLException, IOException;
	
	public void addUser(User u) throws SQLException;
	
	public User userLogin(String name, String password)  throws SQLException, IOException;
	
	public boolean checkUsernameExists(String name) throws SQLException;
	
	// CONVERSATION
	public Store<Uuid, Conversation> getAllConversations();
	
	public Conversation getConversation(Uuid conversationid) throws SQLException, IOException;
	
	public void addConversation(Conversation c) throws SQLException;
	
	public void updateConversationMessages(Conversation c) throws SQLException;
	
	public void updateNextMessage(Message m) throws SQLException;
	
	// MESSAGE
	public Store<Uuid, Message> getAllMessages();
	
	public void addMessage(Message m) throws SQLException;
	
	public Message getMessage(Uuid messageid) throws SQLException, IOException;
	
	// MESSAGE COUNT 
	public int getMessageCount(Uuid uuid) throws SQLException;
	
	public int setUserMessageCount(Uuid userid) throws SQLException;
	
	public void addUserMessageCount(String uuid) throws SQLException;

}
