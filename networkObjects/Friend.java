package networkObjects;
import java.io.Serializable;

import javax.swing.ImageIcon;

public class Friend implements Serializable{
    
    /**
     * 
     */
    private static final long serialVersionUID = -1810270470617190090L;
    public final String username;
    public final ImageIcon icon;
    
    public Friend(String clientName, ImageIcon img ){
        username = clientName;
        icon = img;
    }
    
    public String toString(){
        return "Friend = " + username;
    }

}
