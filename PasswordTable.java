
import java.util.concurrent.*;

public class PasswordTable {
	
	private ConcurrentMap<String, PasswordEntry> passwordTable
	= new ConcurrentHashMap<String, PasswordEntry>();
	
	public void add(String n, PasswordEntry p){
		passwordTable.put(n,p);
	}
	
	public PasswordEntry getPasswordEntry(String accountName){
		if(accountName == null){
			return null;
		}
		return passwordTable.get(accountName);
	}
	
	public boolean isInTable(String accountName){
		if(accountName == null){
			return false;
		}
		return passwordTable.containsKey(accountName);
	}
	

}
