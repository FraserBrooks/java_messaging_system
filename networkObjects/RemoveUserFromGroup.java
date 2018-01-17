package networkObjects;

public class RemoveUserFromGroup extends SerializableMessage {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1166514437394102846L;
    public final String memberToRemove;
    public final String groupName;
    
    public RemoveUserFromGroup(String username, String clientToRemove, String groupToRemoveFrom){
        super("RemoveUserFromGroup", username);
        memberToRemove = clientToRemove;
        groupName = groupToRemoveFrom;
    }
}
