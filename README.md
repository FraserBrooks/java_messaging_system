# Simple messaging system

  * Based on the client-server architecture with server threads and
    socket communication.

# Solution

  * [communication-and-concurrency/6-blocking-queue/messaging/]
    (https://git.cs.bham.ac.uk/mhe/SWW/tree/master/communication-and-concurrency/6-blocking-queue/messaging/)

# Specification

  * Implement a simple messaging system, based on the client-server
    architecture, using threads to serve the clients.

  * Races and deadlocks should be avoided.

The server should be run as 

  $ java Server

The clients should be run as 

  $ java user-name server-address

If there already is a user with this nickname, in this simpled minded
design, the other user becomes innaccessible.

Once my client is running, I can send a message to John by writing
"John" and "Hello" in separate line. So the first line is the adressee
and the second line is the message. That's all we can do, again and
again, in this simpled minded design. There is no provision for the
client to end.

# Proposed solution

  * There are a variety of ways of approaching this. I will take the
    opportunity to teach Maps and BlockingQueues and assertions:

  * [Map] (https://docs.oracle.com/javase/tutorial/collections/interfaces/map.html)
  * [ConcurrentHashMap] (https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ConcurrentHashMap.html)
  * [ConcurrentMap] (https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ConcurrentMap.html)
  * [BlockingQueue] (https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/BlockingQueue.html)

## Solution outline

  * We have two threads for each client.

  * We have 2*n threads in a server attending n clients.

  * Each thread, in the client or server, does either output or input,
    but not both.

  * In the server, we use blocking queues to communicate between threads.

  * We use a map to keep a table associating queues to clients.

  * This is a simplified picture:


```
 user types  +--------------+     socket    +----------------+  queue for suitable user
 --------->  | ClientSender | ------------> | ServerReceiver | ------------------------>
             | thread       |               | thread         |  (determine using table)
             +--------------+               +----------------+
                                                           
                                                           
                                                           
                                                           
                                                           
 user reads  +----------------+    socket   +--------------+  my user's queue
 <---------  | ClientReceiver | <---------- | ServerSender | <-----------------
             | thread         |             | thread       |
             +----------------+             +--------------+ 
```
 

  * But reality is more complicated.

  * There is, in the server, one queue for each client.
  * ServerReceiver directs the message to the appropriate queue.
  * However, ServerSender reads from one queue for a specific client

## Report.java

   * A simple class for reporting normal behaviour and erroneous behaviour.

   * Its methods are all static, and we don't create objects of this class.

## Port.java

   * A class with a static variable defining the socket port, shared by the client and server.
  
## Message.java

   * Used by the server.
   * A message has the sender name and a text body.

## MessageQueue.java

   * Used by the server.
   * A blocking message queue, with offer() and take() methods.
   * offer() adds a message to the queue.
   * take() waits until a message is available in the queue, and removes and returns it.

## ClientTable.java

   * Used by the server.
   * It associates a message queue to each client name.
   * Implemented with Map.
   * More precisely with the interface ConcurrentMap using the implementation ConcurrentHashMap.

## Client.java

   * Reads user name and server address from command line.
   * Opens a socket for communication with the server.
   * Sends the user name to the server.
   * Starts two threads ClientSender and ClientReceiver.
   * Waits for them to end.
   * Then it itself ends.

## ClientSender.java

   * Loops forever doing the following.
   * Reads a recipient name from the user.
   * Reads a message from the user.
   * Sends them both to the server.

## ClientReceiver.java

   * Loops forever doing the following.
   * Reads a string from the server.
   * Prints it for the user to see.

## Server.java

   * Creates server socket.
   * Creates a client table as explained above.
   * Loops forever doing the following.
   * Waits for connection from the socket.
   * Reads the client name.
   * Updates the table with the client as the key and a new queue for it as the value.
   * Starts two threads ServerReceiver and ServerSender.
   
## ServerReceiver.java

   * Loops for ever doing the following.
   * Reads two strings from the client. One it the recipients name. The other is the message.
   * Puts the message in the queue for the recipient.
   * Uses the table to find the queue.

## ServerSender.java

   * Loops for ever reading a message from queue for its correponding
     client (ClientReceiver), and seding it to the client. The table
     is not needed, because the server sender handles one specific
     client.

