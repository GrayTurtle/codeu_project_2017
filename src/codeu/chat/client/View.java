// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package codeu.chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

import codeu.chat.client.simplegui.ChatGuiFX;
import codeu.chat.client.simplegui.MainChatPage;
import codeu.chat.common.BasicView;
import codeu.chat.common.Conversation;
import codeu.chat.common.ConversationSummary;
import codeu.chat.common.LogicalView;
import codeu.chat.common.Message;
import codeu.chat.common.NetworkCode;
import codeu.chat.common.User;
import codeu.chat.util.Logger;
import codeu.chat.util.Serializers;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;
import codeu.chat.util.connections.Connection;
import codeu.chat.util.connections.ConnectionSource;
import javafx.application.Platform;

// VIEW
//
// This is the view component of the Model-View-Controller pattern used by the
// the client to retrieve readonly data from the server. All methods are blocking
// calls.
public final class View implements BasicView, LogicalView{

  private final static Logger.Log LOG = Logger.newLog(View.class);

  private final Connection source;
  
  private ClientConnection client;
  
  // Pauses the main thread to allow ClientConnection 
  // to parse the newly received data.
  public CountDownLatch latch = new CountDownLatch(1);
  
  // Post-processing Variables for Client and Server
  private User signedInUser = null;
  // getUsersExcluding()
  private Collection<User> users = new ArrayList<>();
  // getAllConversations()
  private Collection<ConversationSummary> summaries;
  // getMessages()
  private Collection<Message> messages;
  // getMessageById
  private Message getMessageById;
  // getMessageCount
  int messageCount = 0;
  //  getConversations
  Collection<Conversation> conversations = new ArrayList<>();
  
  // FROM CONTROLLER
  // newUser
  private User newUser;
  private Conversation newConversation;
  private Message newMessage;
  
  // Reference to ClientContext
  public ClientContext clientContext;
  
  // Reference to the GUI
  public MainChatPage mainChatPage;
  

  /**
   * ClientConnections manages the clients connection on the client side
   * by checking for an incoming information and parsing it accordingly.
   * @author malikg
   *
   */
  class ClientConnection extends Thread {
	  private BufferedReader in;

