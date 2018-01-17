package networkObjects;

public class AddUserToGroup extends SerializableMessage {
    
    /**
     * 
     */
    private static final long serialVersionUID = -7338031348934472222L;
    public final String group;
    public final String newMember;
    public final Boolean isAdmin;
    
    public AddUserToGroup(String username, String clientToAdd, String groupToAddInto, Boolean admin){
        super("AddUserToGroup", username);
        newMember = clientToAdd;
        group = groupToAddInto;
        isAdmin = admin;
    }
    
}
