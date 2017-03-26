// Each user has a different incoming-message queue.

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;

public class ClientTable {

  private ConcurrentMap<String,MessageQueue> queueTable
    = new ConcurrentHashMap<String,MessageQueue>();



  public void add(String nickname) {
    queueTable.put(nickname, new MessageQueue());
  }

  // Returns null if the nickname is not in the table:
  public MessageQueue getQueue(String nickname) {
    return queueTable.get(nickname);
  }
  
  public void remove (String name){
	  queueTable.remove(name);
  }
  
  public String toString(){
	  String toReturn = "USERS ONLINE: \n";
	  Set<String> names = queueTable.keySet();
	  Iterator<String> i = names.iterator();
	  while(i.hasNext()){
		  toReturn += i.next() + " \n";
	  }
	  return toReturn;
  }
  
}
