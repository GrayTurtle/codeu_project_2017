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
  private CountDownLatch latch = new CountDownLatch(1);
  
  // Post-processing Variables for Client and Server
  private User signedInUser = null;
  // getUsersExcluding()
  Collection<User> users = new ArrayList<>();
  // getAllConversations()
  Collection<ConversationSummary> summaries;
  // getMessages()
  Collection<Message> messages;
  

  
  class ClientConnection extends Thread {
	  private Connection source2;
	  private BufferedReader in;

	  public ClientConnection(Connection source) {
		  this.source2 = source;
	  }

	  @Override()
	  public void run() {
			  try {
				  in = new BufferedReader(new InputStreamReader(source.in()));
				  while (true) {
					  if (in.ready()) {
						      int type = Serializers.INTEGER.read(source.in());
						      if (type == NetworkCode.NEW_USER_RESPONSE) {} 
							  if (type == NetworkCode.CHECK_USER_RESPONSE) {
								  User validUser = Serializers.nullable(User.SERIALIZER).read(source.in());
								  System.out.println((validUser != null) ?  validUser.name + " was received from the server..." : " valid user is null...");
								  signedInUser = validUser;
								  latch.countDown();
								  latch = new CountDownLatch(1);
							  }
							  else if (type == NetworkCode.GET_USERS_EXCLUDING_RESPONSE) {
								  System.out.println("Got Users Excluding....");
								  users.addAll(Serializers.collection(User.SERIALIZER).read(source.in()));
								  latch.countDown();
								  latch = new CountDownLatch(1);
							  }
							  else if (type == NetworkCode.GET_ALL_CONVERSATIONS_RESPONSE) { 
								  System.out.println("Got All Conversations...");
								  summaries.addAll(Serializers.collection(ConversationSummary.SERIALIZER).read(source.in()));
								  latch.countDown();
								  latch = new CountDownLatch(1);
							  }
							  else if (type == NetworkCode.GET_MESSAGES_BY_RANGE_RESPONSE) { 
								  System.out.println("Getting Messages...");
								  messages.addAll(Serializers.collection(Message.SERIALIZER).read(source.in()));
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
    this.client = new ClientConnection(source);
    client.start();
  }
  
  @Override
  public Collection<User> getUsers(Collection<Uuid> ids) {
	 System.out.println("HERE 1");
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
	  System.out.println("HERE 2");
	  summaries = new ArrayList<>();

    try {
      Serializers.INTEGER.write(source.out(), NetworkCode.GET_ALL_CONVERSATIONS_REQUEST);
      
      latch.await();

      /*if (Serializers.INTEGER.read(source.in()) == NetworkCode.GET_ALL_CONVERSATIONS_RESPONSE) {
        summaries.addAll(Serializers.collection(ConversationSummary.SERIALIZER).read(source.in()));
      } else {
        LOG.error("Response from server failed.");
      }*/

    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return summaries;
  }

  @Override
  public Collection<Conversation> getConversations(Collection<Uuid> ids) {
	  System.out.println("HERE 3");
    final Collection<Conversation> conversations = new ArrayList<>();

    try {

      Serializers.INTEGER.write(source.out(), NetworkCode.GET_CONVERSATIONS_BY_ID_REQUEST);
      Serializers.collection(Uuid.SERIALIZER).write(source.out(), ids);

      if (Serializers.INTEGER.read(source.in()) == NetworkCode.GET_CONVERSATIONS_BY_ID_RESPONSE) {
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
  public Collection<Message> getMessages(Collection<Uuid> ids) {
	  System.out.println("HERE 4");
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
	  System.out.println("HERE 5");
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
	System.out.println("HERE 6");
    users = new ArrayList<>();

    try {
      Serializers.INTEGER.write(source.out(), NetworkCode.GET_USERS_EXCLUDING_REQUEST);
      Serializers.collection(Uuid.SERIALIZER).write(source.out(), ids);
      
      System.out.println("WAITING TO GET USER EXCLUDING");
      latch.await();
      
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return users;
  }

  @Override
  public Collection<Conversation> getConversations(Time start, Time end) {
	  System.out.println("HERE 7");
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
	  System.out.println("HERE 8");
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
	  System.out.println("HERE 9");
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
	  System.out.println("HERE 10");
      messages = new ArrayList<>();

    try {

      Serializers.INTEGER.write(source.out(), NetworkCode.GET_MESSAGES_BY_RANGE_REQUEST);
      Uuid.SERIALIZER.write(source.out(), rootMessage);
      Serializers.INTEGER.write(source.out(), range);

      /*if (Serializers.INTEGER.read(source.in()) == NetworkCode.GET_MESSAGES_BY_RANGE_RESPONSE) {
        messages.addAll(Serializers.collection(Message.SERIALIZER).read(source.in()));
      } else {
        LOG.error("Response from server failed.");
      }*/
      
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
		  System.out.println("WAITING TO GET USER");
		  latch.await();
		  
	  } catch (Exception ex) {
		  System.out.println("ERROR: Exception during call on server. Check log for details.");
	      LOG.error(ex, "Exception during call on server.");
	  }
	 
	  //System.out.println(signedInUser.name + " has been received");
	  return signedInUser;
  }

  public int getMessageCount(Uuid userid) {
	  int messageCount = 0;
	  System.out.println("HERE 12");
	  try {
		  Serializers.INTEGER.write(source.out(), NetworkCode.GET_USER_MESSAGE_COUNT_REQUEST);
		  Uuid.SERIALIZER.write(source.out(), userid);

		  if (Serializers.INTEGER.read(source.in()) == NetworkCode.GET_USER_MESSAGE_COUNT_RESPONSE) {
			  messageCount = Serializers.INTEGER.read(source.in());
		  }
		  else {
		      LOG.error("Response from server failed.");
		  }
	  }
	  catch (Exception ex) {
	      System.out.println("ERROR: Exception during call on server. Check log for details.");
	      LOG.error(ex, "Exception during call on server.");
	    }

  return messageCount;
  }
  
  public Message getMessageById(Uuid messageid) {
	  Message message = null;
	  System.out.println("HERE 13");
	  try {
		  Serializers.INTEGER.write(source.out(), NetworkCode.GET_MESSAGE_BY_ID_REQUEST);
		  Uuid.SERIALIZER.write(source.out(), messageid);

		  if (Serializers.INTEGER.read(source.in()) == NetworkCode.GET_MESSAGE_BY_ID_RESPONSE) {
			  message = Message.SERIALIZER.read(source.in());
		  }
		  else {
		      LOG.error("Response from server failed.");
		  }
	  }
	  catch (Exception ex) {
	      System.out.println("ERROR: Exception during call on server. Check log for details.");
	      LOG.error(ex, "Exception during call on server.");
	    }
	  
  return message;
  }

}
