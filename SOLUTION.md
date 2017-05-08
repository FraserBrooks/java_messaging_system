# Solution

  * [messaging_system/tree/part3]
    (https://git.cs.bham.ac.uk/flb675/messaging_system/tree/part3)


My approach to part 3 is as follows:

I plan to expand the database to allow for the creation and management of groups and hopefully also to 
allow users to add other members as friends which functionally will work as two-user limited groups.
I also intend to create a GUI to make the application easier to interact with (especially with the 
new features). This GUI will be programmed using the Java Swing toolkit. Below is a sketch describing
my design for the GUI:

![picture not found](../guiDesign.jpg "GUI Design")

To expand the database to support this new functionality it will have to become a relational database.
As of part2 there is only one table (the CLIENTS table) but at the very least I am going to need
a GROUPS table and some other table to connect the two as it is a many to many relationship (one 
group can have many clients and one client can have many groups).
___
___
## Implementation Details:

The **Server** once again starts by initialising a **ClientTable** and a **DatabaseAccessObject** which
now uses an expanded version of the local derby database from part 2 to store much more than just usernames
and passwords. The **Server** then also creates an instance of the **ServerInstance** class which holds various
data members needed by the server that aren't linked to any particular client. The **Server** then, as before,
loops forever listening for and accepting new clients, starting a new **ServerAuthenticator** for each client.

The **ClientHasQuitException** and the `Server.getInput()` method that was used to allow the user to
quit have both been deleted as communication between **Server** and **Client** is now much more sophisticated.

The **Client** side has been completely revised with a GUI that makes it much easier to interact with the 
software. The bulk of this GUI is implemented in the **ClientGUI** with various `listener` classes being
employed as needed. The **Client** still receives all messages through a **ClientReceiver** and sends all 
messages from a **ClientSender** class. The big difference now is that rather than sending text via a 
`PrintStream` the two sides of the application send serialized instances of the `SerializableMessage`
class via object output and input streams.
___
### ClientGUI
![picture not found](../finalGui.jpg "Application GUI")

The Client user interface is shown in the screenshot above. The interface is made up of three panels that can be
adjusted in size via two sliders. The left panel contains the Group list and Group related buttons (Create/Remove group,
add/remove user) and the right panel contains the friends list and the friend related buttons (add/remove friend) along
with the button to set your profile picture. The main central pane is where the messages are sent/viewed.

CLicking any of the buttons triggers a drop-down window to appear at the top of the central pane, as does receiving
a notification. Shown below:

![picture not found](../finalGui2.jpg "Application GUI")

The bulk of the UI is made up of two **JSplitPane**'s (one nested in the other) to make the three columns, three
**JScrollPane**'s (one for the group list, one for the friend list, and one for the messageView), and a **JTextArea**
for the user message input.

___
### ServerInstance
The **ServerInstance** class represents a single instance of the running server. This class keeps track of all incoming
friend requests (a friendship can only be created when a matching pair of friend requests are received) and also keeps track
of all the running conversations.
##### Running Conversation?
Rather than having several conversation tables in the database and every message being stored and retrieved individually, 
I figured it would be more efficient if messages were handled as whole conversations. And so were retrieved from and stored in 
the database as infrequently as possible so as to limit the strain on the database.
##### ActiveConversation
A 'conversation' in  this context is a container for the last 50 messages between any two friends or group. When a Client
loads the **MessageWindow** associated with a particular conversation, a **StartConversation** message is serialized and sent 
to the server. The **ServerReceiver** then hands this request to the **ServerInstance** which then checks a concurrent hash
map containing **ActiveConversation** objects. If the conversation is found the client is added to the conversation and sent a copy
of all the messages in the **ActiveConversation**'s message queue. If the conversation is not found then it is created and 
the **ServerInstance** will attempt to load the stored conversation from the database.


___
The constructor for **SerializableMessage** takes two parameters, a String for `sender` and a String for `type`.
**SerializableMessage** is subclassed by numerous other classes that are kept with the **SerializableMessage** 
class in a new `networkObjects` package. Each receiver reconstructs the objects from it's inputStream
as **SerializableMessage** objects and then uses the `type` String (which is set in the constructor of each 
subclass) to safely cast that object to it's specific subclass type. The `networkObjects` subclasses are briefly
outlined below:
* **AddUserToGroup**
* **AuthAttempt**
* **CreateGroup**
* **DeleteGroup**
* **FriendRequest**
* **Notification**
* **RemoveFriend**
* **RemoveGroup**
* **RemoveUserFromGroup**
* **ClientInfoObject** 
    * Sent by the server to the client after a successful login. Contains the clients friends, groups, and notifications 
* **ServerMessage**
    * Message from the server to the client. Could be an error message or a new friend/group 
* **StartConversation**
    * Sent by the client to the server to load a new group or private conversation. For example when userA clicks on his friend userB 
      to send a message, a **StartConversation** will be sent. If the conversation is already active on the server (for example if userB
      had already started it and was writing his own message) then userA simply gets added to the conversation and gets sent all
      messages in the **ActiveConversation**'s **messageQueue**. If the conversation isn't currently running then the server will attempt
      to load the appropriate CONVERSATION_LOG from the database and start the new conversation.

The classes listed above use several complementary classes named **Friend**, **Group**, and **Message** that of course are also Serializable 
so they can be sernt inside the classes listed above. The **Message** class is a simple container for a String message, and a Date object 
indicating the time the message was created. The **Friend** and **Group** class simply act as containers for the the data members  that
the clientGUI needs for it's functionality (eg. username/groupName, Icon, etc.). They are created from records received from the database
but have no access back to those records or the database.
___
The **DatabaseAccessObject** class abstracts all the code for creating and interacting with the database. 
It uses Java DB which is Oracle's supported distribution of the Apache Derby open source database. The derby.jar
file needs to be in the classpath for the Server to run. It is included in JDK 6 onwards and usually located at:
`%JAVA_HOME%\db\lib\derby.jar`
The database is stored locally and SQL commands are used by the DAO to interact with it.
When **Server** is ran for the first time the following tables are created:
```java
// SQL for statement. BLOB is used to store password/salt byte[] arrays
    String createClientsTable = 
        "CREATE TABLE CLIENTS " +
        " (USER_ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,"+  
        " USERNAME VARCHAR(24) NOT NULL, " + 
        " SALT BLOB NOT NULL, " + 
        " PASSWORD BLOB NOT NULL, " + 
        " ICON BLOB, " + 
        " NOTIFICATIONS BLOB )"; 
        
    String createGroupsTable = 
        "CREATE TABLE GROUPS (" +
        "GROUP_ID   INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,"+
        "GROUP_NAME VARCHAR(24) NOT NULL, " +
        "OWNER_ID INT NOT NULL, " +
        "CONVERSATION_LOG BLOB, " +
        "FOREIGN KEY (OWNER_ID) REFERENCES CLIENTS(USER_ID)  ) ";
        
    String createMembershipsTable = 
        "CREATE TABLE MEMBERSHIPS (" +
        " GROUP_ID INT NOT NULL, " +
        " USER_ID INT NOT NULL, " +
        " IS_ADMIN BOOLEAN NOT NULL, " +
        " IS_OWNER BOOLEAN NOT NULL, " +
        " FOREIGN KEY (GROUP_ID) REFERENCES GROUPS(GROUP_ID), " +
        " FOREIGN KEY (USER_ID) REFERENCES CLIENTS(USER_ID), " + 
        " CONSTRAINT GROUP_PERSON_PAIR PRIMARY KEY (GROUP_ID, USER_ID) )";
        
    String createFriendshipsTable = 
        "CREATE TABLE FRIENDSHIPS (" +
        " ID VARCHAR(60) NOT NULL PRIMARY KEY, " +
        " USER_ID_A INT NOT NULL, " +
        " USER_ID_B INT NOT NULL, " +
        " CONVERSATION_LOG BLOB, " +
        " FOREIGN KEY (USER_ID_A) REFERENCES CLIENTS(USER_ID), " +
        " FOREIGN KEY (USER_ID_B) REFERENCES CLIENTS(USER_ID) ) ";

```
As seen above, four tables are created to store various data members for the application. These four tables are described below:
* CLIENTS
    * The CLIENTS table once again stores a username and a  SALT/PASSWORD combo but also now allows for 
     the storage of an ICON and NOTIFICATIONS object (both also stored as BLOBs). When needed these objects
     are read from the database as `byte[]` arrays and then converted back into appropriate objects via a 
     `ByteArrayInputStream` wrapped in a `ObjectInputStream`. This is the main methodology I have used to 
     store objects in the database when needed.
* GROUPS
    * This new GROUPS table stores information pertaining to groups created by Clients. Every group has 
     a name, an ID, and an owner which is a foreign key that references the USER_ID in CLIENTS. Memberships
     for any particular group are stored in the MEMBERSHIPS table:
    * Each GROUP record also has a BLOB data type field for the most recent 50 messages sent to the group.
* MEMBERSHIPS
    * Every record in this table contains a USER_ID and a GROUP_ID which are both foreign keys to the obvious
     tables but together they also make up the Primary Key for this table meaning that each pair must be unique.
     This ensures that no Client can have more than one membership for any particular group. 
    * In every record there are two booleans indicating whether the particular client is the owner and/or an admin.
* FRIENDSHIPS
    * This table works similarly to the MEMBERSHIPS table. Each record represents two users who have added each 
     other as friends. 
    * The table also stores the last 50 messages between the two friends as a BLOB just like in
     the GROUP table.

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

### Ending Gracefully:
Currently the **Server** has no provision to end gracefully and the only way to end the **Client** is to close
the GUI window which leaves the socket open unitl Java cleans up after itself which is not ideal. The **Server** 
will stay running in this situation and continue to accept clients but a better implementation is on the to do list. 

Another issue is that the **ClientGUI** currently has no means to detect a fault with the **ClientReceiver** or 
**ClientSender** such as finishing execution due to an IOException. In this case the **ClientGUI** will remain
open and responsive but nothing will be sent or received from the server. again, this is on the todo list.



  
