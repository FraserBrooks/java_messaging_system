import java.io.*;


// Repeatedly reads clients input and sends to the server
// interrupted by ClientReceiver when client quits 

public class ClientSender extends Thread {

	private PrintStream server;
	private ClientReceiver linkedReceiver;

	ClientSender(PrintStream p, ClientReceiver c) {
		server = p;
		linkedReceiver = c;
	}

	public void run() {
		// So that we can use the method readLine:
		BufferedReader user = new BufferedReader(new InputStreamReader(System.in));

		try {
			// Then loop forever sending messages to recipients via the server:
			while (true) {
				String message = user.readLine();
				if (message != null) {
					server.println(message);
					if (message.toLowerCase().equals("quit")) {
						linkedReceiver.interrupt();
						break;
					}
				}
			}
		} catch (IOException e) {
			Report.errorAndGiveUp("Communication broke in ClientSender" + e.getMessage());
		} finally {
			server.close();
		}

	}
}
