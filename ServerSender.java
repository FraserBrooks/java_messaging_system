import java.io.*;

// Continuously reads from message queue for a particular client,
// forwarding to the client.

public class ServerSender extends Thread {
  private MessageQueue clientQueue;
  private PrintStream client;

  public ServerSender(MessageQueue q, PrintStream c) {
    clientQueue = q;   
    client = c;
  }

  public void run() {
    while (true) {
		try {
			Message msg = clientQueue.take(); 
			client.println(msg); 
		} catch (InterruptedException e) {
			Report.behaviour("ServerSender: has been interrupted and will now close.");
		} 
    }
  }
}
