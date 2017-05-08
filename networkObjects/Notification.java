package networkObjects;


public class Notification extends SerializableMessage{
    
    /**
     * 
     */
    private static final long serialVersionUID = -6369158520971075848L;
    public final Message notificationText;

    
    public Notification(Message msg, String nameOfUserOrGroup){
        super("Notification", nameOfUserOrGroup);
        notificationText = msg;
    }

}
