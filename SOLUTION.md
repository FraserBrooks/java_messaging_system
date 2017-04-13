# Solution

  * [messaging_system/tree/part2]
    (https://git.cs.bham.ac.uk/flb675/messaging_system/tree/part2)


My approach to part 2 is as follows:

The **Server** starts by initialising a **ClientTable** and a **DatabaseAccessObject** which uses a local database
to store clients usernames and passwords. Then attempts to listen on the given port looping forever 
handling incoming connections.

A new static method has also been added called getInput which simply accepts a String and returns it un-changed 
so long as it doesn't equal 'quit'. If it does then the method throws a new **ClientHasQuitException**.
This is a new exception I defined to allow the client to quit at any point within the server's protocol.
Anytime the server reads input from the client it feeds the it through the getInput like so:
`firstInput = Server.getInput(fromClient.readLine());`
___
The **DatabaseAccessObject** class abstracts all the code for creating and interacting with the database. 
It uses Java DB which is Oracle's supported distribution of the Apache Derby open source database. The derby.jar
file needs to be in the classpath for the Server to run. It is included in JDK 6 onwards and usually located at:
`%JAVA_HOME%\db\lib\derby.jar`
The database is stored locally and SQL commands are used by the DAO to interact with it. Everything is stored
in one table created with the sql shown below:
```java
       // SQL for statement. BLOB is used to store password/salt byte[] arrays
        String createClientsTable = 
                "CREATE TABLE CLIENTS " +
                " ( ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                "  USERNAME VARCHAR(24) NOT NULL, " +
                "  SALT BLOB NOT NULL, " +
                "  PASSWORD BLOB NOT NULL)";
```
___
The **PasswordEntry** object is a simple class I have used to pair encrypted user passwords with the salts
that were used to generate them.
```java
public class PasswordEntry {
    private byte[] salt;
    private byte[] encryptedPassword;
...
```
The passwords used to secure the client accounts are all encrypted using many iterations of the SHA-1 algorithm
along with a salt. All the functionality pertaining to encryption sits inside the **PasswordService** class.
*Not having worked with encryption before I had to look at some code online.* 
*For the PasswordService class I got most of the code from:*
> **https://www.javacodegeeks.com/2012/05/secure-password-storage-donts-dos-and.html**

The salt length and the iteration  count I have used are both much lower than they should be in a real world
application but as this is simply a learning exercise I have left them relatively low.
___
The client no longer provides a username as an argument as they now need to log on.
Upon establishing a connection with the **Server** class the **Client** creates a Sender and Receiver thread as before.
The **Server**, instead of doing the same, creates a **ServerAuthenticator** thread and passes it:
* The Socket object associated with that client.
* The input and output data streams obtained from that socket.
* References to the clientTable and DatabaseAccess objects

 `(new ServerAuthenticator(fromClient, toClient, clientTable, dao, socket)).start();`

___
The **ServerAuthenticator** class loops forever reading client commands from the InputStream and sending replies
down the OutputStream. It accepts the following commands:
* **login**  
  * asks the client for their username and their password which are then checked
* **register** 
  * asks the client for their desired username and their desired password
  * asks the user to confirm their password to avoid typos in their password
  * will not let a client take a username that already exists within the database
* **quit**
  * quits from any point in the program

The ServerAuthenticator will loop forever until one of the above commands are received.
Once a client has either logged on, or registered, their username will be added to the 
**ClientTable** along with a new **MessageQueue** and the **ServerAuthenticator** will create 
a **ServerSender** and a **ServerReceiver** thread before ending:

```java
        ServerSender serverSend = new ServerSender(clientTable.getQueue(newUser), toClient);
        serverSend.start();

        (new ServerReceiver(newUser, fromClient, toClient, clientTable, dao, serverSend, clientSocket))
                .start();
```

The **ServerReceiver** now works in much the same way as the **ServerAuthenticator**. Looping forever until one
of the following commands is received:
* **message**  
  * asks the client for the recipient's username and then the message for that user
  * if the recipient does not exist or is not online the client is informed with an appropriate message
* **people** 
  * returns a list of all logged on clients via the `toString()` method of the *ClientTable*
* **logout**
  * removes the client from the *ClientTable* 
  * interrupts the *ServerSender* for this particular client which will then end gracefully
  * starts up a new *ServerAuthenticator* thread giving it all the constructor arguments it needs that were handed to  
    the receiver thread by the original *ServerAuthenticator*: (code is identical to line in the *Server* class)
  ` (new ServerAuthenticator(myClient, toClient, clientTable, dao, clientSocket)).start();`
  * the *ServerReceiver* then ends gracefully
* **quit**
  * quits from any point in the program
___
### Ending Gracefully:
No matter what point the client decides to send the 'quit' command, all streams and sockets will be closed, all unnecessary
threads will end, and the client will close gracefully. All streams, sockets, and threads are also closed as needed in the
case of the server or the client ending unexpectedly.

* When a client enters the *'quit'* command it is sent to the Server through the PrintStream and then 
the ClientSender simply ends; whereas previously it would interrupt the linked Receiver.
* When the **getInput** method in the Server class detects the *'quit'* command and throws the **ClientHasQuitException**,
this exception is caught in either the **ServerAuhenticator** class or the **ServerReceiver**; depending on 
what stage the client was at in the Server protocol.
  * **ServerAuthenticator** 
    * If the exception is thrown in the Authenticator class there isn't much to do as the client hasn't logged on
    so we simply attempt to close the streams and the socket before ending.
  * **ServerReceiver**
    * If it is thrown in the Receiver class then we remove the client from the ClientTable and interrupt the 
    linked ServerSender instance that the ServerReceiver gets a reference to upon creation. We then attempt to 
    close the streams and sockets as within the ServerAuthenticator.

Once the **ClientSender** and **ClientReceiver** threads have been started the main **Client** thread waits for the 
**ClientSender** to end with the `Thread.join()` method. Once *'quit'* is entered the **ClientSender** ends and the 
**Client** waits a few seconds to allow the **ClientReceiver** to end gracefully. If all goes well, the **ServerReceiver** 
or Authenticator will acknowledge the quit and close both streams from the server side. Once the printStream 
is closed the bufferedReader in the **ClientReceiver** will return `null` and the Receiver will end gracefully.
If this does not happen for whatever reason the **Client** will close the inputStream itself. The **Client** will then
attempt to close the socket before exiting quietly.




  
