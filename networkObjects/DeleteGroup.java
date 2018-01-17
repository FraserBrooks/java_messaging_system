package networkObjects;

public class DeleteGroup extends SerializableMessage {
    
    /**
     * 
     */
    private static final long serialVersionUID = -6304424258028673459L;
    
    public final String groupName;
    
    public DeleteGroup(String username, String gName){
        super("DeleteGroup", username);
        groupName = gName;
    }
    
}