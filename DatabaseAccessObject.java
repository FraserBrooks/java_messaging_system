
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;

import networkObjects.Friend;
import networkObjects.Group;
import networkObjects.Notification;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseAccessObject {

    private Connection con = null;
    
    private String databaseHelp = "\n\n USAGE: \n" +
        "    derby.jar must be included in the classpath if it is not already \n" +
        "    java db is included in the newer versions of the JDK so use: \n" +
        "    ---> java -cp \"%JAVA_HOME%\\db\\lib\\derby.jar\"; Server  \n\n OR:  \n" +

        "    If running an older JDK, the neccessary derby.jar file has been\n" +
        "    included in the 'lib' folder in the 'src' directory so use: \n" +
        "    ---> java -cp \"%PROJECT_ROOT%\\src\\lib\\derby.jar\"; Server \n";
    
    
    
    
    // SQL commands for Prepared Statements
    private String addNewUser = 
            "INSERT INTO APP.CLIENTS " +
            "(USERNAME, SALT, PASSWORD) " +
            "VALUES (? , ? , ?) ";
   
    
    private String addNewMembership =
            "INSERT INTO APP.MEMBERSHIPS " +
            "(GROUP_ID, USER_ID, IS_ADMIN, IS_OWNER) " +
            "VALUES (?, ?, ?, ?) ";
    
    private String addNewFriendship =
            "INSERT INTO APP.FRIENDSHIPS " +
            "(ID, USER_ID_A, USER_ID_B) "  +
            "VALUES (? , ? , ?) ";
    
    private String getClientRecord =
            "SELECT * FROM APP.CLIENTS " +
            "WHERE USERNAME = ? ";
    
    private String getGroupRecord =
            "SELECT * FROM APP.GROUPS " +
            "WHERE GROUP_NAME = ? ";
    
    
    private String getMemberships = 
            "SELECT * FROM APP.MEMBERSHIPS " +
            "WHERE USER_ID = ?";
    
    DatabaseAccessObject() {
        
        try {
           Class.forName(Config.DRIVER);
            con = DriverManager.getConnection(Config.JDBC_URL);
        } catch (ClassNotFoundException | SQLException e) {
            Report.error("Database Access: Could not connect to database:  " + e.getMessage());
            Report.errorAndGiveUp(databaseHelp);
        }
        
        if(!clientTableExists()){
            createTables();
            addNewUser("!SERVER!", new PasswordEntry(new byte[2], new byte[2]));
            addNewGroup("GLOBAL CHANNEL", "!SERVER!");
        }
        
    }
    
    
    
    
    public boolean addNewUser(String username, PasswordEntry pe){
        int check = 0;
        try {
            PreparedStatement ps = con.prepareStatement(addNewUser);
            ps.setString(1, username);
            ps.setBytes(2, pe.getSalt());
            ps.setBytes(3, pe.getPassword());
            check = ps.executeUpdate(); //returns the number of rows changed (should be 1)
        } catch (SQLException e) {
            Report.error("Database Access: addNewUser() : Prepared statement failed: " + e.getMessage());
        }
        return check == 1;
    }
    
    public boolean addNewGroup(String groupName, String ownerName ){
        int check = 0;
        int ownerID;
        
        try {
            PreparedStatement ps = con.prepareStatement(getClientRecord);
            ps.setString(1, ownerName);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                ownerID = rs.getInt(1);
            }else{
                Report.error("Database Access: addNewGroup(): Could not find Owner. ");
                return false;
            }
            rs.close();
        } catch (SQLException e) {
            Report.error("Database Access: addNewGroup(): Prepared statement failed: " + e.getMessage());
            return false;
        }
        
        String addNewGroup =
                "INSERT INTO APP.GROUPS " +
                "(GROUP_NAME, OWNER_ID) " +
                "VALUES (?, ?) ";
        
        try {
            PreparedStatement ps = con.prepareStatement(addNewGroup);
            ps.setString(1, groupName);
            ps.setInt(2, ownerID);
            check = ps.executeUpdate(); //returns the number of rows changed (should be 1)
        } catch (SQLException e) {
            Report.error("Database Access: addNewGroup(): Prepared statement failed: " + e.getMessage());
            return false;
        }
        if(check != 1){
            Report.error("Database Access: addNewGroup(): Could not add group ");
            return false;
        }
        
        if(addNewMembership(groupName, ownerName, true, true)){
            return true;
        }
        Report.error("Database Access: addNewGroup(): Could not create owners group membership! ");
        return false;
    }
    
    public boolean addNewMembership(String groupName, String username, boolean isAdmin, boolean isOwner){	
        
        int groupID = -1;
        try {
            PreparedStatement ps = con.prepareStatement(getGroupRecord);
            ps.setString(1, groupName);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                groupID = rs.getInt(1);
            }
            rs.close();
        } catch (SQLException e) {
            Report.error("Database Access: addNewMembership: Prepared statement failed: " + e.getMessage());
            return false;
        }
        if(groupID == -1){
            Report.error("Database Access: addNewMembership: Could not find the specified Group. ");
            return false;
        }
        
        int userID = -1;
        try {
            PreparedStatement ps = con.prepareStatement(getClientRecord);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                userID = rs.getInt(1);
            }
            rs.close();
        } catch (SQLException e) {
            Report.error("Database Access: addNewMembership: Prepared statement failed: " + e.getMessage());
            return false;
        }
        if(userID == -1){
            Report.error("Database Access: addNewMembership: Could not find the specified Client. ");
            return false;
        }
        
        int check = 0;
        try {
            PreparedStatement ps = con.prepareStatement(addNewMembership);
            ps.setInt(1, groupID);
            ps.setInt(2, userID);
            ps.setBoolean(3, isAdmin);
            ps.setBoolean(4, isOwner);
            check = ps.executeUpdate(); //returns the number of rows changed (should be 1)
        } catch (SQLException e) {
            Report.error("Database Access: addNewMembership: Prepared statement failed: " + e.getMessage());
        }
        return check == 1;
    }

    public boolean addNewFriendship(String username1, String username2){	
        int userID1 = -1;
        try {
            PreparedStatement ps = con.prepareStatement(getClientRecord);
            ps.setString(1, username1);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                userID1 = rs.getInt(1);
            }
            rs.close();
        } catch (SQLException e) {
            Report.error("Database Access: addNewFriendship: Prepared statement failed: " + e.getMessage());
            return false;
        }
        if(userID1 == -1){
            Report.error("Database Access: addNewFriendship: Could not find the specified Client. ");
            return false;
        }
        
        int userID2 = -1;
        try {
            PreparedStatement ps = con.prepareStatement(getClientRecord);
            ps.setString(1, username2);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                userID2 = rs.getInt(1);
            }
            rs.close();
        } catch (SQLException e) {
            Report.error("Database Access: addNewFriendship: Prepared statement failed: " + e.getMessage());
            return false;
        }
        if(userID2 == -1){
            Report.error("Database Access: addNewFriendship: Could not find the specified Client. ");
            return false;
        }
        
        String id = Config.getCombinedID(username1, username2);
        
        int check = 0;
        try {
            PreparedStatement ps = con.prepareStatement(addNewFriendship);
            ps.setString(1, id);
            ps.setInt(2, userID1);
            ps.setInt(3, userID2);
            check = ps.executeUpdate(); //returns the number of rows changed (should be 1)
        } catch (SQLException e) {
            Report.error("Database Access: addNewMembership: Prepared statement failed: " + e.getMessage());
        }
        return check == 1;
    }
    
    public boolean clientTableExists(){
        boolean value = false;
        try {
            
            DatabaseMetaData md = con.getMetaData();
            // Search for any table in the database named 'CLIENTS'
            ResultSet rs = md.getTables(null, null, "CLIENTS", null);

            // Returns false when there isn't a next row
            // so should return true once only if CLIENTS
            // table exists
            if (rs.next()) {
                value = true;
            }
            rs.close();
        } catch (SQLException e) {
            Report.errorAndGiveUp("Database Access: clientTableExists() " + e.getMessage());
            e.printStackTrace();
        }
        return value;
    }

    public void createTables(){
        
        Statement statement = null;
        
        // SQL for statement. BLOB is used to store password/salt byte[] arrays
        String createClientsTable = 
                "CREATE TABLE CLIENTS " +
        
                        " ( USER_ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY, " +  
                        "  USERNAME VARCHAR(24) NOT NULL, " + 
                        "  SALT BLOB NOT NULL, " + 
                        "  PASSWORD BLOB NOT NULL, " + 
                        "  ICON BLOB, " + 
                        "  NOTIFICATIONS BLOB )"; 
        try {
            statement = con.createStatement(); 
            statement.execute(createClientsTable); 
        } catch (SQLException e) { 
            Report.errorAndGiveUp("Database Access: createTables() " + e.getMessage()); 
        }
        
        String createGroupsTable = 
                "CREATE TABLE GROUPS (" +
                        "  GROUP_ID   INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY," +
                        "  GROUP_NAME VARCHAR(24) NOT NULL, " +
                        "  OWNER_ID INT NOT NULL, " +
                        "  CONVERSATION_LOG BLOB, " +
                        "  FOREIGN KEY (OWNER_ID) REFERENCES CLIENTS(USER_ID)  ) ";
        
        try {
            statement = con.createStatement();
            statement.execute(createGroupsTable);
        } catch (SQLException e) {
            Report.errorAndGiveUp("Database Access: createTables() " + e.getMessage());
        }
        
        String createMembershipsTable = 
                "CREATE TABLE MEMBERSHIPS (" +
        
                        "  GROUP_ID INT NOT NULL, " +
                        "  USER_ID INT NOT NULL, " +
                        "  IS_ADMIN BOOLEAN NOT NULL, " +
                        "  IS_OWNER BOOLEAN NOT NULL, " +
                        
                        "  FOREIGN KEY (GROUP_ID) REFERENCES GROUPS(GROUP_ID), " +
                        "  FOREIGN KEY (USER_ID) REFERENCES CLIENTS(USER_ID), " + 
                        "  CONSTRAINT GROUP_PERSON_PAIR PRIMARY KEY (GROUP_ID, USER_ID) )";
        
        try {
            statement = con.createStatement();
            statement.execute(createMembershipsTable);
        } catch (SQLException e) {
            Report.errorAndGiveUp("Database Access: createTables() " + e.getMessage());
        }
        
        String createFriendshipsTable = 
                "CREATE TABLE FRIENDSHIPS (" +
                        "  ID VARCHAR(60) NOT NULL PRIMARY KEY, " +
                        "  USER_ID_A INT NOT NULL, " +
                        "  USER_ID_B INT NOT NULL, " +
                        "  CONVERSATION_LOG BLOB, " +
                        
                        "  FOREIGN KEY (USER_ID_A) REFERENCES CLIENTS(USER_ID), " +
                        "  FOREIGN KEY (USER_ID_B) REFERENCES CLIENTS(USER_ID) ) ";
        
        try {
            statement = con.createStatement();
            statement.execute(createFriendshipsTable);
        } catch (SQLException e) {
            Report.errorAndGiveUp("Database Access: createTables() " + e.getMessage());
        }
  
    }
    
    public MessageQueue getGroupConversation(String group_name){	

        byte[] messages = new byte[1];
        
        try {
            PreparedStatement ps = con.prepareStatement(getGroupRecord);
            ps.setString(1, group_name);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                messages = rs.getBytes(3);
            }else{
                Report.error("Database Access: getGroupConversation: Could not find the specified Group. ");
                return new MessageQueue();
            }
            rs.close();
        } catch (SQLException e) {
            Report.error("Database Access: getGroupConversation: Prepared statement failed: " + e.getMessage());
            return new MessageQueue();
        }
        
        
        MessageQueue mq = null;
        try (ByteArrayInputStream fromDB = new ByteArrayInputStream(messages);
                ObjectInputStream oi = new ObjectInputStream(fromDB)){
            mq = (MessageQueue) oi.readObject();
        } catch (IOException e){
            Report.error("Database Access: getGroupConversation: IO failure " + e.getMessage());
            return new MessageQueue();
        } catch (ClassNotFoundException e) {
            Report.error("Database Access: getGroupConversation: Class not found " + e.getMessage());
            return new MessageQueue();
        } 

        return mq;
    }
    
    public MessageQueue getPrivateConversation(String username1, String username2){
        
        byte[] messages = new byte[1];
        
        String id = Config.getCombinedID(username1, username2);

        
        String getFriendship = " SELECT * FROM APP.FRIENDSHIPS " +
                               " WHERE ID = ? " ;
        
        try {
            PreparedStatement ps = con.prepareStatement(getFriendship);
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                messages = rs.getBytes(4);
            }else{
                Report.error("Database Access: getPrivateConversation: Could not find the specified Friendship entry.");
                return new MessageQueue();
            }
            rs.close();
        } catch (SQLException e) {
            Report.error("Database Access: getPrivateConversation: Prepared statement failed: " + e.getMessage());
            return new MessageQueue();
        }
        
        if(messages == null){
            return new MessageQueue();
        }
        
        MessageQueue mq = null;
        try (ByteArrayInputStream fromDB = new ByteArrayInputStream(messages);
                ObjectInputStream oi = new ObjectInputStream(fromDB)){
            mq = (MessageQueue) oi.readObject();
        } catch (IOException e){
            Report.error("Database Access: getPrivateConversation: IO failure " + e.getMessage());
            return new MessageQueue();
        } catch (ClassNotFoundException e) {
            Report.error("Database Access: getPrivateConversation: Class not found " + e.getMessage());
            return new MessageQueue();
        } 
        
        return mq;
    }
    
    @SuppressWarnings("unchecked")
    public ArrayList<Notification> getNotifications(String username){
        
        ArrayList<Notification> notifications = new ArrayList<Notification>();
        byte[] bytes = new byte[1];
        
        try {
            PreparedStatement ps = con.prepareStatement(getClientRecord);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                bytes = rs.getBytes(6);
            }else{
                Report.error("Database Access: getNotifications: Could not find the specified Client. ");
                return notifications;
            }
            rs.close();
        } catch (SQLException e) {
            Report.error("Database Access: getNotifications: Prepared statement failed: " + e.getMessage());
            return notifications;
        }
        
        if(bytes == null){
            return notifications;
        }
        

        try (ByteArrayInputStream fromDB = new ByteArrayInputStream(bytes);
                ObjectInputStream oi = new ObjectInputStream(fromDB)){
            notifications = (ArrayList<Notification>) oi.readObject();
        } catch (IOException e){
            Report.error("Database Access: getNotifications: IO failure " + e.getMessage());
        } catch (ClassNotFoundException e) {
            Report.error("Database Access: getNotifications: Class not found " + e.getMessage());
        } 

        return notifications;
    }
    
    public boolean storeGroupConversation(MessageQueue mq, String id){
        if(mq == null){
            return true;
        }
        
        String sql = " UPDATE APP.GROUPS " +
                     " SET CONVERSATION_LOG = ? " +
                     " WHERE GROUP_NAME = ? ";
        int check = 0;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream os = new ObjectOutputStream(bos)){
            
            Report.behaviour("Server: storeGroupConversation: storing queue starting: " + mq );
            os.writeObject(mq);
            os.flush();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setBytes(1, bos.toByteArray());
            ps.setString(2, id);
            check = ps.executeUpdate(); //returns the number of rows changed (should be 1)
        } catch (IOException e){
            Report.error("Database Access: storeGroupConversation: IO failure " + e.getMessage());
        }  catch (SQLException e) {
            Report.error("Database Access: storeGroupConversation: Prepared statement failed: " + e.getMessage());
            return false;
        }
        
        if(check != 1){
            Report.error("Database Access: storeGroupConversation: Nothing was changed ");
            return false;
        }
        return true;
    }
    
    public boolean storePrivateConversation(MessageQueue mq, String id){	
        
        if(mq == null){
            return true;
        }
        
        String sql = " UPDATE APP.FRIENDSHIPS " +
                        " SET CONVERSATION_LOG = ? " + 
                        " WHERE ID = ? ";

        int check = 0;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream os = new ObjectOutputStream(bos)) {
            Report.behaviour("Server: storePrivateConversation: storing queue starting: " + mq );
            os.writeObject(mq);
            os.flush();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setBytes(1, bos.toByteArray());
            ps.setString(2, id);
            check = ps.executeUpdate(); // returns the number of rows changed
                                        // (should be 1)
        } catch (IOException e) {
            Report.error("Database Access: storePrivateConversation: IO failure " + e.getMessage());
        } catch (SQLException e) {
            Report.error("Database Access: storePrivateConversation: Prepared statement failed: " + e.getMessage());
            return false;
        }

        if (check != 1) {
            Report.error("Database Access: storePrivateConversation: Nothing was changed ");
            return false;
        }

        return true;
    }
    
    public boolean storeNotification(Notification notif, String username){
        
        ArrayList<Notification> notifications = getNotifications(username);
        notifications.add(notif);
        
        String sql = " UPDATE APP.CLIENTS " +
                " SET NOTIFICATIONS = ? " +
                " WHERE USERNAME = ? ";
        
        int check = 0;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream os = new ObjectOutputStream(bos)){
            os.writeObject(notifications);
            os.flush();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setBytes(1, bos.toByteArray());
            ps.setString(2, username);
            check = ps.executeUpdate(); //returns the number of rows changed (should be 1)
        } catch (IOException e){
            Report.error("Database Access: storeNotification: IO failure " + e.getMessage());
        }  catch (SQLException e) {
            Report.error("Database Access: storeNotification: Prepared statement failed: " + e.getMessage());
            return false;
        }
        
        if(check != 1){
            Report.error("Database Access: storeNotification: Nothing was changed ");
            return false;
        }
        
        return true;
    }
    
    
    public ArrayList<Friend> getFriendList(String username){
        
        ArrayList<Friend> friendArr = new ArrayList<Friend>();
        ArrayList<Integer> tempArray = new ArrayList<Integer>();
        
        // Get userID
        int userID;
        try {
            PreparedStatement ps = con.prepareStatement(getClientRecord);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                userID = rs.getInt(1);
            }else{
                Report.error("Database Access: getFriendList: Could not find the specified Client. ");
                return new ArrayList<Friend>();
            }
            rs.close();
        } catch (SQLException e) {
            Report.error("Database Access: getFriendList: Prepared statement failed: " + e.getMessage());
            return new ArrayList<Friend>();
        }
        
        String sql = "SELECT FRIENDSHIPS.USER_ID_A, FRIENDSHIPS.USER_ID_B " + 
                " FROM FRIENDSHIPS WHERE USER_ID_A = ? OR USER_ID_B = ? ";
        
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userID);
            ps.setInt(2, userID);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                tempArray.add(rs.getInt(1));
                tempArray.add(rs.getInt(2));
            }
            rs.close();
        } catch (SQLException e) {
            Report.error("Database Access: getFriendList: Prepared statement failed while selecting user ID's: " + e.getMessage());
            return new ArrayList<Friend>();
        }
        

        // remove all  instances of the userID from our
        // tempArray (we only want Friend ID's)
        while(tempArray.remove(new Integer(userID))){
            // Delete again
        }
        
        String getFriendNames =
                "SELECT CLIENTS.USERNAME FROM CLIENTS " +
                "WHERE USER_ID =  " + Integer.toString(-1);
        
        for(Integer i: tempArray){
            getFriendNames += " OR USER_ID = " + i.toString();
        }
        
        try {
            PreparedStatement ps = con.prepareStatement(getFriendNames);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                friendArr.add(new Friend(rs.getString(1), null));
            }
            rs.close();
        } catch (SQLException e) {
            Report.error("Database Access: getFriendList: Prepared statement failed while selecting usernames: " + e.getMessage());
        }
        
        
        return friendArr;
 
    }
    
    public ArrayList<Group> getGroupList(String username){
        
        
        ArrayList<Group> groupArr = new ArrayList<Group>();
        int userID = -1;
        try {
            PreparedStatement ps = con.prepareStatement(getClientRecord);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                userID = rs.getInt(1);
            }
            rs.close();
        } catch (SQLException e) {
            Report.error("Database Access: getGroupList: Prepared statement failed: " + e.getMessage());
        }
        if(userID == -1){
            Report.error("Database Access: getGroupList: Could not find the specified Client. ");
        }
        
        
        ArrayList<Integer> groupIDArray = new ArrayList<Integer>();
        ArrayList<Integer> clientStatusArray = new ArrayList<Integer>();
        
        try {
            PreparedStatement ps = con.prepareStatement(getMemberships);
            ps.setInt(1, userID);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                if(rs.getBoolean(4)){
                    groupIDArray.add(rs.getInt(1));
                    clientStatusArray.add(3); // Is Owner
                }else if(rs.getBoolean(3)){
                    groupIDArray.add(rs.getInt(1));
                    clientStatusArray.add(2); // Is Admin
                } else {
                    groupIDArray.add(rs.getInt(1));
                    clientStatusArray.add(1); // Regular Member
                }
            }
            rs.close();
        } catch (SQLException e) {
            Report.error("Database Access: getGroupList: Prepared statement failed: " + e.getMessage());
        }
        
        String getGroupNames =
                "SELECT GROUPS.GROUP_NAME FROM GROUPS " +
                "WHERE GROUP_ID =  " + Integer.toString(-1);
        
        for(Integer i: groupIDArray){
            getGroupNames += " OR GROUP_ID = " + i.toString();
        }
        
        int index = 0;
        try {
            PreparedStatement ps = con.prepareStatement(getGroupNames);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                if(clientStatusArray.get(index) == 3){
                    groupArr.add(new Group(rs.getString(1), true, true));
                } else if (clientStatusArray.get(index) == 2){
                    groupArr.add(new Group(rs.getString(1), false, true));
                } else {
                    groupArr.add(new Group(rs.getString(1), false, false));
                }
            }
            rs.close();
        } catch (SQLException e) {
            Report.error("Database Access: getGroupList: Prepared statement failed: " + e.getMessage());
        }
        
        
        return groupArr;
    }
    
    public PasswordEntry getPasswordEntry(String user){
        PasswordEntry pass = null;
        try {
            PreparedStatement ps = con.prepareStatement(getClientRecord);
            ps.setString(1, user);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                pass = new PasswordEntry(rs.getBytes(3), rs.getBytes(4));
            }
            rs.close();
        } catch (SQLException e) {
            Report.error("Database Access: getPasswordEntry(): Prepared statement failed: " + e.getMessage());
        }
        return pass;
    }
    
    public boolean removeFriendship(String friendshipID){
        String sqlDelete = "DELETE FROM FRIENDSHIPS " +
                            " WHERE ID = ? ";
        int rowsChanged = 0;
        try {
            PreparedStatement ps = con.prepareStatement(sqlDelete);
            ps.setString(1, friendshipID);
            rowsChanged = ps.executeUpdate();
        } catch (SQLException e) {
            Report.errorAndGiveUp("Database Access: removeFriendship() " + e.getMessage());
        }
        
        
        return rowsChanged == 0;
    }
    
    public boolean removeMembership(String groupName, String username){
        // Get userID
        int userID;
        try {
            PreparedStatement ps = con.prepareStatement(getClientRecord);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                userID = rs.getInt(1);
            }else{
                Report.error("Database Access: removeMembership(): Could not find the specified Client. ");
                return false;
            }
            rs.close();
        } catch (SQLException e) {
            Report.error("Database Access: removeMembership(): Prepared statement failed: " + e.getMessage());
            return false;
        }
        
        String sqlDelete = "DELETE FROM MEMBERSHIPS " +
                           " WHERE USER_ID = ? ";
        int rowsChanged = 0;
        try {
            PreparedStatement ps = con.prepareStatement(sqlDelete);
            ps.setInt(1, userID);
            rowsChanged = ps.executeUpdate();
        } catch (SQLException e) {
            Report.errorAndGiveUp("Database Access: removeMembership() " + e.getMessage());
        }
        
        
        return rowsChanged != 0;
        
    }
    
    public boolean removeGroup(String groupName){
        
        // Get the group ID from groupName
        int groupID;
        try {
            PreparedStatement ps = con.prepareStatement(getGroupRecord);
            ps.setString(1, groupName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                groupID = rs.getInt(1);
            } else {
                Report.error("Database Access: removeGroup(): Could not find the specified Group. ");
                return false;
            }
            rs.close();
        } catch (SQLException e) {
            Report.error("Database Access: removeGroup(): Prepared statement failed: " + e.getMessage());
            return false;
        }
        
        
        // Delete all memberships for the group to delete in the MEMBERSHIPS table
        String sqlDelete = "DELETE FROM MEMBERSHIPS WHERE GROUP_ID = ? ";
        int rowsChanged = 0;
        try {
            PreparedStatement ps = con.prepareStatement(sqlDelete);
            ps.setInt(1, groupID);
            rowsChanged = ps.executeUpdate();
        } catch (SQLException e) {
            Report.error("Database Access: removeGroup(): Failed to delete Memberships " + e.getMessage());
        }
        
        // Delete the group entry in the group table now that all references to it have been removed
        sqlDelete = "DELETE FROM GROUPS WHERE GROUP_ID = ? ";
        try {
            PreparedStatement ps = con.prepareStatement(sqlDelete);
            ps.setInt(1, groupID);
            if (ps.executeUpdate() == 0) {
                Report.error("Database Access: removeGroup(): Delete Failed: ");
            }
        } catch (SQLException e) {
            Report.error("Database Access: removeGroup(): Prepared statement failed: " + e.getMessage());
            return false;
        }
        
        

        return rowsChanged != 0;
    }
    
}


