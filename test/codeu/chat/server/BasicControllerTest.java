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

import static org.junit.Assert.*;

import codeu.chat.client.ClientUser;
import org.junit.Test;
import org.junit.Before;

import codeu.chat.common.BasicController;
import codeu.chat.common.Conversation;
import codeu.chat.common.Message;
import codeu.chat.common.User;
import codeu.chat.util.Uuid;

public final class BasicControllerTest {

  private Model model;
  private BasicController controller;

  @Before
  public void doBefore() {
    model = new Model();
    controller = new Controller(Uuid.NULL, model);
  }

  @Test
  public void testAddUser() {

    final User user = controller.newUser("user");

    assertFalse(
        "Check that user has a valid reference",
        user == null);
  }

  @Test
  public void testAddConversation() {

    final User user = controller.newUser("user");

    assertFalse(
        "Check that user has a valid reference",
        user == null);

    final Conversation conversation = controller.newConversation(
        "conversation",
        user.id);

    assertFalse(
        "Check that conversation has a valid reference",
        conversation == null);
  }

  @Test
  public void testAddMessage() {

    final User user = controller.newUser("user");

    assertFalse(
        "Check that user has a valid reference",
        user == null);

    final Conversation conversation = controller.newConversation(
        "conversation",
        user.id);

    assertFalse(
        "Check that conversation has a valid reference",
        conversation == null);

    final Message message = controller.newMessage(
        user.id,
        conversation.id,
        "Hello World");

    assertFalse(
        "Check that the message has a valid reference",
        message == null);
  }

  @Test
  public void testAllowsNormalChar() {

    final String allCaps = "GABEROONI";
    final String allLower = "gaberooni";
    final String multiCase = "GaBeRoOnI";
    final String allNumeric = "3141059";
    final String alphaNumeric = "Gaberooni555";

    assertTrue(
            "Checks that inputs in all caps are allowed",
            ClientUser.isValidInput(allCaps)
    );

    assertTrue(
            "Checks that inputs in all lower case are allowed",
            ClientUser.isValidInput(allLower)
    );

    assertTrue(
            "Checks that inputs that are all numbers are allowed",
            ClientUser.isValidInput(allNumeric)
    );

    assertTrue(
            "Checks that inputs with numbers and letters are allowed",
            ClientUser.isValidInput(alphaNumeric)
    );
  }

  @Test
  public void testPreventsBadChar() {

    final String punctuation = "!?`~[]{}@#$%^&*()<>,.;:'\\|-=_+   ";
    final String punctAndGoodChar = "!__[GABE]__!";
    final String foreignLanguage = "日本語が大好きですよ";
    final String foreignAndGoodChar = "Gaburieruガブリエル";
    final String emoji = "u/00D8";
    final String emojiAndGoodChar = "u/00D8WOOHOOu/00D8";

    assertFalse(
            "Checks that inputs with punctuation are prevented",
            ClientUser.isValidInput(punctuation)
    );

    assertFalse(
            "Checks that inputs with punctuation and normal characters are still rejected",
            ClientUser.isValidInput(punctAndGoodChar)
    );

    assertFalse(
            "Checks that inputs in another language are prevented",
            ClientUser.isValidInput(foreignLanguage)
    );

    assertFalse(
            "Checks that inputs in another language and normal characters are still rejected",
            ClientUser.isValidInput(foreignAndGoodChar)
    );

    assertFalse(
            "Checks that inputs with emojis are prevented",
            ClientUser.isValidInput(emoji)
    );

    assertFalse(
            "Checks that inputs with emojis and normal characters are still rejected",
            ClientUser.isValidInput(emojiAndGoodChar)
    );
  }
}
