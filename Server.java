// Usage:		
//
//        derby.jar must be included in the classpath if it is not already
//        java db is included in the newer versions of the JDK:
//        java -cp "%JAVA_HOME%\db\lib\derby.jar"; Server
//
//        If running an older JDK, the neccessary derby.jar file
//        has been included in the 'lib' folder in the 'src' directory:
//        java -cp "%PROJECT_ROOT%\src\lib\derby.jar"; Server
//
//        There is no provision for ending the server gracefully.  It will
//        end if (and only if) something exceptional happens.

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

        ServerInstance serverInstance = new ServerInstance(clientTable, dao);
        
        

        ServerSocket serverSocket = null;
        
        try {
            serverSocket = new ServerSocket(Config.PORTNUMBER);
        } catch (IOException e) {
            Report.errorAndGiveUp("Couldn't listen on port " + Config.PORTNUMBER);
        }
        
        while (true) {
            try {
                // We loop for ever, as servers usually do.
                while (true) {
                    // Listen to the socket, accepting connections from new
                    // clients:
                    Socket socket = serverSocket.accept();

                    // Create input and output streams
                    ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());

                    Report.behaviour("Someone has connected");

                    (new ServerAuthenticator(fromClient, toClient, serverInstance, socket)).start();

                }
            } catch (IOException e) {
                Report.error("IO error " + e.getMessage());

            } finally {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    // Nothing to do
                }
            }
        }
        
		
	}

    


}
