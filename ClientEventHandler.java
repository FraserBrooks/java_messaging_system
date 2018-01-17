import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientEventHandler implements ActionListener {
    
    private ClientGUI cGUI;
    
    public ClientEventHandler(ClientGUI cgui){
        cGUI = cgui;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
        case "cancel":
            cGUI.closeButtonWindow();
            break;
        case "sendFriendRequest":
            cGUI.sendFriendRequest();
            break;
        case "removeSelectedFriend":
            cGUI.removeSelectedFriend();
            break;
        case "newGroupRequest":
            cGUI.newGroupRequest();
            break;
        case "deleteSelectedGroup":
            cGUI.removeSelectedGroup();
            break;
        case "addUserToGroup":
            cGUI.addUserToGroup();
            break;
        case "removeUserFromGroup":
            cGUI.removeUserFromGroup();
            break;
        case "addFriendButton":
            cGUI.openButtonWindow("addFriend");
            break;
        case "removeFriendButton":
            cGUI.openButtonWindow("removeFriend");
            break;
        case "createGroupButton":
            cGUI.openButtonWindow("createGroup");
            break;
        case "deleteGroupButton":
            cGUI.openButtonWindow("deleteGroup");
            break;
        case "addUserToGroupButton":
            cGUI.openButtonWindow("addToGroup");
            break;
        case "removeUserFromGroupButton":
            cGUI.openButtonWindow("removeFromGroup");
            break;
        case "setProfilePictureButton":
            cGUI.openButtonWindow("newProfilePic");
            break;
        case "notificationClick":
            cGUI.notificationClick(e.getSource());
            break;
        case "notificationClose":
            cGUI.removeNotification(e.getSource());
            break;
        case "sendMessage":
            cGUI.sendMessage();
            break;
        }
    }

}
