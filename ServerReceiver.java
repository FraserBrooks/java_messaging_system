import java.io.*;
import java.net.Socket;

// Gets messages and commands from client and puts them in a queue, for another
// thread to forward to the appropriate client.

public class ServerReceiver extends Thread {
    
    private Socket clientSocket;
	private String myClientsName;
	private BufferedReader myClient;
	private PrintStream toClient;
	private ClientTable clientTable;
	private PasswordTable passwordTable;
	private ServerSender linkedSender;
	private final String commands = 
			"Commands:\n"
			+ "     message - message an individual user \n"
			+ "     people - returns a list of users online \n"
			+ "     logout - log out of your account \n"
			+ "     quit - exit from the application \n\n";

	public ServerReceiver(String n, BufferedReader c, PrintStream p, ClientTable t, PasswordTable pt, ServerSender s, Socket so) {
	    clientSocket = so;
		myClientsName = n;
		myClient = c;
		toClient = p;
		clientTable = t;
		passwordTable = pt;
		linkedSender = s;
	}

    public void run() {
        try {
            while (true) {
                toClient.println(commands);
                String firstInput = Server.getInput(myClient.readLine());
                switch (firstInput.toLowerCase()) {
                case "message":
                    createAndSendMessage();
                    break;
                case "people":
                    toClient.println(clientTable);
                    break;
                case "logout":
                    clientTable.remove(myClientsName);
                    linkedSender.interrupt();
                    Report.behaviour("ServerReceiver: Client " + myClientsName + " has logged out. ");
                    (new ServerAuthenticator(myClient, toClient, clientTable, passwordTable, clientSocket)).start();
                    return;
                default:
                    toClient.println("Unrecognised Input. Try Again.\n");
                    break;
                }
            }
        } catch (IOException e) {
            Report.error("ServerReceiver: Lost connection with " + myClientsName + " in ServerReceiver. Message:::  "   + e.getMessage());
        } catch (ClientHasQuitException e) {
            Report.behaviour("ServerReceiver: Client" + myClientsName + " has sent the 'quit' command.");
        }

        // Attempt to close socket and streams. Do nothing on fail as socket is
        // already closed or will eventually be closed by system
        // Note: not using finally block as we don't want to close
        // streams on a successful log out
        clientTable.remove(myClientsName);
        try {
            myClient.close();
        } catch (IOException e) {
            // Nothing to do
        }
        toClient.close();
        linkedSender.interrupt();
        try {
            clientSocket.close();
        } catch (IOException e) {
            // Nothing to do
        }

        // exit thread
    }

    private void createAndSendMessage() throws ClientHasQuitException, IOException {
        toClient.println("Recipient: ");
        String recipient = Server.getInput(myClient.readLine());
        MessageQueue recipientsQueue = clientTable.getQueue(recipient);

        toClient.println("Message: ");
        String text = Server.getInput(myClient.readLine());
        Message msg = new Message(myClientsName, text);
        if (recipientsQueue != null) {
        	recipientsQueue.offer(msg);
        } else {
            Report.behaviour("Could not find requested user: " + recipient);
        	toClient.println("Error: User " + recipient + " not found.\n");
        }
    }

}
