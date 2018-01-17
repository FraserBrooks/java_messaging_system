import java.awt.BorderLayout;	  
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import networkObjects.Message;

public class MessageWindow extends JPanel{

    /**
     * 
     */
    private static final long serialVersionUID = -3809600031022357787L;
    private int messageCount = 0;
    
    private static ArrayList<JTextArea> textAreas = new ArrayList<JTextArea>(); 
    
    public MessageWindow(){
        setLayout(new GridBagLayout());
        setBackground(ClientGUI.greyedOut);
    }
    
    public void addMessage(Message m, boolean clientMessage, String from){
        
        JPanel newMessage = new JPanel(new GridBagLayout());
        
        JPanel messageContainer = new JPanel(new BorderLayout());
        JTextArea messageBody = new JTextArea(m.content);
        JScrollPane scrollPane = new JScrollPane(messageBody);
        JLabel messageDate = new JLabel(m.time.getTime().toString());
        JLabel messageFrom = new JLabel(from + ": ");
        
        messageBody.setFont(ClientGUI.merriweather);
        messageDate.setFont(ClientGUI.merriweatherSmall);
        messageFrom.setFont(ClientGUI.merriweatherSmall);
        
        // TODO using defaultIcon for now
        ImageIcon defaultIcon = ClientGUI.defaultIcon;
        JLabel icon = new JLabel(defaultIcon);
        
        messageBody.setEditable(false);
        messageBody.setColumns(20);
        //messageBody.setBorder(null);
        messageBody.setLineWrap(true);
        messageBody.setWrapStyleWord(true);
        scrollPane.setBorder(null);

        
        scrollPane.getViewport().setBackground(ClientGUI.appOrange);

        messageContainer.add(scrollPane, BorderLayout.CENTER);

        
        newMessage.add(messageContainer, ClientGUI.createGBC(1, 0, 2, ClientGUI.DF, GridBagConstraints.BOTH, GridBagConstraints.LINE_START, 10, 10, 0.8, 0.7, new Insets(0, 20, 0, 20)));
        newMessage.add(messageFrom, ClientGUI.createGBC(1, 1, ClientGUI.DF, ClientGUI.DF, GridBagConstraints.HORIZONTAL, GridBagConstraints.LINE_END, 2, 2, 0.4, 0.3, new Insets(0, 20, 0, 0)));
        newMessage.add(messageDate, ClientGUI.createGBC(2, 1, ClientGUI.DF, ClientGUI.DF, GridBagConstraints.HORIZONTAL, GridBagConstraints.LINE_START, 2, 2, 0.4, 0.3, null));
        
        if(clientMessage){

            newMessage.add(icon, ClientGUI.createGBC(3, 0, ClientGUI.DF, 2, GridBagConstraints.BOTH, GridBagConstraints.LINE_START, 0, 5, 0.2, 0.5, null));
            messageBody.setBackground(ClientGUI.appBlue);
            newMessage.setBackground(ClientGUI.appBlue);
            messageBody.setForeground(Color.WHITE);
            add(newMessage, ClientGUI.createGBC(0, messageCount, ClientGUI.DF, ClientGUI.DF, GridBagConstraints.HORIZONTAL, ClientGUI.DF, 5, 5, 0.2, 0.5, new Insets(20,100,20,10)));
            
        }else{
            
            newMessage.add(icon, ClientGUI.createGBC(0, 0, ClientGUI.DF, 2, GridBagConstraints.BOTH, GridBagConstraints.LINE_START, 0, 5, 0.5, 0.5, null));
            messageBody.setBackground(ClientGUI.appOrange);
            newMessage.setBackground(ClientGUI.appOrange);
            messageBody.setForeground(Color.WHITE);
            add(newMessage, ClientGUI.createGBC(0, messageCount, ClientGUI.DF, ClientGUI.DF, GridBagConstraints.HORIZONTAL, ClientGUI.DF, 5, 5, 0.5, 0.5, new Insets(20,10,20,100)));
        }
        textAreas.add(messageBody);
        messageCount++;
    }

    public void setMaxMessageSize(int width) {
        int limit = getColumnNumber(width);
        for(JTextArea t :textAreas){
            t.setColumns(limit);
        }
    }
    
    private int getColumnNumber(int width){
        while (true) {
            if (width > 1200) {
                return 40;
            }
            if (width > 800) {
                return 30;
            }
            if (width > 700) {
                return 24;
            }
            if (width > 600) {
                return 18;
            }
            return 16;
        }
    }
}

