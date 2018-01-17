package networkObjects;

public class FriendRequest extends SerializableMessage {
    
    /**
     * 
     */
    private static final long serialVersionUID = -753751508940788239L;

    public final String recipient;
    
    public FriendRequest(String username, String friend){
        super("FriendRequest", username);
        recipient = friend;
    }
    
}