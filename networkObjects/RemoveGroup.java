package networkObjects;

public class RemoveGroup extends SerializableMessage {
    
    /**
     * 
     */
    private static final long serialVersionUID = 14467317265869686L;
    public final String groupName;
    public final boolean deleteGroup;
    
    public RemoveGroup(String  client, String nameOfGroup, boolean delete){
        super("RemoveGroup", client);
        groupName = nameOfGroup;
        deleteGroup = delete;
    }
}