	  @Override()
	  public void run() {
			  try {
				  in = new BufferedReader(new InputStreamReader(source.in()));
				  while (true) {
					  if (in.ready()) {
						      int type = Serializers.INTEGER.read(source.in());
						      System.out.println(type + " mapppsssss");
						      if (type == NetworkCode.NEW_USER_RESPONSE) {
						    	  LOG.info("A new user has been received.");
						    	  newUser = Serializers.nullable(User.SERIALIZER).read(source.in());
						    	  if (newUser != null) {
						    	  clientContext.user.updatedUsers(newUser);
						    		  Platform.runLater(new Runnable() {
							    		  
							    		  @Override
							    		  public void run() {
							    			  mainChatPage.fillNewUser(newUser);
							    		  }
							    	  });
						    	  }
						    	  latch.countDown();
						    	  latch = new CountDownLatch(1); 
						      }
						      else if (type == NetworkCode.NEW_CONVERSATION_RESPONSE) { 
						    	  newConversation = Serializers.nullable(Conversation.SERIALIZER).read(source.in());
						    	  LOG.info("A new conversation has been received.");
						    	  clientContext.conversation.updateConversation(newConversation);
						    	  Platform.runLater(new Runnable() {
						    		  
						    		  @Override
						    		  public void run() {
						    			  mainChatPage.fillNewConversation(newConversation);
						    		  }
						    	  });
						    	  latch.countDown();
						    	  latch = new CountDownLatch(1);   
						      }
						      else if (type == NetworkCode.NEW_MESSAGE_RESPONSE) { 
						    	  LOG.info("A new message has been received.");
						    	  newMessage = Serializers.nullable(Message.SERIALIZER).read(source.in());
						    	  Conversation parentConversation = Serializers.nullable(Conversation.SERIALIZER).read(source.in());
						    	  ConversationSummary current = clientContext.conversation.getCurrent();
						    	  User author = null;
						    	  if (clientContext.user.usersById.containsKey(newMessage.author)) {
						    		  author = clientContext.user.usersById.get(newMessage.author);
						    	  }
						    	  if (author != null && !author.name.equals(clientContext.user.getCurrent().name)) {
						    		  if (current.title.equals(parentConversation.title)) {
							    		  System.out.println("ADDING A MESSAGE");
							    		  Platform.runLater(new Runnable() {
								    		  
								    		  @Override
								    		  public void run() {
								    			  mainChatPage.fillNewMessage(newMessage);
								    		  }
								    	  });
						    		  }
						    	  }
						    	  latch.countDown();
						    	  latch = new CountDownLatch(1);  
						      }
						      else if (type == NetworkCode.CHECK_USER_RESPONSE) {
								  User validUser = Serializers.nullable(User.SERIALIZER).read(source.in());
								  signedInUser = validUser;
								  latch.countDown();
								  latch = new CountDownLatch(1);
							  }
							  else if (type == NetworkCode.GET_USERS_EXCLUDING_RESPONSE) {
								  users.addAll(Serializers.collection(User.SERIALIZER).read(source.in()));
								  latch.countDown();
								  latch = new CountDownLatch(1);
							  }
							  else if (type == NetworkCode.GET_ALL_CONVERSATIONS_RESPONSE) { 
								  summaries.addAll(Serializers.collection(ConversationSummary.SERIALIZER).read(source.in()));
								  latch.countDown();
								  latch = new CountDownLatch(1);
							  }
							  else if (type == NetworkCode.GET_MESSAGES_BY_RANGE_RESPONSE) { 
								  messages.addAll(Serializers.collection(Message.SERIALIZER).read(source.in()));
								  latch.countDown();
								  latch = new CountDownLatch(1);
							  }
							  else if (type == NetworkCode.GET_MESSAGE_BY_ID_RESPONSE) {
								  getMessageById = Message.SERIALIZER.read(source.in());
								  latch.countDown();
								  latch = new CountDownLatch(1);
							  }
							  else if (type == NetworkCode.GET_USER_MESSAGE_COUNT_RESPONSE) { 
								  messageCount = Serializers.INTEGER.read(source.in());
								  latch.countDown();
								  latch = new CountDownLatch(1);
							  }
							  else if (type == NetworkCode.GET_CONVERSATIONS_BY_ID_RESPONSE) { 
								  conversations.addAll(Serializers.collection(Conversation.SERIALIZER).read(source.in()));
								  latch.countDown();
								  latch = new CountDownLatch(1);
							  }
					  } 
					  Thread.sleep(10);
				  } 
			  }
			  catch (IOException ex) {
				  LOG.error("Buffered Reader did not work", ex);
			  }
			  catch (InterruptedException ex) {
				  LOG.error("Thread could not sleep", ex);
			  }
	  }

  }

  public View(Connection source) {
    this.source = source;
    this.client = new ClientConnection();
    client.start();
  }
  
