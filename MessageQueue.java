// We use https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/BlockingQueue.html

import java.util.concurrent.*;

public class MessageQueue {

  // We choose the LinkedBlockingQueue implementation of BlockingQueue:
  private BlockingQueue<Message> queue = new LinkedBlockingQueue<Message>();

  // Inserts the specified message into this queue.
  public void offer(Message m) {
    queue.offer(m);
  }

  // Retrieves and removes the head of this queue, waiting if
  // necessary until an element becomes available.
  public Message take() throws InterruptedException {
        return(queue.take());
  }
}
