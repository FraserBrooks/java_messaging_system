
// We use https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/BlockingQueue.html

import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.*;

import networkObjects.SerializableMessage;

public class MessageQueue implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = 205759165204787889L;
    // We choose the LinkedBlockingQueue implementation of BlockingQueue:
    private BlockingQueue<SerializableMessage> queue = new LinkedBlockingQueue<SerializableMessage>();

    // Inserts the specified message into this queue.
    public void offer(SerializableMessage m) {
        queue.offer(m);
    }

    // Retrieves and removes the head of this queue, waiting if
    // necessary until an element becomes available.
    public SerializableMessage take() throws InterruptedException {
        return (queue.take());
    }
    
    public void copyTo(MessageQueue clientQueue){
        Iterator<SerializableMessage> i = queue.iterator();
        while(i.hasNext()){
            clientQueue.offer(i.next());
        }
    }
    
    public int size(){
        return queue.size();
    }
    
    public void removeOldest(){
        queue.poll();
    }
    public String toString(){
        return queue.peek().type + " " + queue.peek().sender;
    }
}
