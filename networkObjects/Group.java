package networkObjects;
import java.io.Serializable;

public class Group implements Serializable{
    
    /**
     * 
     */
    private static final long serialVersionUID = 5857208705336095195L;
    public final String groupName;
    public final boolean clientIsOwner;
    public final boolean clientIsAdmin;
    
    public Group(String name, boolean isOwner, boolean isAdmin){
        groupName = name;
        clientIsOwner = isOwner;
        clientIsAdmin = isAdmin;
    }
    public String toString(){
        return "Group = " + groupName + 
                "\nOwner = " + clientIsOwner +
                "\nAdmin = " + clientIsAdmin;
    }

}
