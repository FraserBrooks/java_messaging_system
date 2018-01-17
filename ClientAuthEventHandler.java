import java.awt.Dimension;	
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import networkObjects.AuthAttempt;

public class ClientAuthEventHandler implements ActionListener {
    
    
    // GUI Components needed to login and register
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton registerButton;
    private JPanel midPanel;
    private JPasswordField confirmPasswordField = new JPasswordField(20);
    private ClientGUI gui;
    private JLabel errorLabel;
    
    
    public ClientAuthEventHandler(ClientGUI g, JTextField username, JPasswordField password, JButton register, JPanel mid, JLabel error){
        gui = g;
        usernameField = username;
        passwordField = password;
        registerButton = register;
        midPanel = mid;
        errorLabel = error;
        confirmPasswordField.setMinimumSize(new Dimension(140,0));
        
    }
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        switch(e.getActionCommand()){
            case "registerButton":
                JLabel confirmLabel = new JLabel("ConfirmPassword: ", SwingConstants.RIGHT);
                midPanel.add(confirmLabel, 
                        ClientGUI.createGBC(
                                // x/y
                                0,2,
                                // width/height
                                ClientGUI.DF, ClientGUI.DF,
                                // fill/anchor
                                ClientGUI.DF, GridBagConstraints.LINE_END,
                                // ipadx/y
                                ClientGUI.DF, ClientGUI.DF,
                                // weightX/Y
                                ClientGUI.DF, ClientGUI.DF,
                                // inset
                                new Insets(10, 0, 10, 0 )));
                
                midPanel.add(confirmPasswordField, 
                        ClientGUI.createGBC(
                                // x/y
                                1,2,
                                // width/height
                                ClientGUI.DF , ClientGUI.DF,
                                // fill/anchor
                                GridBagConstraints.BOTH, GridBagConstraints.LINE_START,
                                // ipadx/y
                                ClientGUI.DF, ClientGUI.DF,
                                // weightX/Y
                                ClientGUI.DF, ClientGUI.DF,
                                // inset
                                new Insets(10, 10, 10, 0 )));
                
                
                registerButton.setActionCommand("register");
                passwordField.setActionCommand("register");
                usernameField.setActionCommand("register");
                confirmPasswordField.setActionCommand("register");
                confirmPasswordField.addActionListener(this);
                gui.setSize(400, 300);
                gui.setMinimumSize(new Dimension(400, 300));
                gui.setMaximumSize(new Dimension(400, 300));
                break;
                
            case "register":
                if(!Arrays.equals(passwordField.getPassword(), confirmPasswordField.getPassword())){
                    errorLabel.setText("Passwords did not match!" );
                }
                if(usernameField.getText().length() > 6 && !usernameField.getText().contains("!")){
                    ClientGUI.queueToServer.offer(new AuthAttempt(usernameField.getText(), new String(passwordField.getPassword()), true));
                    break;
                }
                errorLabel.setText("Username must be >6 characters and no !'s" );
                break;
                
            default:
                errorLabel.setText("");
                ClientGUI.queueToServer.offer(new AuthAttempt(usernameField.getText(), new String(passwordField.getPassword()), false));
        }
    }

}
