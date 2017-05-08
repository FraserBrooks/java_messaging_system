package networkObjects;
import java.io.Serializable;
import java.util.GregorianCalendar;


public class Message implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = -2585723548794719090L;
    
    public final String content;
    public final GregorianCalendar time;
    
    public Message(String message) {
            content = message;
            time = new GregorianCalendar();
        }
    
    public String toString(){
        return content;
    }

}