package csc435.app;

import java.util.List;
import java.util.Scanner;

public class ClientAppInterface {
    private ClientProcessingEngine engine;

    public ClientAppInterface(ClientProcessingEngine engine) {
        this.engine = engine;
    }

    public void readCommands() {
        Scanner sc = new Scanner(System.in);
        String command;

        while (true) {
            System.out.print("> ");

            // read from command line
            command = sc.nextLine();

            // if the command is quit, terminate the program
            if (command.compareTo("quit") == 0) {
                engine.disconnect();
                break;
            }

            // if the command begins with connect, connect to the given server
            if (command.length() >= 7 && command.substring(0, 7).compareTo("connect") == 0) {
                String[] parts = command.split(" ");
                if (parts.length != 3) {
                    System.out.println("Invalid connect command format. Usage: connect <hostname> <port>");
                    continue;
                }
                String hostname = parts[1];
                int port;
                try {
                    port = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid port number.");
                    continue;
                }
                engine.connect(hostname, port);
                continue;
            }

            // if the command begins with index, index the files from the specified directory
            if (command.length() >= 5 && command.substring(0, 5).compareTo("index") == 0) {
                String[] parts = command.split(" ");
                if (parts.length != 2) {
                    System.out.println("Invalid index command format. Usage: index <directory>");
                    continue;
                }
                String directory = parts[1];
                long startTime = System.currentTimeMillis();
                IndexResult indexResult = engine.indexFiles(directory); // Get IndexResult
                long endTime = System.currentTimeMillis();
                System.out.println("Indexing took " + (endTime - startTime) + " milliseconds");
                System.out.println("Total bytes read: " + indexResult.totalBytesRead); // Print total bytes read
                continue;
            }

            // if the command begins with search, search for files that matches the query
            if (command.length() >= 6 && command.substring(0, 6).compareTo("search") == 0) {
                String[] parts = command.split(" ");
                if (parts.length < 2) {
                    System.out.println("Invalid search command format. Usage: search <query>");
                    continue;
                }
                String query = String.join(" ", parts).substring(7); 
                long startTime = System.currentTimeMillis();
                SearchResult searchResult = engine.searchFiles(List.of(query.split(" ")));
                long endTime = System.currentTimeMillis();
                System.out.println("Searching took " + (endTime - startTime) + " milliseconds");
                
                if (searchResult.documentFrequencies != null) {
                    for (int i = 0; i < Math.min(searchResult.documentFrequencies.size(), 10); i++) {
                        System.out.println(searchResult.documentFrequencies.get(i).documentPath);
                    }
                }
                continue;
            }

            System.out.println("unrecognized command!");
        }

        sc.close();
    }
}