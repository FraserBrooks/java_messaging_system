package networkObjects;
import java.io.Serializable;

public abstract class SerializableMessage implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = -5746273630275251297L;
    
    public final String type;
    public final String sender;
    
    public SerializableMessage(String ty, String sen){
        type = ty;
        sender = sen;
    }

}
