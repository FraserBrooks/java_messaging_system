import java.io.*;

// Gets messages from client and puts them in a queue, for another
// thread to forward to the appropriate client.

public class ServerReceiver extends Thread {
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

	public ServerReceiver(String n, BufferedReader c, PrintStream p, ClientTable t, PasswordTable pt, ServerSender s) {
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
				String firstInput = Server.getInput(Server.getInput(myClient.readLine()));
				switch (firstInput.toLowerCase()) {
				case "message":
					toClient.println("Recipient: ");
					String recipient = Server.getInput(myClient.readLine());
					MessageQueue recipientsQueue = clientTable.getQueue(recipient);

					toClient.println("Message: ");
					String text = Server.getInput(myClient.readLine());
					Message msg = new Message(myClientsName, text);
					if (recipientsQueue != null) {
						recipientsQueue.offer(msg);
					} else {
						toClient.println("Error: User " + recipient + " not found.\n");
					}
					break;
				case "people":
					toClient.println(clientTable);
					break;
				case "logout":
					clientTable.remove(myClientsName);
					linkedSender.interrupt();
					(new ServerAuthenticator(myClient, toClient, clientTable, passwordTable)).start();
					return;
				default:
					toClient.println("Unrecognised Input. Try Again.\n");
					break;
				}
			}
		} catch (IOException e) {
			Report.error("Something went wrong with the client " + myClientsName + " " + e.getMessage());
		} catch (ClientHasQuitException e) {
			// Client has decided to quit. Close streams and exit.
			Report.behaviour("Client" + myClientsName + " quiting server.");
			toClient.println("Application exiting......");
		}

		clientTable.remove(myClientsName);
		try {
			myClient.close();
		} catch (IOException e) {
			// Nothing to do
		}
		toClient.close();
		Report.behaviour("ServerReceiver of " + myClientsName + " is interrupting ServerSender & ending.");
		linkedSender.interrupt();
	}

}
