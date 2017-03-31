import java.io.*;
import java.net.*;

// Gets messages from other clients via the server (by the
// ServerSender thread).

public class ClientReceiver extends Thread {

    private BufferedReader server;
    private BufferedReader userInputStream;
    // Need the reference to the user input stream so that we can close it if
    // server dies as the client sender doesn't throw an IOException

    ClientReceiver(BufferedReader s, BufferedReader i) {
        server = s;
        userInputStream = i;
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
        } catch (IOException e) {
            Report.errorAndGiveUp("ClientReceiver: IOException thrown.. Message:" + e.getMessage());
        }
        try {
            userInputStream.close();
        } catch (IOException e) {
            // Nothing to do
        }
    }
}
