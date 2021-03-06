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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import codeu.chat.common.Conversation;
import codeu.chat.common.ConversationSummary;
import codeu.chat.common.Message;
import codeu.chat.util.Logger;
import codeu.chat.util.Method;
import codeu.chat.util.Uuid;
import codeu.chat.util.store.Store;

public final class ClientConversation {

  private final static Logger.Log LOG = Logger.newLog(ClientConversation.class);

  private final Controller controller;
  private final View view;

  private ConversationSummary currentSummary = null;
  private Conversation currentConversation = null;

  private final ClientUser userContext;
  private ClientMessage messageContext = null;
  
  private final Map<Uuid, Conversation> conversationsByUuid = new HashMap<>();

  // This is the set of conversations known to the server.
  private final Map<Uuid, ConversationSummary> summariesByUuid = new HashMap<>();

  // This is the set of conversations known to the server, sorted by title.
  private Store<String, ConversationSummary> summariesSortedByTitle =
      new Store<>(String.CASE_INSENSITIVE_ORDER);
  
  
  private Map<ConversationSummary, String> summariesSortedByCreationTime =
	 new  TreeMap<ConversationSummary, String>(new sortByCreation());
  
  /**
   * Implements Comparator that orders ConversationSummaries
   * by the last message creation time of the conversation their  
   * mapped to (newest to oldest) when inserted into the TreeMap
   * summariesSortedByCreationTime.
   * @author malikg
   *
   */
  class sortByCreation implements Comparator<ConversationSummary> {
	  @Override
	  public int compare(ConversationSummary a, ConversationSummary b) {
		  if (!Uuid.equals(a.lastMessage, Uuid.NULL) && !Uuid.equals(b.lastMessage, Uuid.NULL)) {
			  
			  Message messageA = view.getMessageById(a.lastMessage);
			  Message messageB = view.getMessageById(b.lastMessage);
			  if (messageA.creation.inMs() > messageB.creation.inMs()) return -1;
			  else if (messageA.creation.inMs() < messageB.creation.inMs()) return 1;
			  
		  return 0;
		  }
		  if (!Uuid.equals(a.lastMessage, Uuid.NULL)) return -1;
		  if (!Uuid.equals(b.lastMessage, Uuid.NULL)) return 1;
		  
	  return 0;
	  }
  }

  public ClientConversation(Controller controller, View view, ClientUser userContext) {
    this.controller = controller;
    this.view = view;
    this.userContext = userContext;
    
    System.out.println(this.getClass().toString() + " instantiate ClientConversation");
  }

  public void setMessageContext(ClientMessage messageContext) {
    this.messageContext = messageContext;
  }

  // Validate the title of the conversation
  static public boolean isValidTitle(String title) {
	  
    boolean clean = true;
    if ((title.length() <= 0) || (title.length() > 64)) {
      clean = false;
    } else {

      // TODO: check for invalid characters

    }
    System.out.println("Client Conversation " + " isValidTitle");
    return clean;
  }

  public boolean hasCurrent() {
	System.out.println(this.getClass().toString() + " hasCurrent()");
    return (currentSummary != null);
  }

  public ConversationSummary getCurrent() {
	System.out.println(this.getClass().toString() + " getCurrent()");
    return currentSummary;
  }

  public Uuid getCurrentId() { 
	  System.out.println(this.getClass().toString() + " getCurrentId()");
	  return (currentSummary != null) ? currentSummary.id : null; 
  }

  public int currentMessageCount() {
	System.out.println(this.getClass().toString() + " currentMessageCount()");
    return messageContext.currentMessageCount();
  }

  public void showCurrent() {
	System.out.println(this.getClass().toString() + " showCurrent()");
    printConversation(currentSummary, userContext);
  }

  public void startConversation(String title, Uuid owner) {
	System.out.println(this.getClass().toString() + " startConversation()");
    final boolean validInputs = isValidTitle(title);

    final Conversation conv = (validInputs) ? view.newConversation(title, owner) : null;

    if (conv == null) {
      System.out.format("Error: conversation not created - %s.\n",
          (validInputs) ? "server failure" : "bad input value");
      return;
    } else {
      LOG.info("New conversation: Title= \"%s\" UUID= %s", conv.title, conv.id);
      
      summariesByUuid.put(conv.summary.id, conv.summary);
      summariesSortedByTitle.insert(conv.summary.title, conv.summary);
      summariesSortedByCreationTime.put(conv.summary, conv.title);
      conversationsByUuid.put(conv.id, conv);

      currentSummary = conv.summary;

      updateAllConversations(currentSummary != null);
    }
  }

