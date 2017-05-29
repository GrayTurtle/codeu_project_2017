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
//

package codeu.chat;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import codeu.chat.common.Relay;
import codeu.chat.common.Secret;
import codeu.chat.server.NoOpRelay;
import codeu.chat.server.RemoteRelay;
import codeu.chat.server.Server;
import codeu.chat.util.Logger;
import codeu.chat.util.RemoteAddress;
import codeu.chat.util.Uuid;
import codeu.chat.util.connections.ClientConnectionSource;
import codeu.chat.util.connections.Connection;
import codeu.chat.util.connections.ConnectionSource;
import codeu.chat.util.connections.ServerConnectionSource;

final class ServerMain {

  private static final Logger.Log LOG = Logger.newLog(ServerMain.class);

  private static int myPort;

  public static void main(String[] args) {

    Logger.enableConsoleOutput();

    try {
      Logger.enableFileOutput("chat_server_log.log");
    } catch (IOException ex) {
      LOG.error(ex, "Failed to set logger to write to file");
    }

    LOG.info("============================= START OF LOG =============================");

    myPort = Integer.parseInt(args[0]);
    // This is "0" because we are not using secret team id's
    final byte[] secret = Secret.parse("0");

    Uuid id = null;
    try {
      id = Uuid.parse(args[0]);
    } catch (IOException ex) {
      System.out.println("Invalid id - shutting down server");
      System.exit(1);
    }

    final RemoteAddress relayAddress = args.length > 4 ?
                                       RemoteAddress.parse(args[4]) :
                                       null;

    try (
        final ConnectionSource serverSource = ServerConnectionSource.forPort(myPort);
        final ConnectionSource relaySource = relayAddress == null ? null : new ClientConnectionSource(relayAddress.host, relayAddress.port)
    ) {

      LOG.info("Starting server...");
      runServer(id, secret, serverSource, relaySource);

    } catch (IOException ex) {

      LOG.error(ex, "Failed to establish connections");

    }
  }

  private static void runServer(Uuid id,
                                byte[] secret,
                                ConnectionSource serverSource,
                                ConnectionSource relaySource) {

    final Relay relay = relaySource == null ?
                        new NoOpRelay() :
                        new RemoteRelay(relaySource);

    final Server server = new Server(id, secret, relay);

    LOG.info("Created server.");

    final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);
    /**
     * Handles new clients on the server side and does
     * all its work for the client on a new thread.
     */
    Runnable acceptConnections = new Runnable() {

    	@Override
    	public void run() {
    		LOG.info("Established connection...");
    		    ConnectionSource serverSourceCopy = null;
    		    // The given serverSource returns an error when connected
    		    // so a new one is initiated.
    			try {
    				serverSourceCopy = ServerConnectionSource.forPort(myPort);
    			}
    			catch (IOException ex) {
    				 LOG.error(ex, "Failed to create server on port");
    			}
        		while (true) {
    			try {
    				final Connection connection = serverSourceCopy.connect();
    				LOG.info("Handling a new client!");
    				Runnable handleClients = new Runnable() {
    					@Override
    					public void run() {
    						LOG.info("About to run a new client!");
    						server.handleConnection(connection);
    					}
    				};

    				clientProcessingPool.submit(handleClients);
    			}
    			catch (IOException ex) {
    				 LOG.error(ex, "Failed to establish connection.");
    			}
    		}
    	}
    };

    Thread cycleServer = new Thread(acceptConnections);
    cycleServer.start();
  }

}
