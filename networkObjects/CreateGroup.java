package networkObjects;

public class CreateGroup extends SerializableMessage {
    
    /**
     * 
     */
    private static final long serialVersionUID = 3717514069690199117L;
    public final String groupName;
    
    public CreateGroup(String username, String gName){
        super("CreateGroup", username);
        groupName = gName;
    }
    
}
