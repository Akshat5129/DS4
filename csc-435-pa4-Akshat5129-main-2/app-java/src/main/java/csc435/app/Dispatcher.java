package csc435.app;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Dispatcher implements Runnable {
    private ServerProcessingEngine engine;
    private int serverPort; 
    private ServerSocket serverSocket; // Add this field

    public Dispatcher(ServerProcessingEngine engine, int serverPort) {
        this.engine = engine;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(serverPort); // Initialize serverSocket here
            System.out.println("Server listening on port " + serverPort);

            while (true) {
                Socket clientSocket = serverSocket.accept(); 
                engine.spawnWorker(clientSocket); 
            }
        } catch (IOException e) {
            System.err.println("Error in Dispatcher: " + e.getMessage());
        }
    }

    public void shutdown() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); // Close the server socket
                System.out.println("Server socket closed.");
            }
        } catch (IOException e) {
            System.err.println("Error shutting down Dispatcher: " + e.getMessage());
        }
    }
}