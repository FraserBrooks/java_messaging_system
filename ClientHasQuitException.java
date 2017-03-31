
// New exception used on the server to allow 
// the client to quit from anywhere within the code
// Thrown by the getInput static method in the server
// class which all input is routed through.

public class ClientHasQuitException extends Exception {

    private static final long serialVersionUID = 1L;
    // Not really needed as we are not sending the object.
    // Just declared to keep eclipse happy

    public ClientHasQuitException(String message) {
        super(message);
    }

}
