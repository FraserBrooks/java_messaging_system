
public class PasswordEntry {
	
	private byte[] salt;
	private byte[] encryptedPassword;
	
	PasswordEntry(byte[] s, byte[] e){
		salt = s;
		encryptedPassword = e;
	}
	
	public byte[] getSalt(){
		return salt;
	}
	
	public byte[] getPassword(){
		return encryptedPassword;
	}

}
