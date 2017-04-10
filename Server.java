// Usage:	
//
//        derby.jar must be included in the classpath if it is not already:
//        java -cp "%JAVA_HOME%\db\lib\derby.jar"; Server
//
// There is no provision for ending the server gracefully.  It will
// end if (and only if) something exceptional happens.


import java.net.*;	
import java.io.*;

public class Server {

    public static void main(String[] args) {
        // This table will be shared by the server threads:
        ClientTable clientTable = new ClientTable();
        
        
        // Opens the connection to the database. 
        // Creates a new database if no database is found.
        // Database is saved in current folder.
        // This object is shared by all ServerAuthenticator and ServerReceiver Threads.
        DatabaseAccessObject dao = new DatabaseAccessObject();

        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(Config.PORTNUMBER);
        } catch (IOException e) {
            Report.errorAndGiveUp("Couldn't listen on port " + Config.PORTNUMBER);
        }

		try {
			// We loop for ever, as servers usually do.
			while (true) {
				// Listen to the socket, accepting connections from new clients:
				Socket socket = serverSocket.accept();

				// Create input and output streams
				BufferedReader fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintStream toClient = new PrintStream(socket.getOutputStream());

				Report.behaviour("Someone has connected");

				(new ServerAuthenticator(fromClient, toClient, clientTable, dao, socket)).start();

			}
		} catch (IOException e) {
			// Lazy approach:
			Report.error("IO error " + e.getMessage());
			// A more sophisticated approach could try to establish a new
			// connection. But this is beyond this simple exercise.
		}
	}

    
    public static String getInput(String input) throws ClientHasQuitException {
        
        if (input.toLowerCase().equals("quit")) {
            throw new ClientHasQuitException("Client has entered 'quit'");
        }
        return input;
    }
    


}
