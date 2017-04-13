
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
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
            Report.error("Database Access: Could not connect to database:  " + e.getMessage());
            Report.errorAndGiveUp(databaseHelp);
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