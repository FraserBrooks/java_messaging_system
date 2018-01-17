import java.util.ArrayList;

import networkObjects.SerializableMessage;

public class ActiveConversation {
    
    private MessageQueue mq;
    private ArrayList<String> connectedClients;
    public String conversationID;
    public boolean is_group; 
    
    ActiveConversation(ArrayList<String> clients, MessageQueue messageQ, String id, boolean isGroup ){
        if(clients != null){
            connectedClients = clients;
        }else{
            connectedClients = new ArrayList<String>();
        }
        
        mq = messageQ;
        conversationID = id;
        is_group = isGroup;
    }
    
    public void connectClient(String user) {
        if (connectedClients.contains(user)) {
            return;
        } else {
            connectedClients.add(user);
        }
    }

    public ArrayList<String> getClients(){
        return connectedClients;
    }
    
    public void storeMessage(SerializableMessage toStore){
        mq.offer(toStore);
        if (mq.size() > 50){
            mq.removeOldest();
        }
    }
    
    public MessageQueue getQueue(){
        return mq;
    }
    
    public void saveAndQuit() {
        // TODO Auto-generated method stub
        
    }
    
}