  @Override
  public Collection<User> getUsers(Collection<Uuid> ids) {
    final Collection<User> users = new ArrayList<>();

    try {

      Serializers.INTEGER.write(source.out(), NetworkCode.GET_USERS_BY_ID_REQUEST);
      Serializers.collection(Uuid.SERIALIZER).write(source.out(), ids);

      if (Serializers.INTEGER.read(source.in()) == NetworkCode.GET_USERS_BY_ID_RESPONSE) {
        users.addAll(Serializers.collection(User.SERIALIZER).read(source.in()));
      } else {
        LOG.error("Response from server failed.");
      }

    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return users;
  }

  @Override
  public Collection<ConversationSummary> getAllConversations() {
	  summaries = new ArrayList<>();

    try {
      Serializers.INTEGER.write(source.out(), NetworkCode.GET_ALL_CONVERSATIONS_REQUEST);
      latch.await();
      
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return summaries;
  }

  @Override
  public Collection<Conversation> getConversations(Collection<Uuid> ids) {
    conversations = new ArrayList<>();

    try {
      Serializers.INTEGER.write(source.out(), NetworkCode.GET_CONVERSATIONS_BY_ID_REQUEST);
      Serializers.collection(Uuid.SERIALIZER).write(source.out(), ids);
      
      latch.await();
      
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return conversations;
  }

  
  @Override
  public Collection<Message> getMessages(Collection<Uuid> ids) {
    final Collection<Message> messages = new ArrayList<>();

    try {
      Serializers.INTEGER.write(source.out(), NetworkCode.GET_MESSAGES_BY_ID_REQUEST);
      Serializers.collection(Uuid.SERIALIZER).write(source.out(), ids);

      if (Serializers.INTEGER.read(source.in()) == NetworkCode.GET_MESSAGES_BY_ID_RESPONSE) {
        messages.addAll(Serializers.collection(Message.SERIALIZER).read(source.in()));
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return messages;
  }

  @Override
  public Uuid getUserGeneration() {
    Uuid generation = Uuid.NULL;

    try {

      Serializers.INTEGER.write(source.out(), NetworkCode.GET_USER_GENERATION_REQUEST);

      if (Serializers.INTEGER.read(source.in()) == NetworkCode.GET_USER_GENERATION_RESPONSE) {
        generation = Uuid.SERIALIZER.read(source.in());
      } else {
        LOG.error("Response from server failed");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return generation;
  }

  @Override
  public Collection<User> getUsersExcluding(Collection<Uuid> ids) {
    users = new ArrayList<>();

    try {
      Serializers.INTEGER.write(source.out(), NetworkCode.GET_USERS_EXCLUDING_REQUEST);
      Serializers.collection(Uuid.SERIALIZER).write(source.out(), ids);
      
      latch.await();
      
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return users;
  }

  @Override
  public Collection<Conversation> getConversations(Time start, Time end) {
    final Collection<Conversation> conversations = new ArrayList<>();

    try {

      Serializers.INTEGER.write(source.out(), NetworkCode.GET_CONVERSATIONS_BY_TIME_REQUEST);
      Time.SERIALIZER.write(source.out(), start);
      Time.SERIALIZER.write(source.out(), end);

      if (Serializers.INTEGER.read(source.in()) == NetworkCode.GET_CONVERSATIONS_BY_TIME_RESPONSE) {
        conversations.addAll(Serializers.collection(Conversation.SERIALIZER).read(source.in()));
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return conversations;
  }

  @Override
  public Collection<Conversation> getConversations(String filter) {
    final Collection<Conversation> conversations = new ArrayList<>();

    try {

      Serializers.INTEGER.write(source.out(), NetworkCode.GET_CONVERSATIONS_BY_TITLE_REQUEST);
      Serializers.STRING.write(source.out(), filter);

      if (Serializers.INTEGER.read(source.in()) == NetworkCode.GET_CONVERSATIONS_BY_TITLE_RESPONSE) {
        conversations.addAll(Serializers.collection(Conversation.SERIALIZER).read(source.in()));
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return conversations;
  }

  @Override
  public Collection<Message> getMessages(Uuid conversation, Time start, Time end) {
    final Collection<Message> messages = new ArrayList<>();

    try {

      Serializers.INTEGER.write(source.out(), NetworkCode.GET_MESSAGES_BY_TIME_REQUEST);
      Time.SERIALIZER.write(source.out(), start);
      Time.SERIALIZER.write(source.out(), end);

      if (Serializers.INTEGER.read(source.in()) == NetworkCode.GET_MESSAGES_BY_TIME_RESPONSE) {
        messages.addAll(Serializers.collection(Message.SERIALIZER).read(source.in()));
      } else {
        LOG.error("Response from server failed.");
      }

    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return messages;
  }

  @Override
  public Collection<Message> getMessages(Uuid rootMessage, int range) {
      messages = new ArrayList<>();

    try {

      Serializers.INTEGER.write(source.out(), NetworkCode.GET_MESSAGES_BY_RANGE_REQUEST);
      Uuid.SERIALIZER.write(source.out(), rootMessage);
      Serializers.INTEGER.write(source.out(), range);
      
      latch.await();

    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return messages;
  }

  public User checkUserLogin(String name, String password) {
	 try {
		  Serializers.INTEGER.write(source.out(), NetworkCode.CHECK_USER_REQUEST);
		  Serializers.STRING.write(source.out(), name);
		  Serializers.STRING.write(source.out(), password);
		  
		  // Pause this thread to allow the ClientConnection
		  // Thread to run and release the latch.
		  latch.await();
		  
	  } catch (Exception ex) {
		  System.out.println("ERROR: Exception during call on server. Check log for details.");
	      LOG.error(ex, "Exception during call on server.");
	  }
	 
	  return signedInUser;
  }

  public int getMessageCount(Uuid userid) {
	  try {
		  Serializers.INTEGER.write(source.out(), NetworkCode.GET_USER_MESSAGE_COUNT_REQUEST);
		  Uuid.SERIALIZER.write(source.out(), userid);

		  latch.await();
	  }
	  catch (Exception ex) {
	      System.out.println("ERROR: Exception during call on server. Check log for details.");
	      LOG.error(ex, "Exception during call on server.");
	    }

  return messageCount;
  }
  
  public Message getMessageById(Uuid messageid) {
	  getMessageById = null;
	  try {
		  Serializers.INTEGER.write(source.out(), NetworkCode.GET_MESSAGE_BY_ID_REQUEST);
		  Uuid.SERIALIZER.write(source.out(), messageid);
		  
		  latch.await();
	  }
	  catch (Exception ex) {
	      System.out.println("ERROR: Exception during call on server. Check log for details.");
	      LOG.error(ex, "Exception during call on server.");
	    }
	  
  return getMessageById;
  }
  
  
  // Added methods from the controller
  // NOTE: These methods have been added from the controller
  // to unify where information was being received from the server
  // and where the information needed to be given. Having these 
  // two things in two separate locations would be more complex.
  public User newUser(String name, String password) {
	    newUser = null;

	    try {

	      Serializers.INTEGER.write(source.out(), NetworkCode.NEW_USER_REQUEST);
	      Serializers.STRING.write(source.out(), name);
	      
	      // ADDED BY MALIK
	      Serializers.STRING.write(source.out(), password);
	      
	      LOG.info("newUser: Request completed.");
	      
	      latch.await();
	    } catch (Exception ex) {
	      System.out.println("ERROR: Exception during call on server. Check log for details.");
	      LOG.error(ex, "Exception during call on server.");
	    }

	    return newUser;
}
  
 public Conversation newConversation(String title, Uuid owner)  {
	    newConversation = null;

	    try {

	      Serializers.INTEGER.write(source.out(), NetworkCode.NEW_CONVERSATION_REQUEST);
	      Serializers.STRING.write(source.out(), title);
	      Uuid.SERIALIZER.write(source.out(), owner);
	      
	      latch.await();

	    } catch (Exception ex) {
	      System.out.println("ERROR: Exception during call on server. Check log for details.");
	      LOG.error(ex, "Exception during call on server.");
	    }

	    return newConversation;
	  }
  
  public Message newMessage(Uuid author, Uuid conversation, String body) {
	    newMessage = null;

	    try {

	      Serializers.INTEGER.write(source.out(), NetworkCode.NEW_MESSAGE_REQUEST);
	      Uuid.SERIALIZER.write(source.out(), author);
	      Uuid.SERIALIZER.write(source.out(), conversation);
	      Serializers.STRING.write(source.out(), body);
	      Serializers.nullable(Conversation.SERIALIZER).write(source.out(), clientContext.message.conversationHead);

	      latch.await();

	    } catch (Exception ex) {
	      System.out.println("ERROR: Exception during call on server. Check log for details.");
	      LOG.error(ex, "Exception during call on server.");
	    }

	    return newMessage;
	  }

}
