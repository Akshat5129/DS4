package csc435.app;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexWorker implements Runnable {
    private IndexStore store;
    private Socket clientSocket;

    public IndexWorker(IndexStore store, Socket clientSocket) {
        this.store = store;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
        ) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String[] parts = inputLine.split(" ");
                String command = parts[0];

                if (command.equals("INDEX")) {
                    handleIndexRequest(parts, out);
                } else if (command.equals("SEARCH")) {
                    handleSearchRequest(parts, out);
                } else if (command.equals("QUIT")) {
                    break; // Client disconnected
                } else {
                    out.println("Invalid command.");
                }
            }
        } catch (IOException e) {
            System.err.println("Error in IndexWorker: " + e.getMessage());
        }
    }

    private void handleIndexRequest(String[] parts, PrintWriter out) {
        if (parts.length < 3) {
            out.println("Invalid INDEX request format.");
            return;
        }

        int clientId = Integer.parseInt(parts[1]); 
        String documentPath = parts[2];
        Map<String, Long> wordFrequencies = new HashMap<>();

        for (int i = 3; i < parts.length; i++) {
            String[] wordFreq = parts[i].split(":");
            if (wordFreq.length == 2) {
                wordFrequencies.put(wordFreq[0], Long.parseLong(wordFreq[1]));
            }
        }

        long documentNumber = store.putDocument(documentPath);
        store.updateIndex(documentNumber, wordFrequencies);

        out.println("INDEX_OK"); // Send acknowledgment 
    }

    private void handleSearchRequest(String[] parts, PrintWriter out) {
        if (parts.length < 2) {
            out.println("Invalid SEARCH request format.");
            return;
        }

        List<String> terms = new ArrayList<>();
        for (int i = 1; i < parts.length; i++) {
            terms.add(parts[i]);
        }

        List<DocFreqPair> searchResults = performSearch(terms);
        
        if(searchResults != null){
            for (DocFreqPair result : searchResults) {
                String documentPath = store.getDocument(result.documentNumber);
                out.println(documentPath + " " + result.wordFrequency);
            }
        }

        out.println("END"); // Indicate end of results
    }

    private List<DocFreqPair> performSearch(List<String> terms) {
        if (terms.isEmpty()) {
            return Collections.emptyList();
        }

        List<DocFreqPair> results = store.lookupIndex(terms.get(0));
        for (int i = 1; i < terms.size(); i++) {
            results = intersectPostingsLists(results, store.lookupIndex(terms.get(i)));
        }

        // Calculate combined frequencies and sort
        List<DocFreqPair> finalResults = new ArrayList<>();
        for (DocFreqPair result : results) {
            long combinedFrequency = calculateCombinedFrequency(result.documentNumber, terms);
            finalResults.add(new DocFreqPair(result.documentNumber, combinedFrequency));
        }

        Collections.sort(finalResults, (a, b) -> Long.compare(b.wordFrequency, a.wordFrequency)); // Sort by frequency (descending)

        return finalResults.subList(0, Math.min(finalResults.size(), 10)); // Return top 10
    }

    private List<DocFreqPair> intersectPostingsLists(List<DocFreqPair> list1, List<DocFreqPair> list2) {
        List<DocFreqPair> intersection = new ArrayList<>();
        int i = 0, j = 0;
        while (i < list1.size() && j < list2.size()) {
            if (list1.get(i).documentNumber == list2.get(j).documentNumber) {
                intersection.add(list1.get(i)); 
                i++;
                j++;
            } else if (list1.get(i).documentNumber < list2.get(j).documentNumber) {
                i++;
            } else {
                j++;
            }
        }
        return intersection;
    }

    private long calculateCombinedFrequency(long documentNumber, List<String> terms) {
        long combinedFrequency = 0;
        for (String term : terms) {
            List<DocFreqPair> postingsList = store.lookupIndex(term);
            for (DocFreqPair pair : postingsList) {
                if (pair.documentNumber == documentNumber) {
                    combinedFrequency += pair.wordFrequency;
                    break;
                }
            }
        }
        return combinedFrequency;
    }
}