package csc435.app;

public class FileRetrievalServer {
    public static void main(String[] args) {
        int serverPort = 2020; // TODO: Change to a non-privileged port (e.g., > 1024)

        if (args.length > 0) {
            try {
                serverPort = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default port " + serverPort);
            }
        }

        IndexStore store = new IndexStore();
        ServerProcessingEngine engine = new ServerProcessingEngine(store);
        ServerAppInterface appInterface = new ServerAppInterface(engine);

        engine.initialize(serverPort); 
        appInterface.readCommands(); 
    }
}