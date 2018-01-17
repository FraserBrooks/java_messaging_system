package networkObjects;
public class NetworkMessage extends SerializableMessage {

    /**
     * 
     */
    private static final long serialVersionUID = -3579592701518133933L;
    public final String recipient;
    public final Message message; 
    public final Boolean groupMessage;
    
    public NetworkMessage(String sender, String recip, Message messageOb, Boolean isForGroup) {
        super("NetworkMessage", sender);
        recipient = recip;
        message = messageOb;
        groupMessage = isForGroup;
    }

}
