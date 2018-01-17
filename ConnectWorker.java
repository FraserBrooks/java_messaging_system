
import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class ConnectWorker implements Runnable {
    
    private ClientGUI cGUI;
    private String serverAddress;
    private Socket server = null;
    private ObjectOutputStream outStream;
    private ObjectInputStream inStream;
    

    
    public ConnectWorker(ClientGUI c, String serverName){
        serverAddress = serverName;
        cGUI = c;
    }
    
    @Override
    public void run() {
        
        try{
            
            // Just to test that gui is responsive as
            // on local system connection is made instantly
            Thread.sleep(1000);
            
            
            while(server == null){
                try {
                    server = new Socket(serverAddress, Config.PORTNUMBER);
                } catch (UnknownHostException e) {
                    Report.error("ConnectWorker: Unknown host: " + serverAddress);
                } catch (IOException e) {
                    Report.error("ConnectWorker: The server doesn't seem to be running " + e.getMessage());
                }
                Thread.sleep(1000); // Keep trying to connect every second
            }
            
            Report.behaviour("Got socket connection from server");
            
            // Create ClientSender and Receiver
            try {
                outStream = new ObjectOutputStream(server.getOutputStream());
                inStream = new ObjectInputStream(server.getInputStream());
            } catch (IOException e) {
                Report.errorAndGiveUp("ConnectWorker: The server doesn't seem to be running " + e.getMessage());
            }
            ClientSender sender = new ClientSender(outStream);
            ClientReceiver receiver = new ClientReceiver(inStream, sender, cGUI);
            
            sender.start();
            receiver.start();
            Report.behaviour("Sender and Receiver started");
            
            //Update label and buttons
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    cGUI.connectedToServer();
            }
            });

            
        }catch(InterruptedException e){
            Report.error("ConnectWorker: Interrupted before connection was made: " + e.getMessage());
        }
    }


}
