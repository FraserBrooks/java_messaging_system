import java.io.*;
import java.net.*;

// Gets messages from other clients via the server (by the
// ServerSender thread).

public class ClientReceiver extends Thread {

    private BufferedReader server;
    private ClientSender sender;


    ClientReceiver(BufferedReader s, ClientSender se) {
        server = s;
        sender = se;
    }

    public void run() {
        // Print to the user whatever we get from the server:
        try {
            // Currently this Thread shouldn't ever be interrupted
            while (!Thread.interrupted()) {
                String s = server.readLine();
                if (s != null)
                    System.out.println(s);
                else { // If null then stream has been closed from server end. Exit gracefully
                    Report.behaviour("ClientReceiver: End of input stream reached (connection closed).");
                    return;
                }

            }
        } catch (SocketException e) {
            Report.error("ClientReceiver:: SocketException thrown. Message::: " + e.getMessage());
            System.out.println("Press enter to exit...");
        } catch (IOException e) {
            Report.errorAndGiveUp("ClientReceiver: IOException thrown.. Message:" + e.getMessage());
        }
        //Interrupt Sender
        sender.interrupt();
    }
}
