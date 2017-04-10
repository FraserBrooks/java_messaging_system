// This is so that server and client can use the same port without
// in-lining it, and so that only this place needs to be changed if we
// decide/need to use another port.

public class Config {
  public static final int PORTNUMBER = 4478;
  public static final int MIN_PASSWORD_LENGTH = 4;
  public static final int MIN_USERNAME_LENGTH = 4;
  
  // Database Configuration
  
  public static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
  public static final String JDBC_URL = "jdbc:derby:mesagedb;create=true";
  

}
