import java.net.*;
import java.io.*;

// Gets messages from client and puts them in a queue, for another
// thread to forward to the appropriate client.

public class ServerReceiver extends Thread {
  private String myClientsName;
  private BufferedReader myClient;
  private ClientTable clientTable;
  private ServerSender linkedSender;

  public ServerReceiver(String n, BufferedReader c, ClientTable t, ServerSender s) {
    myClientsName = n;
    myClient = c;
    clientTable = t;
    linkedSender = s;
  }

  public void run() {
    try {
      while (true) {
        String recipient = myClient.readLine(); // Matches CCCCC in ClientSender.java
        if(recipient.equals("quit")){
        	break;
        }
        String text = myClient.readLine();      // Matches DDDDD in ClientSender.java
        Message msg = new Message(myClientsName, text);
        MessageQueue recipientsQueue = clientTable.getQueue(recipient); // Matches EEEE in ServerSender.java
        if (recipientsQueue != null)
          recipientsQueue.offer(msg);
        else
          Report.error("Message for unexistent client "
                         + recipient + ": " + text);
        
      }
    }
    catch (IOException e) {
      Report.error("Something went wrong with the client " 
                   + myClientsName + " " + e.getMessage()); 
      // No point in trying to close sockets. Just give up.
      // We end this thread (we don't do System.exit(1)).
    }
    Report.behaviour("ServerReceiver of " + myClient + " is interrupting ServerSender & ending.");
    linkedSender.interrupt();
    clientTable.remove(myClientsName);
  }
}

