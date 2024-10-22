package csc435.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

class DocFreqPair {
    public long documentNumber;
    public long wordFrequency;

    public DocFreqPair(long documentNumber, long wordFrequency) {
        this.documentNumber = documentNumber;
        this.wordFrequency = wordFrequency;
    }
}

public class IndexStore {
    private Map<String, Long> documentMap; // Document path to document number
    private Map<String, List<DocFreqPair>> termInvertedIndex; // Term to list of (document number, frequency) pairs
    private long nextDocumentNumber; 

    private ReentrantLock documentMapLock;
    private ReentrantLock termInvertedIndexLock; 

    public IndexStore() {
        documentMap = new HashMap<>();
        termInvertedIndex = new HashMap<>();
        nextDocumentNumber = 1; // Start document numbers from 1

        documentMapLock = new ReentrantLock();
        termInvertedIndexLock = new ReentrantLock();
    }

    public long putDocument(String documentPath) {
        documentMapLock.lock(); 
        try {
            if (!documentMap.containsKey(documentPath)) {
                documentMap.put(documentPath, nextDocumentNumber);
                nextDocumentNumber++;
            }
            return documentMap.get(documentPath);
        } finally {
            documentMapLock.unlock(); 
        }
    }

    public String getDocument(long documentNumber) {
        documentMapLock.lock();
        try {
            for (Map.Entry<String, Long> entry : documentMap.entrySet()) {
                if (entry.getValue() == documentNumber) {
                    return entry.getKey();
                }
            }
            return null; // Document not found
        } finally {
            documentMapLock.unlock();
        }
    }

    public void updateIndex(long documentNumber, Map<String, Long> wordFrequencies) {
        termInvertedIndexLock.lock();
        try {
            for (Map.Entry<String, Long> entry : wordFrequencies.entrySet()) {
                String term = entry.getKey();
                long frequency = entry.getValue();

                List<DocFreqPair> postingsList = termInvertedIndex.getOrDefault(term, new ArrayList<>());
                postingsList.add(new DocFreqPair(documentNumber, frequency));
                termInvertedIndex.put(term, postingsList);
            }
        } finally {
            termInvertedIndexLock.unlock();
        }
    }

    public List<DocFreqPair> lookupIndex(String term) {
        termInvertedIndexLock.lock();
        try {
            return new ArrayList<>(termInvertedIndex.getOrDefault(term, new ArrayList<>())); 
        } finally {
            termInvertedIndexLock.unlock();
        }
    }
}