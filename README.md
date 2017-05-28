
# CODEU CHAT SERVER | README


## DISCLAIMER

CODEU is a program created by Google to develop the skills of future software
engineers. This project is not an offical Google Product. This project is a
playground for those looking to develop their coding and software engineering
skills.


## ENVIRONMENT

All instructions here are relative to a LINUX environment. There will be some
differences if you are working on a non-LINUX system. We will not support any
other development environment.

This project was built using JAVA 8. It is recommended that you install the
standard Oracle version of [JAVA&nbsp;8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)  when working with this project.


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
       $ sh run_server.sh <team_id> <team_secret> 2007 <persistent-dir>
       $ sh run_simple_gui_client.sh
       ```

     You must specify the following startup arguments for `run_server.sh:
     + `<team_id>` and `<team_secret>`: a numeric id for your team, and a secret
       code, which are used to authenticate your server with the Relay server.
       You can specify any integer value for `<team_id>`, and a value expressed
       in hexadecimal format (using numbers `0-9` and letters in the range
       `A-F`) for `<team_secret>` when you launch the server in your local setup
       since it will not connect to the Relay server.
     + `2007`: the TCP port that your Server will listen on for connections
       from the Client. It is only on port 2007 because it was hard coded in from 
       the main repository. We are aware of this and decided to have the server only
       be run on port 2007. If another process is running on port 2007, 
       the server will return an error:

         ```
         java.net.BindException: Address already in use (Bind failed)
         ```
     + `<persistent-dir>`: the path where you want the server to save data between
       runs.

All running images write informational and exceptional events to log files.
The default setting for log messages is "INFO". The logging is implemented 
in `codeu.chat.util.Logger.java`, which is built on top of 
`java.util.logging.Logger`, which you can refer to for more information.



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

## OUR CHANGES (Team 27)

- Swapped out the Java Swing client GUI with a JavaFX version
- Updated the client-side and server-side controllers to interface with new GUI and database
- Integrated a DERBY local database to save user's database
- User's names are displayed in different colors depending on how many messages they've sent

## PLANNED CHANGES

