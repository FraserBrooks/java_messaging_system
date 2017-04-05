#Simple Message System

  * Based on the client-server architecture with server threads and socket communication.

## Solution

  * [communication-and-concurrency/6-blocking-queue/messaging/]
    (https://git.cs.bham.ac.uk/mhe/SWW/tree/master/communication-and-concurrency/6-blocking-queue/messaging/)

## Specification

  * Implement a simple messaging system, based on the client-server architecture, using threads to serve the clients.

  * Races and deadlocks should be avoided.

>The server should be run as  `$ java Server`

>The clients should be run as  `$ java server-address`

Once client is started a connection is made to the server and the client can attempt to log in with the **login** command, attempt to register as a new user with the **register** command or quit at any time by sending the **quit** command. Input is sent to the server one line at a time.

While logged in a client can request a list of all active users with the **people** command. Message a specific user with the **message** command or log out with the **logout** command.

Upon sending the **message** command the user will be prompted for the recipient and then for the message. If the user exists and is active the message will be sent in the form:
>	`From sender: message`

The user accounts are protected by encrypted passwords. The passwords are encrypted with the PBKDF2 *(Password Based Key Derivation Function)* algorithm which uses a hashing algorithm *(in this case SHA-1)* and applies the hashing function to the password with a salt many times over to produce a stronger encryption. This process is known as key-stretching. Using a salt may be overkill in this simple messaging exercise but it's a standard practice in real applications.


## Solution outline
  * We have two threads in the client for sending and receiving input.
  * For each client the server will create a single authentication thread that handles the login/register process. Once a client has logged in the server will create two threads for sending and receiving input.

  * Once a client has sent the **logout** command the two threads on the server handling that particular client will pass the client to a new authentication thread and then close gracefully.

  * In the server, we use blocking queues to communicate between threads.

  * We use two maps to keep two tables for client names and their queues/passwords

  * This is a simplified picture:


![alt text](../picture.jpg "Application Diagram")
 

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

   * Reads server address from command line.
   * Opens a socket and creates streams for communication with the server.
   * Starts two threads ClientSender and ClientReceiver.
   * Waits for them to end.
   * Then it itself ends.

## ClientSender.java

   * Loops forever doing the following.
   * Reads an input from the user.
   * Sends it to the server if it is non-empty string.
   * If input is the **quit** command then exit.

## ClientReceiver.java

* Loops forever doing the following.
* Reads a string from the server.
* Prints it for the user to see.
* If inputStream returns null then end thread
  * Happens when the Server closes stream after **quit**
* If Exception is thrown then interrupt ClientSender

## Server.java

   * Creates server socket.
   * Creates a client table and password table
   * Loops forever doing the following.
   * Waits for connection from the socket.
   * Passes the connection to a new ServerAuthenticator

## ServerAuthenticator.java

   * Sends welcome message to user
   * Loops forever until client logs on or quits
   * Reads user input and runs the appropriate method:
   	* Log in
   	* Register
   	* Quit
   * Once a client has logged on it creates a ServerReceiver
      and Sender thread to handle the client and adds the client to the client table.
    
## ServerReceiver.java
* Loops forever doing the following
* Reads user input and runs the appropriate method:
  * Message
  * Log Out
  * People
  * Quit
* If a client logs out then remove them from the client table  
and create a new ServerAuthenicator

## ServerSender.java

   * Loops forever reading a message from queue for its corresponding client (ClientReceiver), and sending it to the client. The table is not needed, because the server sender handles one specific client.
     
## ClientHasQuitException

   * Exception to be thrown when a client enters the **quit** command no matter where they are in the protocol.

## PasswordTable.java
* Used by the server.
   * It associates a PasswordEntry to each client name.
   * Implemented with Map.
   * More precisely with the interface ConcurrentMap using the implementation ConcurrentHashMap.
   
## PasswordEntry.java
* Class for grouping salt with encrypted password
## PasswordService.java
* Static class that handles all encryption and authorisation 
