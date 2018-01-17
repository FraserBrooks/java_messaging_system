package networkObjects;

public class ServerMessage extends SerializableMessage {
    
    /**
     * 
     */
    private static final long serialVersionUID = -1283077274074392282L;
    public final Message  message;
    public final Boolean failedLogIn;
    public final Boolean nameAlreadyExists;
    public final Boolean noSuchUser;
    public final Boolean wrongPassword;
    public final Boolean newFriend;
    public final Boolean newGroup;
    public Friend friend = null;
    public Group group = null;
    
    
    public ServerMessage( Message messageOb, 
            Boolean failedLog, 
            Boolean nameTaken, 
            Boolean noUserFound, 
            Boolean incorrectPass,
            Boolean isFriendAdd, 
            Boolean isGroupAdd, 
            Friend f, 
            Group g) {
        super("ServerMessage", "Server");
        message = messageOb;
        
        failedLogIn = failedLog;
        nameAlreadyExists = nameTaken;
        noSuchUser = noUserFound;
        wrongPassword = incorrectPass;
        newFriend = isFriendAdd;
        newGroup = isGroupAdd;
        friend = f;
        group = g;
        
    }
    
    public String toString(){
        String s = "Server Message: \n" +
                    message + "\n" +
                   "failedLogIn = " + failedLogIn + "\n" +
                   "nameAlreadyExists = " + nameAlreadyExists + "\n" +
                   "noSuchUser = " + noSuchUser + "\n" +
                   "wrongPassword = " + wrongPassword + "\n" +
                   "newFriend = " + newFriend + "\n" + friend + "\n" +
                   "newGroup = " + newGroup + "\n" + group + "\n";
        return s;
    }
    
    
}
