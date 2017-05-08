package networkObjects;
import java.io.Serializable;

public class AuthAttempt extends SerializableMessage implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 3188799306777914949L;
    public final String password;
    public final boolean creatingNewAccount;
    
    public AuthAttempt(String sender, String pw, boolean isNewUser) {
        super("AuthAttempt", sender);
        password = pw;
        creatingNewAccount = isNewUser;
    }

}
