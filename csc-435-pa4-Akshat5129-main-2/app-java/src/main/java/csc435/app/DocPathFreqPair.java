package csc435.app;

public class DocPathFreqPair implements Comparable<DocPathFreqPair> {
    public String documentPath;
    public long wordFrequency;

    public DocPathFreqPair(String documentPath, long wordFrequency) {
        this.documentPath = documentPath;
        this.wordFrequency = wordFrequency;
    }

    @Override
    public int compareTo(DocPathFreqPair other) {
        // Sort in descending order of wordFrequency
        return Long.compare(other.wordFrequency, this.wordFrequency); 
    }
}