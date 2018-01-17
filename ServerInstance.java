import java.util.ArrayList;	
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import networkObjects.*;

public class ServerInstance {
    
    private ClientTable clientTable;
    private DatabaseAccessObject dao;
    private ConcurrentMap<String, ActiveConversation> activeConversations = new ConcurrentHashMap<String, ActiveConversation>();
    private ArrayList<String> pendingFriendRequests;
    
    ServerInstance(ClientTable ct, DatabaseAccessObject da){
        clientTable = ct;
        dao = da;
        pendingFriendRequests = new ArrayList<String>();
        
        // Start global communication channel for all users
        activeConversations.put(
                "GLOBAL CHANNEL", new ActiveConversation(
                                           null, new MessageQueue(), "GLOBAL!CHANNEL", true) );
        
        (new ConversationManager(activeConversations, clientTable, dao)).start();
    }
    
    public void initialiseConversation(String clientName, String groupOrFriendName, Boolean isGroup){
        
        ActiveConversation ac;
        
        if(isGroup){
            ac = activeConversations.get(groupOrFriendName);
            if (ac != null){
                ac.connectClient(clientName);
                MessageQueue clientQ = clientTable.getQueue(clientName);
                MessageQueue conversationQueue = ac.getQueue();
                conversationQueue.copyTo(clientQ);
            }else{
                MessageQueue queueFromDatabase = dao.getGroupConversation(groupOrFriendName);
                if(queueFromDatabase == null){
                    Report.error(" Server could not load conversation from database for group " + groupOrFriendName);
                    queueFromDatabase = new MessageQueue();
                }
                ArrayList<String> clients = new ArrayList<String>();
                clients.add(clientName);
                MessageQueue clientQ = clientTable.getQueue(clientName);
                queueFromDatabase.copyTo(clientQ);
                ac = new ActiveConversation(clients, queueFromDatabase, groupOrFriendName, true);
                Report.behaviour("New conversation initialised: " + clientName + " connected to --> " + groupOrFriendName );
                if(activeConversations.putIfAbsent(groupOrFriendName, ac) == null){
                    return;
                }else{
                    Report.error("New conversation could not be added to HashMap as a conversation with that key already exists");
                }
            }
        }else{
            
            String conversationID = Config.getCombinedID(clientName, groupOrFriendName);

            ac = activeConversations.get(conversationID);
            if (ac != null){
                ac.connectClient(clientName);
                MessageQueue clientQ = clientTable.getQueue(clientName);
                MessageQueue conversationQueue = ac.getQueue();
                conversationQueue.copyTo(clientQ);
            }else{
                MessageQueue queueFromDatabase = dao.getPrivateConversation(clientName, groupOrFriendName );
                if(queueFromDatabase == null){
                    Report.error(" Server could not load conversation from database for client " + groupOrFriendName + " at the request of " + clientName);
                    queueFromDatabase = new MessageQueue();
                }
                ArrayList<String> clients = new ArrayList<String>();
                clients.add(clientName);
                MessageQueue clientQ = clientTable.getQueue(clientName);
                queueFromDatabase.copyTo(clientQ);
                ac = new ActiveConversation(clients, queueFromDatabase, conversationID, false);
                if(activeConversations.putIfAbsent(conversationID, ac) == null){
                    Report.behaviour("Server: new conversation successfuly created with ID " + conversationID);
                    return;
                }else{
                    Report.error("Server: New conversation could not be added to HashMap as a conversation with that key already exists");
                }
            }
        }
    }
    
