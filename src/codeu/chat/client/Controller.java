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

import codeu.chat.common.BasicController;
import codeu.chat.common.Conversation;
import codeu.chat.common.Message;
import codeu.chat.common.NetworkCode;
import codeu.chat.common.User;
import codeu.chat.util.Logger;
import codeu.chat.util.Serializers;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;
import codeu.chat.util.connections.Connection;
import codeu.chat.util.connections.ConnectionSource;

public class Controller implements BasicController {

  private final static Logger.Log LOG = Logger.newLog(Controller.class);

  private final Connection source;

  public Controller(Connection source) {
    this.source = source;
  }

  @Override
  public Message newMessage(Uuid author, Uuid conversation, String body) {
    
    Message response = null;

    try {

      Serializers.INTEGER.write(source.out(), NetworkCode.NEW_MESSAGE_REQUEST);
      Uuid.SERIALIZER.write(source.out(), author);
      Uuid.SERIALIZER.write(source.out(), conversation);
      Serializers.STRING.write(source.out(), body);

      if (Serializers.INTEGER.read(source.in()) == NetworkCode.NEW_MESSAGE_RESPONSE) {
        response = Serializers.nullable(Message.SERIALIZER).read(source.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public User newUser(String name, String password) {

    User response = new User(Uuid.NULL, "", "", Time.now());

    try {

      Serializers.INTEGER.write(source.out(), NetworkCode.NEW_USER_REQUEST);
      Serializers.STRING.write(source.out(), name);
      
      // ADDED BY MALIK
      Serializers.STRING.write(source.out(), password);
      
      LOG.info("newUser: Request completed.");

      /*if (Serializers.INTEGER.read(source.in()) == NetworkCode.NEW_USER_RESPONSE) {
        response = Serializers.nullable(User.SERIALIZER).read(source.in());
        LOG.info("newUser: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }*/
  
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public Conversation newConversation(String title, Uuid owner)  {

    Conversation response = null;

    try {

      Serializers.INTEGER.write(source.out(), NetworkCode.NEW_CONVERSATION_REQUEST);
      Serializers.STRING.write(source.out(), title);
      Uuid.SERIALIZER.write(source.out(), owner);

      if (Serializers.INTEGER.read(source.in()) == NetworkCode.NEW_CONVERSATION_RESPONSE) {
        response = Serializers.nullable(Conversation.SERIALIZER).read(source.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }
  
  public int updateMessageCount(Uuid userid) {
	  int messageCount = 0;
	  
	  try { 
		  Serializers.INTEGER.write(source.out(), NetworkCode.UPDATE_MESSAGE_COUNT_REQUEST);
		  Uuid.SERIALIZER.write(source.out(), userid);
		  
		  if (Serializers.INTEGER.read(source.in()) == NetworkCode.UPDATE_MESSAGE_COUNT_RESPONSE) {
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
  
}
