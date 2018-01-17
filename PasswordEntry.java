import java.io.Serializable;

// Pairs a salt with the encrypted password that used it
// All passwords are stored within an instance of this class

public class PasswordEntry implements Serializable{


    
    /**
     * 
     */
    private static final long serialVersionUID = -4012584625488423518L;
    private byte[] salt;
    private byte[] encryptedPassword;

    PasswordEntry(byte[] s, byte[] e) {
        salt = s;
        encryptedPassword = e;
    }

    public byte[] getSalt() {
        return salt;
    }

    public byte[] getPassword() {
        return encryptedPassword;
    }

}
