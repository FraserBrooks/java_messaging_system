import java.io.*;

import networkObjects.SerializableMessage;

// Continuously reads from message queue for a particular client,
// forwarding to the client.

public class ServerSender extends Thread {
  private MessageQueue clientQueue;
  private ObjectOutputStream toClient;

  public ServerSender(MessageQueue q, ObjectOutputStream o) {
    clientQueue = q;   
    toClient = o;
  }

  public void run() {
    while (true) {
		try {
			SerializableMessage msg = clientQueue.take(); 
			toClient.writeObject(msg);
		} catch (InterruptedException e) {
			Report.behaviour("ServerSender: has been interrupted and will now close.");
		} catch (IOException e) {
            Report.error("ServerSender: IOException : " + e.getMessage());
            e.printStackTrace();
        } 
    }
  }
}
