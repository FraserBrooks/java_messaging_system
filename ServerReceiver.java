import java.io.*;	
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import networkObjects.*;

// Gets messages and commands from client and puts them in a queue, for another
// thread to forward to the appropriate client.

public class ServerReceiver extends Thread {
    
    private Socket clientSocket;
	private String myClientsName;
	private ObjectInputStream myClient;
	private ObjectOutputStream toClient;
	private ServerInstance serverInstance;
	private ServerSender linkedSender;

	public ServerReceiver(String n, ObjectInputStream oi, ObjectOutputStream oo, ServerInstance sr, ServerSender s, Socket so) {
		myClientsName = n;
		myClient = oi;
		toClient = oo;
		serverInstance = sr;
		linkedSender = s;
		clientSocket = so;
	}

    public void run() {
        try {
            initialiseClient();
            while (true) {
                SerializableMessage m = (SerializableMessage) myClient.readObject();
                switch (m.type) {
                case "NetworkMessage":
                    Report.behaviour("NetworkMesage received from " + m.sender );
                    serverInstance.handleMessage((NetworkMessage) m);
                    break;
                case "StartConversation":
                    Report.behaviour("StartConversation request received from " + m.sender );
                    StartConversation sc = (StartConversation) m;
                    serverInstance.initialiseConversation(sc.sender, sc.groupOrClientName, sc.groupMessage);
                    break;
                case "FriendRequest":
                    Report.behaviour("FriendRequest received from " + m.sender );
                    serverInstance.handleFriendRequest((FriendRequest) m );
                    break;
                case "RemoveFriend":
                    Report.behaviour("RemoveFriend request received from " + m.sender );
                    RemoveFriend rf = (RemoveFriend) m;
                    serverInstance.removeFriend(rf);
                    break;
                case "CreateGroup":
                    Report.behaviour("CreateGroup request received from " + m.sender );
                    CreateGroup cg = (CreateGroup) m;
                    serverInstance.createGroup(cg);
                    break;
                case "RemoveGroup":
                    Report.behaviour("RemoveGroup request received from " + m.sender );
                    RemoveGroup rg = (RemoveGroup) m;
                    serverInstance.removeGroup(rg);
                    break;
                case "AddUserToGroup":
                    Report.behaviour("AddUserToGroup request received from " + m.sender );
                    AddUserToGroup addUTG = (AddUserToGroup) m;
                    serverInstance.addUserToGroup(addUTG);
                    break;
                case "RemoveUserFromGroup":
                    Report.behaviour("RemoveUserFromGroup request received from " + m.sender );
                    RemoveUserFromGroup removeUFG = (RemoveUserFromGroup) m;
                    serverInstance.removeUserFromGroup(removeUFG);
                    break;
                default:
                    Report.error("ServerReceiver read unknown type '" + m.type + "' from stream." );
                    break;
                }
            }
        } catch (IOException e) {
            Report.error("ServerReceiver: Lost connection with " + myClientsName + " in ServerReceiver. Message:  "   + e.getMessage());
        } catch (ClassNotFoundException e) {
            Report.error("ServerReceiver: Failed to load object from stream: Message:  "   + e.getMessage());
            e.printStackTrace();
        }

        // Attempt to close socket and streams. Do nothing on fail as socket is
        // already closed or will eventually be closed by system
        // Note: not using finally block as we don't want to close
        // streams on a successful log out
        serverInstance.removeClient(myClientsName);
        try {
            myClient.close();
        } catch (IOException e) {
            // Nothing to do
        }
        try {
            toClient.close();
        } catch (IOException e1) {
         // Nothing to do
        }
        linkedSender.interrupt();
        try {
            clientSocket.close();
        } catch (IOException e) {
            // Nothing to do
        }

        // exit thread
    }

    private void initialiseClient() throws IOException {
        Report.behaviour("Server is initialising client " + myClientsName);
        
        
        ArrayList<Friend> friends = serverInstance.getFriendList(myClientsName);

        ArrayList<Group> groups = serverInstance.getGroupList(myClientsName);
        groups.add(0, new Group("GLOBAL CHANNEL", false, false));
        
        ArrayList<Notification> ntfcs = serverInstance.getNotifications(myClientsName);
        
        
        ClientInfoObject cio = new ClientInfoObject(myClientsName, friends, groups, ntfcs);
        toClient.writeObject(cio);
        Report.behaviour("Server has initialised client " + myClientsName);
    }
}
