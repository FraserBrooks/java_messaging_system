
import java.util.concurrent.*;

public class PasswordTable {
	
	private ConcurrentMap<String, PasswordEntry> passwordTable
	= new ConcurrentHashMap<String, PasswordEntry>();
	
	public void add(String n, PasswordEntry p){
		passwordTable.put(n,p);
	}
	
	public PasswordEntry getPasswordEntry(String accountName){
		return passwordTable.get(accountName);
	}
	
	

}
