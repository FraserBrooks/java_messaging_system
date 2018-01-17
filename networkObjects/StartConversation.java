package networkObjects;

public class StartConversation extends SerializableMessage{
    
    /**
     * 
     */
    private static final long serialVersionUID = 8722075256190874295L;
    public final String groupOrClientName;
    public final Boolean groupMessage;
    
    public StartConversation(String client, String conversationName, Boolean isAGroup){
        super("StartConversation", client);
        groupOrClientName = conversationName;
        groupMessage = isAGroup;
    }

}
