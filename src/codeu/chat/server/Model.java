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

import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;

import codeu.chat.DerbyStore;
import codeu.chat.common.Conversation;
import codeu.chat.common.ConversationSummary;
import codeu.chat.common.LinearUuidGenerator;
import codeu.chat.common.Message;
import codeu.chat.common.User;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;
import codeu.chat.util.store.Store;
import codeu.chat.util.store.StoreAccessor;

public final class Model {

  private static final Comparator<Uuid> UUID_COMPARE = new Comparator<Uuid>() {

    @Override
    public int compare(Uuid a, Uuid b) {

      if (a == b) { return 0; }

      if (a == null && b != null) { return -1; }

      if (a != null && b == null) { return 1; }

      final int order = Integer.compare(a.id(), b.id());
      return order == 0 ? compare(a.root(), b.root()) : order;
    }
  };

  private static final Comparator<Time> TIME_COMPARE = new Comparator<Time>() {
    @Override
    public int compare(Time a, Time b) {
      return a.compareTo(b);
    }
  };

  private static final Comparator<String> STRING_COMPARE = String.CASE_INSENSITIVE_ORDER;
  
  private final DerbyStore ds = new DerbyStore();

  //private final Store<Uuid, User> userById = new Store<>(UUID_COMPARE);
  
  // EDIT - Malik Graham
  // Object are now initialized with the information from the databases
  private final Store<Uuid, User> userById = ds.getAllUsers();
  private final Store<Time, User> userByTime = new Store<>(TIME_COMPARE);
  private final Store<String, User> userByText = new Store<>(STRING_COMPARE);

  //private final Store<Uuid, Conversation> conversationById = new Store<>(UUID_COMPARE);
  private final Store<Uuid, Conversation> conversationById = ds.getAllConversations();
  private final Store<Time, Conversation> conversationByTime = new Store<>(TIME_COMPARE);
  private final Store<String, Conversation> conversationByText = new Store<>(STRING_COMPARE);

  //private final Store<Uuid, Message> messageById = new Store<>(UUID_COMPARE);
  private final Store<Uuid, Message> messageById = ds.getAllMessages();
  private final Store<Time, Message> messageByTime = new Store<>(TIME_COMPARE);
  private final Store<String, Message> messageByText = new Store<>(STRING_COMPARE);
  

  private final Uuid.Generator userGenerations = new LinearUuidGenerator(null, 1, Integer.MAX_VALUE);
  private Uuid currentUserGeneration = userGenerations.make();
  

  public void add(User user) {
    currentUserGeneration = userGenerations.make();
    
    // EDIT - Malik Graham
    // Save the information in the user table
    try {
		ds.addUser(user);
	}
	catch (Exception ex) {
		System.out.println("Saving a user did not work.");
		ex.printStackTrace();
	}

    userById.insert(user.id, user);
    userByTime.insert(user.creation, user);
    userByText.insert(user.name, user);
  }
  
  public User checkUserLogin(String name, String password) {
	  User user = null;
	  try {
		  user = ds.userLogin(name, password);
	  }
	  catch (Exception ex) {
			ex.printStackTrace(); 
	  }
	  
	  return user;
  }
  
  public boolean checkUsername(String name) {
	  try {
		  return ds.checkUsernameExists(name);
	  }
	  catch (Exception ex) {
		 ex.printStackTrace(); 
	  }
	  
  return false;
  }
  
  public int getMessageCount(Uuid userid) {
	  try {
		  return ds.getMessageCount(userid);
	  }
	  catch (Exception ex) {
		 ex.printStackTrace(); 
	  }
	 
  return 0;
  }
  
  public int setMessageCount(Uuid userid) {
	  try {
		  return ds.setUserMessageCount(userid);
	  }
	  catch (Exception ex) {
		  
	  }
	  
  return 0;
  }

  public StoreAccessor<Uuid, User> userById() {
    return userById;
  }

  public StoreAccessor<Time, User> userByTime() {
    return userByTime;
  }

  public StoreAccessor<String, User> userByText() {
    return userByText;
  }

  public Uuid userGeneration() {
    return currentUserGeneration;
  }

  public void add(Conversation conversation) {
	// EDIT - Malik Graham
	// Save the information in the conversation table
	try {
		ds.addConversation(conversation);
	}
	catch (Exception ex) {
		System.out.println("Saving a conversation did not work.");
		ex.printStackTrace();
	}
	conversationById.insert(conversation.id, conversation);
	conversationByTime.insert(conversation.creation, conversation);
	conversationByText.insert(conversation.title, conversation);
  }

  public StoreAccessor<Uuid, Conversation> conversationById() {
    return conversationById;
  }
  
  public Iterable<Conversation> getAllConversationsStored() {
	  return ds.getAllConversations().all();
  }

  public StoreAccessor<Time, Conversation> conversationByTime() {
    return conversationByTime;
  }

  public StoreAccessor<String, Conversation> conversationByText() {
    return conversationByText;
  }

  public void add(Message message) {
	// EDIT - Malik Graham
	// Save the information in the message table
	try {
		ds.addMessage(message);
	}
	catch (Exception ex) {
		System.out.println("Saving a message did not work.");
		ex.printStackTrace();
	}
    messageById.insert(message.id, message);
    messageByTime.insert(message.creation, message);
    messageByText.insert(message.content, message);
  }

  public StoreAccessor<Uuid, Message> messageById() {
    return messageById;
  }
  
  public Message getMessageById(Uuid messageid) throws IOException, SQLException {
	  return ds.getMessage(messageid);
  }

  public StoreAccessor<Time, Message> messageByTime() {
    return messageByTime;
  }

  public StoreAccessor<String, Message> messageByText() {
    return messageByText;
  }
  
  public void updateConversation(Conversation conversation) {
	  	// EDIT - Malik Graham
	    // Update the conversations first and last message
	    try {
	    	ds.updateConversationMessages(conversation);
	    }
	    catch (Exception ex) {
	    	ex.printStackTrace();
	    }
  }
  
  public void updateMessageLink(Message message) {
	  // EDIT - Malik Graham
	  // Updating the next message for the last message
      try {
    	  ds.updateNextMessage(message);
      }
      catch (Exception ex) {
      	ex.printStackTrace();
      }
  }
}
