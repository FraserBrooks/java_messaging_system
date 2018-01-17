import java.io.*;

import networkObjects.SerializableMessage;


// Repeatedly reads clients input and sends to the server
// interrupted by ClientReceiver when client quits 

public class ClientSender extends Thread {

	private ObjectOutputStream server;

	ClientSender(ObjectOutputStream o) {
        server = o;
    }

    public void run() {
        try {
            // Then loop forever sending messages to recipients via the server:
            while (!Thread.interrupted()) {

                SerializableMessage message = ClientGUI.queueToServer.take();
                
                if (message != null) {
                    server.writeObject(message);
                    server.flush(); // flush the stream
                    if (message.type.toLowerCase().equals("quit")) {
                        return;
                    }
                }
            }
        } catch (IOException e) {
            Report.error("ClientSender: Communication Broke: " + e.getMessage());
        } catch (InterruptedException e) {
            Report.error("ClientSender: Interrupted: " + e.getMessage());
            e.printStackTrace();
        }

    }
}
