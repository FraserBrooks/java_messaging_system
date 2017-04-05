import java.io.*;


// Repeatedly reads clients input and sends to the server
// interrupted by ClientReceiver when client quits 

public class ClientSender extends Thread {

	private PrintStream server;

	ClientSender(PrintStream p) {
        server = p;
    }

    public void run() {
        BufferedReader user = new BufferedReader(new InputStreamReader(System.in));
        try {
            // Then loop forever sending messages to recipients via the server:
            while (!Thread.interrupted()) {

                String message = user.readLine();
                
                if (message != null && message.length() > 0) {
                    server.println(message);
                    if (message.toLowerCase().equals("quit")) {
                        server.flush(); // flush the stream just to be sure
                        return;
                    }
                }
            }
        } catch (IOException e) {
            Report.error("Client sender: Communication Broke: " + e.getMessage());
        }

    }
}
