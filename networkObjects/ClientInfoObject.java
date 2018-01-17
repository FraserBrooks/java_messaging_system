package networkObjects;

import java.util.ArrayList;

public class ClientInfoObject extends SerializableMessage {

    /**
     * 
     */
    private static final long serialVersionUID = 4019641410030664882L;
    
    public final String clientUsername;
    public final ArrayList<Friend> friendList;
    public final ArrayList<Group> groupList;
    public final ArrayList<Notification> notificationList;

    public ClientInfoObject(String name, ArrayList<Friend> friends, ArrayList<Group> groups, ArrayList<Notification> notifications) {
            super("ClientInfoObject", "server");
            clientUsername = name;
            friendList = friends;
            groupList = groups;
            notificationList = notifications;
        }
    public String toString(){
        String value = "";
        for(int i = 0; i < friendList.size(); i++){
            value += "\nFriend: " + friendList.get(i).username;
        }
        for(int i = 0; i < groupList.size(); i++){
            value += "\nGroup: " + groupList.get(i).groupName;
        }
        return value + "\n\n";
    }

}