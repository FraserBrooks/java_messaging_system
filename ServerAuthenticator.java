
import java.io.*;

public class ServerAuthenticator extends Thread{
	
	private BufferedReader fromClient;
	private PrintStream toClient;
	private ClientTable clientTable;
	private PasswordTable passwordTable;
	private String firstInput = null;
	private String commands = 
			"Commands:\n"
			+ "     login \n"
			+ "     register \n"
			+ "     help \n"
			+ "     quit \n\n";
	
	
	ServerAuthenticator(BufferedReader f, PrintStream to, ClientTable t, PasswordTable p){
		fromClient = f;
		toClient = to;
		clientTable = t;
		passwordTable = p;
	}
	
	public void run(){
		try{
			toClient.println("Server: You have successfully connected to the message server.");
			toClient.println(commands);
			while(true){
				firstInput = fromClient.readLine();
				switch(firstInput.toLowerCase()){
					case "login": 
						login();
						break;
					case "regster": 
						register();
						break;
					case "quit": 
						toClient.println("Exiting...");
						Report.behaviour("User quiting before logging in");
						break;
					case "help":
						toClient.println(commands);
						break;
					default:
						toClient.println("Unrecognised Input. Try Again.");
						break;
				}
			}
		}
	}
	
	private boolean login(){
		String username;
		String givenPassword;
		
		try{
			toClient.println("Username: ");
			username = fromClient.readLine();
			PasswordEntry correct = passwordTable.getPasswordEntry(username);
			if(correct.equals(null)){
				toClient.println("No such username. Please try again or register.");
				return false;
			}
			
			toClient.println("Password: ");
			givenPassword = fromClient.readLine();
		}
		
	}
	
	private boolean register(){
		
	}
	

}
