import java.io.*;


// Repeatedly reads clients input and sends to the server
// interrupted by ClientReceiver when client quits 

public class ClientSender extends Thread {

	private PrintStream server;
	private BufferedReader user;

	ClientSender(PrintStream p, BufferedReader i) {
        server = p;
        user = i;
    }

    public void run() {

        try {
            // Then loop forever sending messages to recipients via the server:
            while (true) {
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
            Report.errorAndGiveUp("Communication broke in ClientSender" + e.getMessage());
        }

    }
}
