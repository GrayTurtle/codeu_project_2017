package codeu.chat;

/**
 * Stores all of the column names for each table
 * in the database.
 * 
 * Classes should not be instantiated.
 * 
 * @author malikg
 */
public class DBColumns {
	
	class UserTable {
		public final static String userid = "userid";
		public final static String name = "name";
		public final static String password = "password";
		public final static String creation = "creation";	
	}
	
	class ConversationTable {
		public final static String conversationid = "conversationid";
		public final static String owner = "owner";
		public final static String creation = "creation";
		public final static String title = "title";
		public final static String firstMessage = "firstMessage";
		public final static String lastMessage = "lastMessage";
	}
	
	class MessagesTable {
		public final static String messageid = "messageid";
		public final static String previousMessage = "previousMessage";
		public final static String creation = "creation";
		public final static String author = "author";
		public final static String content = "content";
		public final static String nextMessage = "nextMessage";
	}
	
	class ChatParticipantTable {
		public final static String conversationid = "conversationid";
		public final static String userid = "userid";
	}
	
	class UserMessageCountTable {
		public final static String messageCount = "messageCount";
	}

}
