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
  
  //String variable to add unique buffer between two values from users
  // to guarantee uniqeness ( '!' is not allowed in usernames or group names)
  public static final String uniqueBuffer = "!!!!";
  
  public static String getCombinedID(String user1, String user2){
      String id;
      int val = user1.compareTo(user2);
      
      if(val < 0){
          id = user1 + uniqueBuffer  + user2;
      }else{
          id = user2 + uniqueBuffer + user1;
      }
      return id;
  }

}
