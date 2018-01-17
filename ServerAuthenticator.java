
// First destination for incoming clients

import java.io.*;	
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import networkObjects.AuthAttempt;
import networkObjects.Message;
import networkObjects.SerializableMessage;
import networkObjects.ServerMessage;

public class ServerAuthenticator extends Thread{
	
    private Socket clientSocket;
	private ObjectInputStream fromClient;
	private ObjectOutputStream toClient;
	private ServerInstance serverInstance;
	private SerializableMessage fromUser = null;

	
	ServerAuthenticator(ObjectInputStream f, ObjectOutputStream to, ServerInstance sr, Socket so) {
		fromClient = f;
		toClient = to;
		serverInstance = sr;
		clientSocket = so;
	}

    public void run() {
        String user = null;

        try {
            while (!Thread.interrupted()) {
                fromUser = (SerializableMessage) fromClient.readObject();
                switch (fromUser.type) {
                case "AuthAttempt":
                    user = attemptAuthorise(fromUser);
                    if (user != null) {
                        Report.behaviour("Server Authenticator: " + user + " has logged on.");
                        exitWith(user);
                        return; // Exit point of Thread on successful login
                    }
                    break;
                default:
                    //send error to client
                    break;
                }

            }
        } catch (IOException e) {
            Report.error("ServerAuthenticator: IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            Report.error("ServerAuthenticator: NoSuchAlgorithmException: " + e.getMessage());
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            Report.error("ServerAuthenticator: InvalidKeySpecException: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Report.error("ServerAuthenticator: ClassNotFoundException: " + e.getMessage());
            e.printStackTrace();
        }
        // Attempt to close socket and streams
        // Note: not using finally block as we don't want
        // to close streams on a successful login attempt
        try {
            toClient.close();
        } catch (IOException e1) {
            // Nothing to do
        }
        try {
            fromClient.close();
        } catch (IOException e) {
            // Nothing to do
        }

        try {
            clientSocket.close();
        } catch (IOException e) {
            // Nothing to do
        }

        // Exit point of Thread on failed or abandoned login
    }

    private String attemptAuthorise(SerializableMessage c) 
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        
        AuthAttempt client = (AuthAttempt) c;
        if (client.creatingNewAccount){
            if (serverInstance.getPasswordEntry(client.sender) != null) {
                ServerMessage sm = new ServerMessage(new Message("Unfortunately that name is taken."), true, true, false, false, false, false, null, null);
                toClient.writeObject(sm);
                return null;
            }else{
                byte[] salt = PasswordService.generateSalt();
                byte[] encryptedPassword = PasswordService.encrypt(client.password, salt);
                if (serverInstance.addNewUserToDB(client.sender, new PasswordEntry(salt, encryptedPassword))) {
                    return client.sender;
                } else {
                    ServerMessage sm = new ServerMessage(new Message("Unfortunately something went horribly wrong."), true, false, false, false, false, false, null, null);
                    toClient.writeObject(sm);
                    return null;
                }
            }
        }else{
            PasswordEntry correct = serverInstance.getPasswordEntry(client.sender);
            if (correct == null) {
                ServerMessage sm = new ServerMessage(new Message("Incorrect username or password."), true, false, true, false, false, false, null, null);
                toClient.writeObject(sm);
                return null;
            }else{
                byte[] salt = correct.getSalt();
                byte[] encryptedP = correct.getPassword();
                if (!PasswordService.authenticate(client.password, encryptedP, salt)) {
                    ServerMessage sm = new ServerMessage(new Message("Incorrect username or password."), true, false, false, true, false, false, null, null);
                    toClient.writeObject(sm);
                    return null;
                }
                // User authorised, return name of authorised user
                return client.sender;
            }
        }    
    }


    private void exitWith(String user) {
        String newUser = user;
        serverInstance.addClient(newUser);
        Report.behaviour("ServerAuthenticator starting Sender and Receiver");
         // We create and start a new thread to send to the client:
        ServerSender serverSend = new ServerSender(serverInstance.getClientQueue(newUser), toClient);
        serverSend.start();

        // We create and start a new thread to read from the client:
        (new ServerReceiver(newUser, fromClient, toClient, serverInstance, serverSend, clientSocket)).start();
        
    }

}
