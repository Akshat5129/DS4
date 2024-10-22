package csc435.app;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerProcessingEngine {
    private IndexStore store;
    private Dispatcher dispatcher;
    private ExecutorService workerPool; 
    private List<String> connectedClients;

    public ServerProcessingEngine(IndexStore store) {
        this.store = store;
        this.workerPool = Executors.newCachedThreadPool(); // Create a thread pool for worker threads
        this.connectedClients = new ArrayList<>();
    }

    public void initialize(int serverPort) {
        dispatcher = new Dispatcher(this, serverPort);
        new Thread(dispatcher).start(); // Start the Dispatcher thread
    }

    public void spawnWorker(Socket clientSocket) {
        IndexWorker worker = new IndexWorker(store, clientSocket);
        workerPool.execute(worker); // Execute the worker in the thread pool

        // Add client information to the list (you might need to get IP and port from clientSocket)
        String clientInfo = "Client connected: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
        connectedClients.add(clientInfo); 
    }

    public void shutdown() {
        dispatcher.shutdown(); // You'll need to add a shutdown method to Dispatcher
        workerPool.shutdown(); 
        // ... (handle closing client connections, etc.)
    }

    public List<String> getConnectedClients() {
        return new ArrayList<>(connectedClients); // Return a copy of the list for thread safety
    }
}