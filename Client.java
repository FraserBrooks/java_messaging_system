// Usage:		
//        java Client server-hostname
//
// After initialising and opening appropriate sockets, we start two
// client threads, one to send messages, and another one to get
// messages.
//
// 


import java.io.*;
import java.net.*;

class Client {

  public static void main(String[] args) {

    // Check correct usage:
    if (args.length != 1) {
      Report.errorAndGiveUp("Usage: java Client server-hostname");
    }

    // Initialise information:
    String hostname = args[0];

    // Initialise sockets and streams
    PrintStream toServer = null;
    BufferedReader fromServer = null;
    Socket server = null;

    try {
      server = new Socket(hostname, Port.number); 
      toServer = new PrintStream(server.getOutputStream());
      fromServer = new BufferedReader(new InputStreamReader(server.getInputStream()));
    } 
    catch (UnknownHostException e) {
      Report.errorAndGiveUp("Client: Unknown host: " + hostname);
    } 
    catch (IOException e) {
      Report.errorAndGiveUp("Client: The server doesn't seem to be running " + e.getMessage());
    }
    
    // Create two client threads of a different nature:
    ClientSender sender = new ClientSender(toServer);
    ClientReceiver receiver = new ClientReceiver(fromServer, sender);
    

    // Run them in parallel:
    sender.start();
    receiver.start();
    
        // Wait for them to end and close sockets.
        try {
            sender.join();
            Report.behaviour("ClientSender ended. Attempting to close streams");

            // Attempt to close the streams as they are still open if the
            // user quit. If the user was thrown out due to IO exception
            // then the streams may have already been closed by the server
            toServer.close();
            
            
            // Sleep for 2.5 seconds before closing the incoming stream to give the server time to close it gracefully
            Thread.sleep(2500);
            try {
                fromServer.close(); 
                // This will end the ClientReciever by triggering an IOException
                // If server has already closed stream then this simply does nothing
            } catch (IOException e) {
                Report.error("Could not close fromServer stream: " + e.getMessage());
            }

            // receiver input should return null with the incoming stream closed
            // so it should exit gracefully
            receiver.join();

            // finally close the socket
            try {
                server.close();
            } catch (IOException e) {
                Report.errorAndGiveUp("Could not close socket: " + e.getMessage());
            }

        } catch (InterruptedException e) {
            Report.errorAndGiveUp("Unexpected interruption " + e.getMessage());
        }
        Report.behaviour("Client ended gracefully.");
    }
}
