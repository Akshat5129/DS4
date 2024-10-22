package csc435.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientProcessingEngine {
    private Socket socket; 
    private PrintWriter out;
    private BufferedReader in;
    private int clientId; // You might need to assign a unique client ID

    public ClientProcessingEngine() {
        clientId = 1; // Assign a default client ID (you might need a better way to manage this)
    }

    public IndexResult indexFiles(String folderPath) {
        IndexResult result = new IndexResult(0.0, 0);
        long startTime = System.currentTimeMillis();

        try {
            File folder = new File(folderPath);
            if (!folder.isDirectory()) {
                System.err.println("Error: Invalid folder path.");
                return result;
            }

            File[] files = folder.listFiles();
            if (files == null) {
                System.err.println("Error: No files found in the folder.");
                return result;
            }

            for (File file : files) {
                if (file.isFile()) {
                    result.totalBytesRead += file.length();
                    processFile(file);
                }
            }
        } catch (IOException e) {
            System.err.println("Error indexing files: " + e.getMessage());
        }

        long endTime = System.currentTimeMillis();
        result.executionTime = (endTime - startTime) / 1000.0; // Execution time in seconds

        return result;
    }

    private void processFile(File file) throws IOException {
        Map<String, Long> wordFrequencies = new HashMap<>();
        Pattern pattern = Pattern.compile("[a-zA-Z0-9]{3,}"); // At least 3 alphanumeric characters

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) {
                    String word = matcher.group().toLowerCase();
                    wordFrequencies.put(word, wordFrequencies.getOrDefault(word, 0L) + 1);
                }
            }
        }

        sendIndexRequest(file.getAbsolutePath(), wordFrequencies);
    }

    private void sendIndexRequest(String filePath, Map<String, Long> wordFrequencies) {
        // Construct the INDEX REQUEST message
        String message = "INDEX " + clientId + " " + filePath; 
        for (Map.Entry<String, Long> entry : wordFrequencies.entrySet()) {
            message += " " + entry.getKey() + ":" + entry.getValue();
        }
        
        out.println(message);

        // You might need to handle the INDEX REPLY here
        // ... (e.g., read the reply from the server)
    }

    public SearchResult searchFiles(List<String> terms) {
        SearchResult result = new SearchResult(0.0, new ArrayList<>());
        long startTime = System.currentTimeMillis();

        // Construct the SEARCH REQUEST message
        String message = "SEARCH " + String.join(" ", terms); 
        out.println(message);


        try {
            String replyLine;
            while ((replyLine = in.readLine()) != null) {
                if(replyLine.equals("END")){
                    break;
                }

                String[] parts = replyLine.split(" ");
                if (parts.length >= 2) {
                    String documentPath = parts[0];
                    long wordFrequency = Long.parseLong(parts[1]);
                    result.documentFrequencies.add(new DocPathFreqPair(documentPath, wordFrequency));
                }
            }
        } catch (IOException e) {
            System.err.println("Error receiving search results: " + e.getMessage());
        }


        result.documentFrequencies.sort(null); // Sort using the compareTo() method in DocPathFreqPair

        long endTime = System.currentTimeMillis();
        result.executionTime = (endTime - startTime) / 1000.0;

        return result;
    }

    public void connect(String serverIP, int serverPort) {
        try {
            socket = new Socket(serverIP, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Connected to server: " + serverIP + ":" + serverPort);
        } catch (UnknownHostException e) {
            System.err.println("Error: Unknown host " + serverIP);
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (socket != null) {
                out.println("QUIT"); // Send a QUIT message to the server
                socket.close();
                System.out.println("Disconnected from server.");
            }
        } catch (IOException e) {
            System.err.println("Error disconnecting from server: " + e.getMessage());
        }
    }
}