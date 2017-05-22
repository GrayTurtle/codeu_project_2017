package codeu.chat.common;

import codeu.chat.util.store.Store;

import java.io.IOException;
import java.sql.SQLException;

import codeu.chat.util.Uuid;

/**
 * 
 * @author malikg
 *
 */
public interface DerbyDatabaseInteractions {
	
	// USER
	/**
	 * @return list of users stored in the database.
	 */
	public Store<Uuid, User> getAllUsers();
	
	/**
	 * 
	 * @param userid
	 * @return a specific user stored stored in the database.
	 * @throws SQLException
	 * @throws IOException
	 */
	public User getUser(Uuid userid) throws SQLException, IOException;
	
	/**
	 * 
	 * Adds a user to the database.
	 * @param User 
	 * @throws SQLException
	 */
	public void addUser(User user) throws SQLException;
	
	/**
	 * 
	 * @param username
	 * @param password
	 * @return User object stored in database after
	 * 		   verifying username and password.
	 * @throws SQLException
	 * @throws IOException
	 */
	public User userLogin(String username, String password)  throws SQLException, IOException;
	
	/**
	 * 
	 * @param name
	 * @return  a boolean on whether the username exists.
	 * @throws SQLException
	 */
	public boolean checkUsernameExists(String username) throws SQLException;
	
	// CONVERSATION
	/**
	 * 
	 * @return list of conversations stored in the database.
	 */
	public Store<Uuid, Conversation> getAllConversations();
	
	/**
	 * 
	 * @param conversationid
	 * @return a specific conversation stored stored in the database.
	 * @throws SQLException
	 * @throws IOException
	 */
	public Conversation getConversation(Uuid conversationid) throws SQLException, IOException;
	
	/**
	 * 
	 * Adds a conversation to the database.
	 * @param conversation
	 * @throws SQLException
	 */
	public void addConversation(Conversation conversation) throws SQLException;
	
	/**
	 * 
	 * Update a created conversations firstMessage and lastMessage once 
	 * messages have been added.
	 * @param conversation
	 * @throws SQLException
	 */
	public void updateConversationMessages(Conversation conversation) throws SQLException;
	
	/**
	 * 
	 * Updates the next message given the current message.
	 * @param message
	 * @throws SQLException
	 */
	public void updateNextMessage(Message message) throws SQLException;
	
	// MESSAGE
	/**
	 * 
	 * @return list of messages stored in the database.
	 */
	public Store<Uuid, Message> getAllMessages();
	
	/**
	 * Adds a message to the database.
	 * @param message
	 * @throws SQLException
	 */
	public void addMessage(Message message) throws SQLException;
	
	/**
	 * 
	 * @param messageid
	 * @return a specific message.
	 * @throws SQLException
	 * @throws IOException
	 */
	public Message getMessage(Uuid messageid) throws SQLException, IOException;
	
	// MESSAGE COUNT 
	/**
	 * 
	 * @param userid
	 * @return the number of messages a user has successfully sent without
	 * updating the number stored.
	 * @throws SQLException
	 */
	public int getMessageCount(Uuid userid) throws SQLException;
	
	/**
	 * 
	 * @param userid
	 * @return the number of messages a user has sent while incrementing
	 * the number by 1.
	 * @throws SQLException
	 */
	public int setUserMessageCount(Uuid userid) throws SQLException;
	
	/**
	 * Adds the user and their initial message count to the database.
	 * @param String userid
	 * @throws SQLException
	 */
	public void addUserMessageCount(String userid) throws SQLException;

}
