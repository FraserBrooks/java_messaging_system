import java.util.ArrayList;
import java.util.concurrent.ConcurrentMap;

public class ConversationManager extends Thread {
    
    private final int waitTime = 20000;
    
    private ClientTable clientTable;
    private ConcurrentMap<String, ActiveConversation> acMap;
    private DatabaseAccessObject dao;
    
    ConversationManager(ConcurrentMap<String, ActiveConversation> conversations, 
                            ClientTable ct, 
                                DatabaseAccessObject database){
        acMap = conversations;
        clientTable = ct;
        dao = database;
    }
    
    public void run() {

        try {
            while (true) {
                Thread.sleep(waitTime);
                Report.behaviour("ConversationManager checking for dead conversations to log");
                for (String key : acMap.keySet()) {
                    ActiveConversation ac = acMap.get(key);
                    if (ac.conversationID.equals("GLOBAL!CHANNEL")) {
                        continue;
                    }
                    ArrayList<String> clients = ac.getClients();
                    boolean allClientsOffline = true;
                    for (String client : clients) {
                        if (clientTable.getQueue(client) != null) {
                            allClientsOffline = false;
                        }
                    }

                    if (allClientsOffline) {
                        Report.behaviour("ConversationManager found dead conversation: " + ac.conversationID);
                        if (ac.is_group) {
                            dao.storeGroupConversation(ac.getQueue(), ac.conversationID);
                            acMap.remove(key);
                            Report.behaviour(
                                    "ConversationManager stored dead conversation messageQueue and removed from Conversation Map ");
                            continue;
                        } else {
                            dao.storePrivateConversation(ac.getQueue(), ac.conversationID);
                            acMap.remove(key);
                            Report.behaviour(
                                    "ConversationManager stored dead conversation messageQueue and removed from Conversation Map ");
                            continue;
                        }
                    }
                }
                Report.behaviour("ConversationManager found no dead conversations. Will check again in " + waitTime
                        + " milliseconds.");
            }
        } catch (InterruptedException e) {

        }
    }
}