    public void handleMessage(NetworkMessage message){
        
        
        if(message.groupMessage){
            ActiveConversation ac = activeConversations.get(message.recipient);
            if (ac == null){
                Report.error("Server encountered message for conversation that hasn't been initialised." );
            }
            ac.storeMessage(message);
            
            ArrayList<String> clientsInGroup = ac.getClients();
            MessageQueue mq;
            for(String client: clientsInGroup){
                mq = clientTable.getQueue(client);
                if(mq != null){
                    mq.offer(message);
                }
            }

        }else{

            String conversationID = Config.getCombinedID(message.recipient, message.sender);
            
            ActiveConversation ac = activeConversations.get(conversationID);
            if (ac == null){
                Report.error("Server encountered message for conversation that hasn't been initialised. ID = " + conversationID );
                return;
            }
            ac.storeMessage(message);
            
            ArrayList<String> clientsInGroup = ac.getClients();
            MessageQueue mq;
            for(String client: clientsInGroup){
                mq = clientTable.getQueue(client);
                if(mq != null){
                    mq.offer(message);
                }
            }
        }
    }
    
    
    public boolean handleFriendRequest(FriendRequest fr){
        MessageQueue senderQueue = clientTable.getQueue(fr.sender);
        PasswordEntry p = getPasswordEntry(fr.recipient);
        
        if(p == null){
            senderQueue.offer(new ServerMessage(new Message("No Such User"), false, false, true, false, false, false, null, null));
            return false;
        }
        
        String friendShipID = Config.getCombinedID(fr.recipient, fr.sender);

        
        if(pendingFriendRequests.contains(friendShipID)){
            
            pendingFriendRequests.remove(friendShipID);
            if(!dao.addNewFriendship(fr.sender, fr.recipient)){
                Report.error("Failed to add new friendship");
                senderQueue.offer(new ServerMessage(new Message("Something went wrong"), false, false, true, false, false, false, null, null));
                return false;
            }
            senderQueue.offer(
                    new ServerMessage(
                            new Message("New Friend"), false, false, false, false, true, false, 
                            new Friend(fr.recipient, null), null));
            MessageQueue recipientQueue = clientTable.getQueue(fr.recipient);
            Notification newFriendNotification = new Notification(new Message(fr.sender + " accepted your friend request!"), fr.sender);
            if(recipientQueue != null){
                recipientQueue.offer(new ServerMessage(
                            new Message("New Friend"), false, false, false, false, true, false, 
                            new Friend(fr.sender, null), null));
                recipientQueue.offer(newFriendNotification);
            }else{
                dao.storeNotification(newFriendNotification, fr.recipient);
            }
            
        }else{
            pendingFriendRequests.add(friendShipID);
            Notification newFriendRequestNotification = new Notification(new Message(fr.sender + " sent you a friend request! Click to accept."), fr.sender);
            MessageQueue recipientQueue = clientTable.getQueue(fr.recipient);
            if(recipientQueue != null){
                recipientQueue.offer(newFriendRequestNotification);
            }else{
                Report.behaviour("Storing friend request notification for " + fr.recipient );
                dao.storeNotification(newFriendRequestNotification, fr.recipient);
            }
        }
        return true;
    }
    
    public boolean removeFriend(RemoveFriend rf){
        
        String friendShipID = Config.getCombinedID(rf.toRemove, rf.sender);

        MessageQueue deletedFriend = clientTable.getQueue(rf.toRemove);
        if(deletedFriend != null){
            deletedFriend.offer(rf);
        }
        
        return dao.removeFriendship(friendShipID);
    }
    
    public boolean createGroup(CreateGroup cg){
        
        MessageQueue sender = clientTable.getQueue(cg.sender);
        if(dao.addNewGroup(cg.groupName, cg.sender)){
            if(sender != null){
                sender.offer(new ServerMessage(
                        new Message("Group Successfully Created"), false, false, false, false, false, true, null, 
                        new Group(cg.groupName, true, true)));
            }
            return true;
        }else{
            if(sender != null){
                sender.offer(new ServerMessage(
                        new Message("That group name is taken. Please try again."), false, true, false, false, false, false, null, 
                        null));
            }
            return false;
        }

    }
    
    public void removeClient(String myClientsName) {
        clientTable.remove(myClientsName);
    }

    public ArrayList<Friend> getFriendList(String clientsName) {
        return dao.getFriendList(clientsName);
    }

    public ArrayList<Group> getGroupList(String clientsName) {	
        return dao.getGroupList(clientsName);
    }
    
    public ArrayList<Notification> getNotifications(String clientsName){
        return dao.getNotifications(clientsName);
    }
    
    public PasswordEntry getPasswordEntry(String clientsName) {
        return dao.getPasswordEntry(clientsName);
    }

    public boolean addNewUserToDB(String clientsName, PasswordEntry passwordEntry) {
        return dao.addNewUser(clientsName, passwordEntry);
    }

    public void addClient(String newUser) {
        clientTable.add(newUser);
        
    }
    
    public MessageQueue getClientQueue(String username){
        return clientTable.getQueue(username);
    }

    public boolean removeGroup(RemoveGroup rg) {
        if(rg.deleteGroup){
            //sender is owner: remove group and every membership for it in database
            return dao.removeGroup(rg.groupName);
        }else{
            // sender is just a member: remove their membership only
            return dao.removeMembership(rg.groupName, rg.sender);
        }
        
    }

    public boolean addUserToGroup(AddUserToGroup addUTG) {
        
       if(dao.addNewMembership(addUTG.group, addUTG.newMember, addUTG.isAdmin, false)){
           MessageQueue newUser = clientTable.getQueue(addUTG.newMember);
           Notification newGroupNotification = new Notification(new Message(addUTG.sender + " added you to a new group: " + addUTG.group ), addUTG.group);
           if(newUser != null){
               newUser.offer((new ServerMessage(
                       new Message("New Group"), false, false, false, false, false, true, 
                      null, new Group(addUTG.group, false, addUTG.isAdmin))));
               newUser.offer(newGroupNotification);
           }else{
               dao.storeNotification(newGroupNotification, addUTG.newMember);
           }
           
           return true;
       }else{
           MessageQueue sender = clientTable.getQueue(addUTG.sender);
           sender.offer((new ServerMessage(
                            new Message("Failed to add new user. User may already exist in group."), false, false, true, false, false, false, 
                           null, null)));
           return false;
       }
    }

    public boolean removeUserFromGroup(RemoveUserFromGroup removeUFG) {
        return dao.removeMembership(removeUFG.groupName, removeUFG.memberToRemove);
        
    }



   
}
