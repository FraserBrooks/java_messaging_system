
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseAccessObject {

    private Connection con = null;
    
    // SQL commands for Prepared Statements
    private String addNewUser = 
            "INSERT INTO APP.CLIENTS " +
            "(USERNAME, SALT, PASSWORD)" +
            "VALUES (? , ? , ?) ";
    private String getRecord =
            "SELECT * FROM APP.CLIENTS " +
           "WHERE USERNAME = ? ";
    
    DatabaseAccessObject() {
        
        try {
           Class.forName(Config.DRIVER);
            con = DriverManager.getConnection(Config.JDBC_URL);
        } catch (ClassNotFoundException | SQLException e) {
            Report.errorAndGiveUp("Database Access: Could not connect to database:  " + e.getMessage());
        }
        
        if(!clientTableExists()){
            createClientTable();
        }
        
    }
    
    
    public PasswordEntry getPasswordEntry(String user){
        PasswordEntry pass = null;
        try {
            PreparedStatement ps = con.prepareStatement(getRecord);
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
    
    public boolean addNewUser(String username, PasswordEntry pe){
        boolean success = false;
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
        if(check == 1){
            success = true;
        }
        return success;
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
    
    public void createClientTable(){

        // SQL for statement. BLOB is used to store password/salt byte[] arrays
        String createClientsTable = 
                "CREATE TABLE CLIENTS " +
                        " ( ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                        "  USERNAME VARCHAR(24) NOT NULL, " +
                        "  SALT BLOB NOT NULL, " +
                        "  PASSWORD BLOB NOT NULL)";
        
        
        Statement statement = null;
        try {
            statement = con.createStatement();
            statement.execute(createClientsTable);
        } catch (SQLException e) {
            Report.errorAndGiveUp("Database Access: createClientTable() " + e.getMessage());
        }
        
        
    }

}