import java.io.*;
import java.net.*;

// Gets messages from other clients via the server (by the
// ServerSender thread).

public class ClientReceiver extends Thread {

  private BufferedReader server;

  ClientReceiver(BufferedReader server) {
    this.server = server;
  }

  public void run() {
    // Print to the user whatever we get from the server:
    try {
      while (!Thread.interrupted()) {
        String s = server.readLine();
        if (s != null)
          System.out.println(s);
        else
          Report.errorAndGiveUp("Server seems to have died"); 
      }
    }
    catch(SocketException e){
    	Report.behaviour("ClientReceiver SocketException");
    }
    catch (IOException e) {
      Report.errorAndGiveUp("Server seems to have died " + e.getMessage());
    }finally{
    	try {
			server.close();
		} catch (IOException e) {
			// Nothing more to do
		}
    }
    Report.behaviour("ClientReceiver Interrupted");
  }
}
