import java.io.*;


// Repeatedly reads recipient's nickname and text from the user in two
// separate lines, sending them to the server (read by ServerReceiver
// thread).

public class ClientSender extends Thread {

  private String nickname;
  private PrintStream server;

  ClientSender(String nickname, PrintStream server) {
    this.nickname = nickname;
    this.server = server;
  }

  public void run() {
    // So that we can use the method readLine:
    BufferedReader user = new BufferedReader(new InputStreamReader(System.in));

    try {
      // Then loop forever sending messages to recipients via the server:
      while (true) {
        String firstMessage  = user.readLine();
        if(firstMessage.equals("quit")){
        	server.println("quit"); // Matches CCCCC in ServerReceiver.java
        	Report.behaviour(nickname + " quit");
        	break;
        }
        String secondMessage = user.readLine();
        server.println(firstMessage); // Matches CCCCC in ServerReceiver.java
        server.println(secondMessage);      // Matches DDDDD in ServerReceiver.java

      }
    }
    catch (IOException e) {
      Report.errorAndGiveUp("Communication broke in ClientSender" 
                        + e.getMessage());
    }
  }
}
