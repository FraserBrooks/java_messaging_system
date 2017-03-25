
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.*;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordService {
	
	public static boolean authenticate(String attemptedPassword, 
			byte[] encryptedPassword, 
			byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException{
		
		byte[] encryptedAttempt = encrypt(attemptedPassword, salt);
		return Arrays.equals(encryptedPassword, encryptedAttempt);
	}
	
	public static byte[] encrypt(String password, byte[] salt)
	throws NoSuchAlgorithmException, InvalidKeySpecException{
		String algorithm = "PBKDF2WithHmacSHA1";
		int keyLength = 160;
		int iterations = 1000;
		
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
		
		SecretKeyFactory f = SecretKeyFactory.getInstance(algorithm);
		
		return f.generateSecret(spec).getEncoded();
	}
	
	public static byte[] generateSalt() throws NoSuchAlgorithmException{
		
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		
		byte[] salt = new byte[8];
		random.nextBytes(salt);
		
		return salt;
	}

}
