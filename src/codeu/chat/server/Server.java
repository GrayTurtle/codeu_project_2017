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


package codeu.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;

import codeu.chat.DerbyStore;
import codeu.chat.common.Conversation;
import codeu.chat.common.ConversationSummary;
import codeu.chat.common.LinearUuidGenerator;
import codeu.chat.common.Message;
import codeu.chat.common.NetworkCode;
import codeu.chat.common.Relay;
import codeu.chat.common.User;
import codeu.chat.util.Logger;
import codeu.chat.util.Serializers;
import codeu.chat.util.Time;
import codeu.chat.util.Timeline;
import codeu.chat.util.Uuid;
import codeu.chat.util.connections.Connection;

public final class Server {

  private static final Logger.Log LOG = Logger.newLog(Server.class);

  private static final int RELAY_REFRESH_MS = 5000;  // 5 seconds

  private final Timeline timeline = new Timeline();

  private final Uuid id;
  private final byte[] secret;

  private final Model model = new Model();
  private final View view = new View(model);
  private final Controller controller;

  private final Relay relay;
  private Uuid lastSeen = Uuid.NULL;
  
  
  // Post-processing Variables for Client and Server
  private User newUser;
  private Conversation newConversation;
  private Message newMessage;
  private Conversation parentConversation;

  public Server(final Uuid id, final byte[] secret, final Relay relay) {

    this.id = id;
    this.secret = Arrays.copyOf(secret, secret.length);

    this.controller = new Controller(id, model);
    this.relay = relay;

    timeline.scheduleNow(new Runnable() {
      @Override
      public void run() {
        try {

          LOG.info("Reading update from relay...");

          for (final Relay.Bundle bundle : relay.read(id, secret, lastSeen, 32)) {
            onBundle(bundle);
            lastSeen = bundle.id();
          }

        } catch (Exception ex) {

          LOG.error(ex, "Failed to read update from relay.");

        }

        timeline.scheduleIn(RELAY_REFRESH_MS, this);
      }
    });
  }

