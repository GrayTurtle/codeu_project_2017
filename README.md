
## GameGab, a CodeU Chat Program by GameGab Team(Team 27) | README

## ENVIRONMENT
**YOU MUST HAVE Oracle Java 8! WITHOUT IT YOU CANNOT RUN THE PROGRAM BECAUSE IT USES JAVAFX!**

**INSTALL IT HERE:** [ORACLE JAVA &nbsp;8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

All instructions here are relative to a LINUX environment.

## GETTING STARTED

  1. To build the project:
       ```
       $ sh clean.sh
       $ sh make.sh
       ```

  1. To test the project:
       ```
       $ sh test.sh
       ```

  1. To run the project you will need to run both the client and the server. Run
     the following two commands in separate shells:

       ```
       $ sh run_server.sh <port>
       $ sh run_simple_gui_client.sh localhost <port>
       ```

     One example is:

       ```
       $ sh run_server.sh 2007
       $ sh run_simple_gui_client.sh localhost 2007
       ```

     You must specify the following startup arguments for `run_server.sh`:
     + `<port>`: the TCP port that your Server will listen on for connections
       from the Client. You can use any value between 1024 and 65535, as long as
       there is no other service currently listening on that port in your
       system. The server will return an error which will be logged in `/bin`:

         ```
         java.net.BindException: Address already in use (Bind failed)
         ```

       if the port is already in use.
       
     The startup arguments for `run_client.sh` are the following:
     + `<host>`: the first argument. It is the hostname or IP address of the computer
       on which the server is listening. This version just uses the local computer,
       so `<host>` MUST BE `localhost` which is seen at the top of step 3.
     + `<port>`: the port on which your server is listening. Must be the same
       port number you have specified when you launched `run_server.sh`.

All running images write informational and exceptional events to log files.
The default setting for log messages is "INFO". The logging is implemented
in `codeu.chat.util.Logger.java`, which is built on top of
`java.util.logging.Logger`, which you can refer to for more information.

## OUR CHANGES & NEW FEATURES (Team 27: GameGab Team)

- Swapped out the Java Swing client GUI with a JavaFX version
- Created a sign-in screen seperate from the main chat
- Requires a password that is checked to make sure it has no SQL injection and only alphanumeric characters
![alt text](https://raw.githubusercontent.com/GrayTurtle/codeu_project_2017/develop/images/SignIn.png)
- Revamped the chat to be more user friendly
- Chat updates automatically without the update button
- Chat updates for other clients
- The current user does not see themselves in the user list
- Conversations reorder themselves based on most recent message sent
- User's names are displayed in different colors depending on how many messages they've sent
- 5 messages sent turns the user's name to red, 10 to blue, and 15 to gradient
![alt text](https://raw.githubusercontent.com/GrayTurtle/codeu_project_2017/develop/images/Chat.png)
- Updated the client-side and server-side controllers to interface with new GUI and database
- Integrated a DERBY local database to save chat information
- The chat is saved, and once users sign in it is the same as they had left it


## PLANNED CHANGES

- Implement leaving a conversation (we currently just have a button for it)
- Make the client update faster by changing out the timer for something else more efficient
- Add more colors for the names to change into
- Make the client more vibrant with CSS styling
- Add more gamification features such as hangman and leaderboards

## Finding your way around the project

All the source files (except test-related source files) are in
`./src/codeu/chat`.  The test source files are in `./test/codeu/chat`. If you
use the supplied scripts to build the project, the `.class` files will be placed
in `./bin`. There is a `./third_party` directory that holds the jar files for
JUnit (a Java testing framework). Your environment may or may not already have
this installed. The supplied scripts use the version in `./third_party`.

Finally, there are some high-level design documents in the project Wiki. Please
review them as they can help you find your way around the sources.

## Source Directories

The major project components have been separated into their own packages. The
main packages/directories under `src/codeu/chat` are:

### codeu.chat.client

Classes for building the two clients (`codeu.chat.ClientMain` and
`codeu.chat.SimpleGuiClientMain`).

### codeu.chat.server

Classes for building the server (`codeu.chat.ServerMain`).

### codeu.chat.relay

Classes for building the Relay Server (`codeu.chat.RelayMain`). The Relay Server
is not currently in use.

### codeu.chat.common

Classes that are shared by the clients and servers.

### codeu.chat.util

Some basic infrastructure classes used throughout the project.
