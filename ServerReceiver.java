import java.net.*;
import java.io.*;

// Gets messages from client and puts them in a queue, for another
// thread to forward to the appropriate client.

public class ServerReceiver extends Thread {
  private String myClientsName = null;
  private boolean logged_on = false;
  private BufferedReader myClient;
  private ClientTable clientTable;
  private PasswordTable passwords;
  private ServerSender linkedSender;

  public ServerReceiver(BufferedReader c, ClientTable t, PasswordTable p, ServerSender s) {
    myClient = c;
    clientTable = t;
    passwords = p;
    linkedSender = s;
  }

  public void run() {
	  while(true){
		  while(!logged_on){
			  //log on or register
		  }
		  while(logged_on){
			  //message
		  }
	  }
  }
	  
	  
	  
	/**
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
    Report.behaviour("ServerReceiver of " + myClientsName + " is interrupting ServerSender & ending.");
    linkedSender.interrupt();
    clientTable.remove(myClientsName);
  }
  **/
}