  /**
   * Handles any incoming information for a client and sends back
   * information to them. If any new users, conversations, or clients
   * are detected then it sends them to all connected clients.
   * @param connection
   */
  public void handleConnection(final Connection connection) {
	  try {
	  BufferedReader in = new BufferedReader(new InputStreamReader(connection.in()));
	  User prevUser = newUser;
	  Conversation prevConversation = newConversation;
	  Message prevMessage = newMessage;
	  	while (true) {
	  		if (in.ready()) {
				LOG.info("Handling connection...");
				
				final boolean success = onMessage(connection.in(), connection.out());
				LOG.info("Connection handled: %s", success ? "ACCEPTED" : "REJECTED");
			    prevUser = newUser;	   
			    prevMessage = newMessage;
			    prevConversation = newConversation;
	  		}
	  		else {
	  			   if (newUser != null && !newUser.equals(prevUser)) {
	  				   Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_USER_RESPONSE);
	  				   Serializers.nullable(User.SERIALIZER).write(connection.out(), newUser);
	  				   newUser = null;
	  			   }
	  			   if (newConversation != null && !newConversation.equals(prevConversation)) {
	  				   Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_CONVERSATION_RESPONSE);
	  				   Serializers.nullable(Conversation.SERIALIZER).write(connection.out(), newConversation);
	  				   newConversation = null;
	  			   }
	  			   if (newMessage != null && !newMessage.equals(prevMessage)) {
	  				  Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_MESSAGE_RESPONSE);
	  				  Serializers.nullable(Message.SERIALIZER).write(connection.out(), newMessage); 
	  				  Serializers.nullable(Conversation.SERIALIZER).write(connection.out(), parentConversation); 
	  				  newMessage = null;
	  			   }
	  			   
	  		}
	  		Thread.sleep(10);
	  	}
	  }
	  catch (IOException ex) {
		  LOG.error("Error in creating buffer", ex);
		  ex.printStackTrace();
	  }
	  catch (InterruptedException ex) {
		  LOG.error("Thread could not sleep", ex);
	  }
  }

  private boolean onMessage(InputStream in, OutputStream out) throws IOException {

    final int type = Serializers.INTEGER.read(in);

    if (type == NetworkCode.NEW_MESSAGE_REQUEST) {

      final Uuid author = Uuid.SERIALIZER.read(in);
      final Uuid conversation = Uuid.SERIALIZER.read(in);
      final String content = Serializers.STRING.read(in);
      parentConversation = Serializers.nullable(Conversation.SERIALIZER).read(in);

      newMessage = controller.newMessage(author, conversation, content);
      
      Serializers.INTEGER.write(out, NetworkCode.NEW_MESSAGE_RESPONSE);
      Serializers.nullable(Message.SERIALIZER).write(out, newMessage);
      Serializers.nullable(Conversation.SERIALIZER).write(out, parentConversation);

      timeline.scheduleNow(createSendToRelayEvent(
          author,
          conversation,
          newMessage.id));

    } else if (type == NetworkCode.NEW_USER_REQUEST) {

      final String name = Serializers.STRING.read(in);
      
      // ADDED by Malik
      final String password = Serializers.STRING.read(in);

      newUser = controller.newUser(name, password);

      Serializers.INTEGER.write(out, NetworkCode.NEW_USER_RESPONSE);
      Serializers.nullable(User.SERIALIZER).write(out, newUser);

    } else if (type == NetworkCode.NEW_CONVERSATION_REQUEST) {

      final String title = Serializers.STRING.read(in);
      final Uuid owner = Uuid.SERIALIZER.read(in);

      newConversation = controller.newConversation(title, owner);

      Serializers.INTEGER.write(out, NetworkCode.NEW_CONVERSATION_RESPONSE);
      Serializers.nullable(Conversation.SERIALIZER).write(out, newConversation);

    } else if (type == NetworkCode.GET_USERS_BY_ID_REQUEST) {

      final Collection<Uuid> ids = Serializers.collection(Uuid.SERIALIZER).read(in);

      final Collection<User> users = view.getUsers(ids);

      Serializers.INTEGER.write(out, NetworkCode.GET_USERS_BY_ID_RESPONSE);
      Serializers.collection(User.SERIALIZER).write(out, users);

    } else if (type == NetworkCode.GET_ALL_CONVERSATIONS_REQUEST) {

      final Collection<ConversationSummary> conversations = view.getAllConversations();

      Serializers.INTEGER.write(out, NetworkCode.GET_ALL_CONVERSATIONS_RESPONSE);
      Serializers.collection(ConversationSummary.SERIALIZER).write(out, conversations);

    } else if (type == NetworkCode.GET_CONVERSATIONS_BY_ID_REQUEST) {

      final Collection<Uuid> ids = Serializers.collection(Uuid.SERIALIZER).read(in);

      final Collection<Conversation> conversations = view.getConversations(ids);

      Serializers.INTEGER.write(out, NetworkCode.GET_CONVERSATIONS_BY_ID_RESPONSE);
      Serializers.collection(Conversation.SERIALIZER).write(out, conversations);

    } else if (type == NetworkCode.GET_MESSAGES_BY_ID_REQUEST) {

      final Collection<Uuid> ids = Serializers.collection(Uuid.SERIALIZER).read(in);

      final Collection<Message> messages = view.getMessages(ids);

      Serializers.INTEGER.write(out, NetworkCode.GET_MESSAGES_BY_ID_RESPONSE);
      Serializers.collection(Message.SERIALIZER).write(out, messages);

    } else if (type == NetworkCode.GET_USER_GENERATION_REQUEST) {

      Serializers.INTEGER.write(out, NetworkCode.GET_USER_GENERATION_RESPONSE);
      Uuid.SERIALIZER.write(out, view.getUserGeneration());

    } else if (type == NetworkCode.GET_USERS_EXCLUDING_REQUEST) {

      final Collection<Uuid> ids = Serializers.collection(Uuid.SERIALIZER).read(in);

      final Collection<User> users = view.getUsersExcluding(ids);

      Serializers.INTEGER.write(out, NetworkCode.GET_USERS_EXCLUDING_RESPONSE);
      Serializers.collection(User.SERIALIZER).write(out, users);

    } else if (type == NetworkCode.GET_CONVERSATIONS_BY_TIME_REQUEST) {

      final Time startTime = Time.SERIALIZER.read(in);
      final Time endTime = Time.SERIALIZER.read(in);

      final Collection<Conversation> conversations = view.getConversations(startTime, endTime);

      Serializers.INTEGER.write(out, NetworkCode.GET_CONVERSATIONS_BY_TIME_RESPONSE);
      Serializers.collection(Conversation.SERIALIZER).write(out, conversations);

    } else if (type == NetworkCode.GET_CONVERSATIONS_BY_TITLE_REQUEST) {

      final String filter = Serializers.STRING.read(in);

      final Collection<Conversation> conversations = view.getConversations(filter);

      Serializers.INTEGER.write(out, NetworkCode.GET_CONVERSATIONS_BY_TITLE_RESPONSE);
      Serializers.collection(Conversation.SERIALIZER).write(out, conversations);

    } else if (type == NetworkCode.GET_MESSAGES_BY_TIME_REQUEST) {

      final Uuid conversation = Uuid.SERIALIZER.read(in);
      final Time startTime = Time.SERIALIZER.read(in);
      final Time endTime = Time.SERIALIZER.read(in);

      final Collection<Message> messages = view.getMessages(conversation, startTime, endTime);

      Serializers.INTEGER.write(out, NetworkCode.GET_MESSAGES_BY_TIME_RESPONSE);
      Serializers.collection(Message.SERIALIZER).write(out, messages);

    } else if (type == NetworkCode.GET_MESSAGES_BY_RANGE_REQUEST) {

      final Uuid rootMessage = Uuid.SERIALIZER.read(in);
      final int range = Serializers.INTEGER.read(in);

      final Collection<Message> messages = view.getMessages(rootMessage, range);

      Serializers.INTEGER.write(out, NetworkCode.GET_MESSAGES_BY_RANGE_RESPONSE);
      Serializers.collection(Message.SERIALIZER).write(out, messages);

    } else if (type == NetworkCode.CHECK_USER_REQUEST) {
    	
    	final String username = Serializers.STRING.read(in);
    	final String password = Serializers.STRING.read(in);
    	
    	User validUser = null;
    	try {
    		validUser = model.checkUserLogin(username, password);
    	}
    	catch (Exception ex) {
    		ex.printStackTrace();
    	}
    	
    	Serializers.INTEGER.write(out, NetworkCode.CHECK_USER_RESPONSE);
    	Serializers.nullable(User.SERIALIZER).write(out, validUser);

  	} else if (type == NetworkCode.GET_USER_MESSAGE_COUNT_REQUEST) {
  		final Uuid userid = Uuid.SERIALIZER.read(in);
  		
  		
  		int messageCount = 0;
  		try {
  			messageCount = model.getMessageCount(userid);
  		}
  		catch (Exception ex) {
  			ex.printStackTrace();
  		}
  		
  		Serializers.INTEGER.write(out, NetworkCode.GET_USER_MESSAGE_COUNT_RESPONSE);
  		Serializers.INTEGER.write(out, messageCount);
  		
  	} 
  	else if (type == NetworkCode.UPDATE_MESSAGE_COUNT_REQUEST) {
  		final Uuid userid = Uuid.SERIALIZER.read(in);
  		
  		int messageCount = 0;
  		try {
  			model.setMessageCount(userid);
  		}
  		catch (Exception ex) {
  			ex.printStackTrace();
  		}
  		
  		Serializers.INTEGER.write(out, NetworkCode.UPDATE_MESSAGE_COUNT_RESPONSE);
  		Serializers.INTEGER.write(out, messageCount);
  		
  	}
  	else if (type == NetworkCode.GET_MESSAGE_BY_ID_REQUEST) {
  		final Uuid messageid = Uuid.SERIALIZER.read(in);
  		Message message = null;
  		
  		try {
  			message = model.getMessageById(messageid);
  		}
  		catch (Exception ex) {
  			ex.printStackTrace();
  		}
  		
  		Serializers.INTEGER.write(out, NetworkCode.GET_MESSAGE_BY_ID_RESPONSE);
  		Message.SERIALIZER.write(out, message);
  		
  	} else {
      // In the case that the message was not handled make a dummy message with
      // the type "NO_MESSAGE" so that the client still gets something.

      Serializers.INTEGER.write(out, NetworkCode.NO_MESSAGE);

    }

    return true;
  }

  private void onBundle(Relay.Bundle bundle) {

    final Relay.Bundle.Component relayUser = bundle.user();
    final Relay.Bundle.Component relayConversation = bundle.conversation();
    final Relay.Bundle.Component relayMessage = bundle.user();

    User user = model.userById().first(relayUser.id());

    // TODO: THIS NEEDS TO BE LOOKED AT --- ADDING relayUser.text() for password will probably not work
    if (user == null) {
      user = controller.newUser(relayUser.id(), relayUser.text(), relayUser.text(), relayUser.time());
    }

    Conversation conversation = model.conversationById().first(relayConversation.id());

    if (conversation == null) {

      // As the relay does not tell us who made the conversation - the first person who
      // has a message in the conversation will get ownership over this server's copy
      // of the conversation.
      conversation = controller.newConversation(relayConversation.id(),
                                                relayConversation.text(),
                                                user.id,
                                                relayConversation.time());
    }

    Message message = model.messageById().first(relayMessage.id());

    if (message == null) {
      message = controller.newMessage(relayMessage.id(),
                                      user.id,
                                      conversation.id,
                                      relayMessage.text(),
                                      relayMessage.time());
    }
  }

  private Runnable createSendToRelayEvent(final Uuid userId,
                                          final Uuid conversationId,
                                          final Uuid messageId) {
    return new Runnable() {
      @Override
      public void run() {
        final User user = view.findUser(userId);
        final Conversation conversation = view.findConversation(conversationId);
        final Message message = view.findMessage(messageId);
        relay.write(id,
                    secret,
                    relay.pack(user.id, user.name, user.creation),
                    relay.pack(conversation.id, conversation.title, conversation.creation),
                    relay.pack(message.id, message.content, message.creation));
      }
    };
  }
}
