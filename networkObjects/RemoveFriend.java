package networkObjects;
public class RemoveFriend extends SerializableMessage {

    /**
     * 
     */
    private static final long serialVersionUID = 9213256312510074023L;
    
    public String toRemove;
    
    public RemoveFriend(String username, String friend) {
        super("RemoveFriend", username);
        toRemove = friend;
    }

}
