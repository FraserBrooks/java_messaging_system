
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

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
	
	ServerAuthenticator(BufferedReader f, PrintStream to, ClientTable t, PasswordTable p) {
		fromClient = f;
		toClient = to;
		clientTable = t;
		passwordTable = p;
	}

	public void run() {
		String user = null;
		try {
			toClient.println("Welcome to the messaging service. \n");
			while (!Thread.interrupted()) {
				toClient.println(commands);
				firstInput = Server.getInput(fromClient.readLine());
				switch (firstInput.toLowerCase()) {
				case "login":
					user = login();
					if (user != null) {
						finish(user);
						return;
					}
					break;
				case "register":
					user = register();
					if (user != null) {
						finish(user);
						return;
					}
					break;
				case "quit":
					toClient.println("Exiting...");
					Report.behaviour("Client quiting before logging in");
					return;
				case "help":
					toClient.println(commands);
					break;
				default:
					toClient.println("Unrecognised Input. Try Again.");
					break;
				}

			}
		} catch (IOException e) {
			Report.error("Something went wrong with the ServerAuthenticator: IOException: " + e.getMessage());
			// No point in trying to close sockets. Just give up.
			// We end this thread (we don't do System.exit(1)).
		} catch (NoSuchAlgorithmException e) {
			Report.error(
					"Something went wrong with the ServerAuthenticator: NoSuchAlgorithmException: " + e.getMessage());
		} catch (InvalidKeySpecException e) {
			Report.error(
					"Something went wrong with the ServerAuthenticator: InvalidKeySpecException: " + e.getMessage());
		} catch (ClientHasQuitException e) {
			// Client has decided to quit. Close streams and exit.
			Report.behaviour("Client quiting before logging in");
		}
		// Attempt to close streams
		toClient.close();
		try {
			fromClient.close();
		} catch (IOException e1) {
			// Nothing more to do
		}
	}

	private String login()
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, ClientHasQuitException {
		String username;
		String givenPassword;

		toClient.println("Username: ");
		username = Server.getInput(fromClient.readLine());

		PasswordEntry correct = passwordTable.getPasswordEntry(username);
		if (correct == null) {
			toClient.println("No such username. Please try again or register.");
			return null;
		}

		toClient.println("Password: ");
		givenPassword = Server.getInput(fromClient.readLine());

		byte[] encryptedP = correct.getPassword();
		byte[] salt = correct.getSalt();
		if (!PasswordService.authenticate(givenPassword, encryptedP, salt)) {
			toClient.println("Incorrect Password. Please try again or register.");
			return null;
		}

		// User authorised, return name of authorised user
		return username;
	}

	private String register()
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, ClientHasQuitException {
		String username;
		String desiredPassword;

		toClient.println("Desired username: ");
		username = Server.getInput(fromClient.readLine());

		if (username.length() < Server.min_username_length) {
			toClient.println("Username too short. Please try again.");
			return null;
		}
		if (passwordTable.isInTable(username)) {
			toClient.println("Username already taken. Please try again.");
			return null;
		}

		toClient.println("Desired password: ");
		desiredPassword = Server.getInput(fromClient.readLine());

		if (desiredPassword.length() < Server.min_password_length) {
			toClient.println(
					"Invalid password. Password must be at least " + Server.min_password_length + " chars long.");
			return null;
		}

		toClient.println("Please confirm password: ");

		if (desiredPassword.equals(Server.getInput(fromClient.readLine()))) {
			byte[] salt = PasswordService.generateSalt();
			byte[] encryptedPassword = PasswordService.encrypt(desiredPassword, salt);
			passwordTable.add(username, new PasswordEntry(salt, encryptedPassword));
			toClient.println("Account successfully created.");
			return username;
		}
		toClient.println("Passwords did not match. Please try again.");
		return null;
	}

	private void finish(String user) {
		String newClient = user;
		clientTable.add(newClient);

		ServerSender serverSend = new ServerSender(clientTable.getQueue(newClient), toClient);
		serverSend.start();

		// We create and start a new thread to read from the client:
		(new ServerReceiver(newClient, fromClient, toClient, clientTable, passwordTable, serverSend)).start();

	}

}
