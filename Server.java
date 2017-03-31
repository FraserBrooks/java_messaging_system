// Usage:
//        java Server
//
// There is no provision for ending the server gracefully.  It will
// end if (and only if) something exceptional happens.


import java.net.*;
import java.io.*;

public class Server {
	
	public static final int MIN_PASSWORD_LENGTH = 4;
	public static final int MIN_USERNAME_LENGTH = 4;

	public static String getInput(String input) throws ClientHasQuitException {
		
		if (input.toLowerCase().equals("quit")) {
			throw new ClientHasQuitException("Client has entered 'quit'");
		}
		return input;
	}

	public static void main(String[] args) {

		// This table will be shared by the server threads:
		ClientTable clientTable = new ClientTable();
		PasswordTable passwordTable = new PasswordTable();

		ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket(Port.number);
		} catch (IOException e) {
			Report.errorAndGiveUp("Couldn't listen on port " + Port.number);
		}

		try {
			// We loop for ever, as servers usually do.
			while (true) {
				// Listen to the socket, accepting connections from new clients:
				Socket socket = serverSocket.accept(); // Matches AAAAA in
														// Client.java

				// Create input and output streams
				BufferedReader fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintStream toClient = new PrintStream(socket.getOutputStream());

				Report.behaviour("Someone has connected");

				(new ServerAuthenticator(fromClient, toClient, clientTable, passwordTable, socket)).start();

			}
		} catch (IOException e) {
			// Lazy approach:
			Report.error("IO error " + e.getMessage());
			// A more sophisticated approach could try to establish a new
			// connection. But this is beyond this simple exercise.
		}
	}

}