  public void setCurrent(ConversationSummary conv) { 
	  System.out.println(this.getClass().toString() + " setCurrent()");
	  currentSummary = conv; 
  }

  public void showAllConversations() {
	System.out.println(this.getClass().toString() + " showAllConversations()");
    updateAllConversations(false);

    for (final ConversationSummary c : summariesByUuid.values()) {
      printConversation(c, userContext);
    }
  }

  // Get a single conversation from the server.
  public Conversation getConversation(Uuid conversationId) {
	  // TODO: Could be optimized by saving conversations in memory
	    for (final Conversation c : view.getConversations(Arrays.asList(conversationId))) {
	    	return c;
	    }
	    
    return null;
  }

  private void joinConversation(String match) {
    Method.notImplemented();
  }

  private void leaveCurrentConversation() {
    Method.notImplemented();
  }

  private void updateCurrentConversation() {
	System.out.println(this.getClass().toString() + " updateCurrentConversation()");
    if (currentSummary == null) {
      currentConversation = null;
    } else {
      currentConversation = getConversation(currentSummary.id);
      if (currentConversation == null) {
        LOG.info("GetConversation: current=%s, current.id=%s, but currentConversation == null",
            currentSummary, currentSummary.id);
      } else {
        LOG.info("Get Conversation: Title=\"%s\" UUID=%s first=%s last=%s\n",
            currentConversation.title, currentConversation.id, currentConversation.firstMessage,
            currentConversation.lastMessage);
      }
    }
  }

  public int conversationsCount() {
	  System.out.println(this.getClass().toString() + " conversationsCount()");
   return summariesByUuid.size();
  }

  public Iterable<ConversationSummary> getConversationSummaries() {
	  System.out.println(this.getClass().toString() + " getConversationSummaries()");
    return summariesSortedByTitle.all();
  }
  
  public Store<String, ConversationSummary> getConversationSummariesStore() {
	  System.out.println(this.getClass().toString() + " getConversationSummaries()");
    return summariesSortedByTitle;
  }
  
  public Map<ConversationSummary, String> getSummariesByCreationTime() {
	  return summariesSortedByCreationTime;
  }

  // Update the list of known Conversations.
  // If the input currentChanged is true, then re-establish the state of
  // the current Conversation, including its messages.
  public void updateAllConversations(boolean currentChanged) {
	System.out.println(this.getClass().toString() + " updateAllConversations()");
	// TODO: Conversation summaries aren't updated with lastMessage of
	// a conversation so removing if statement (if (summariesByUuid.size() == 0)) to allow re-order of conversations,
	// but this should be optimized to lower the number of calls to the server.
	// TODO: TAKE OUT CONVERSATION AND THEN PLACE IT BACK WHEN FINISHED
	
	    summariesByUuid.clear();
	    summariesSortedByTitle = new Store<>(String.CASE_INSENSITIVE_ORDER);
	    
	    summariesSortedByCreationTime = new TreeMap<ConversationSummary, String>(new sortByCreation());
	    
	    for (final ConversationSummary cs : view.getAllConversations()) {
	      summariesByUuid.put(cs.id, cs);
	      summariesSortedByTitle.insert(cs.title, cs);
	      summariesSortedByCreationTime.put(cs, cs.title);
	    }
	
	
	
	if (currentChanged) {
	    updateCurrentConversation();
	    messageContext.resetCurrent(true);
	 }
  }
  
  public void updateConversation(Conversation convo) {
	  if (!summariesByUuid.containsKey(convo.summary.id)) {
		  summariesByUuid.put(convo.summary.id, convo.summary);
	      summariesSortedByTitle.insert(convo.summary.title, convo.summary);
	      summariesSortedByCreationTime.put(convo.summary, convo.summary.title);
	      conversationsByUuid.put(convo.id, convo);
	  }
  }

  // Print Conversation.  User context is used to map from owner UUID to name.
  public static void printConversation(ConversationSummary c, ClientUser userContext) {
    if (c == null) {
      System.out.println("Null conversation");
    } else {
      final String name = (userContext == null) ? null : userContext.getName(c.owner);
      final String ownerName = (name == null) ? "" : String.format(" (%s)", name);
      System.out.format(" Title: %s\n", c.title);
      System.out.format("    Id: %s owner: %s%s created %s\n", c.id, c.owner, ownerName, c.creation);
    }
  }

  // Print Conversation outside of User context.
  public static void printConversation(ConversationSummary c) {
    printConversation(c, null);
  }
}
