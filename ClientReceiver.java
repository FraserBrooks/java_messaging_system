import java.awt.BorderLayout;	
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.*;
import java.net.*;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import networkObjects.*;

// Gets messages from other clients via the server (by the
// ServerSender thread).

public class ClientReceiver extends Thread {

    private ObjectInputStream server;
    private ClientSender sender;
    private ClientGUI cGUI;
    private boolean loggedOn;

    ClientReceiver(ObjectInputStream o, ClientSender se, ClientGUI c) {
        server = o;
        sender = se;
        cGUI = c;
        loggedOn = false;
    }

    public void run() {

        SerializableMessage m = null;
        try {
            // Currently this Thread shouldn't ever be interrupted
            while (!Thread.interrupted()) {
                m = (SerializableMessage) server.readObject();

                switch (m.type) {
                case "ClientInfoObject":
                    if(!loggedOn){
                        login((ClientInfoObject) m);
                        loggedOn = true;
                    }
                    break;
                case "ServerMessage":
                    newServerMessage((ServerMessage) m);
                    break;
                case "NetworkMessage":
                        Report.behaviour("Received new network message.");
                        newMessage((NetworkMessage) m);
                        break;
                case "Notification":
                    newNotification((Notification) m);
                    break;
                default:
                    Report.error("ClientReceiver: Unknown Class received through stream.");
                    break;
                }
            }

        } catch (SocketException e) {
            Report.error("ClientReceiver: SocketException thrown. Message: " + e.getMessage());
            System.out.println("Press enter to exit...");
        } catch (IOException e) {
            Report.errorAndGiveUp("ClientReceiver: IOException thrown.. Message: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            Report.error("ClientReceiver: ClassNotFoundException thrown. Message: " + e.getMessage());
            e.printStackTrace();
        }
      //Interrupt Sender
        sender.interrupt();
    }
    
    private void login(ClientInfoObject cio){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                cGUI.initialiseMainUi(cio);
            }
        });
    }
    
    private void newServerMessage(ServerMessage sm){
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                cGUI.newServerMessage(sm, loggedOn);
            }
        });
    }
    
    private void newMessage(NetworkMessage sm){
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                cGUI.newMessage(sm);
            }
        });
    }
    
    private void newNotification(Notification n){
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                cGUI.newNotification(n);
            }
        });
    }
}     
                