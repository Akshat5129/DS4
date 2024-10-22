package csc435.app;

import java.util.List;

public class SearchResult {
    public double executionTime;
    public List<DocPathFreqPair> documentFrequencies;

    public SearchResult(double executionTime, List<DocPathFreqPair> documentFrequencies) {
        this.executionTime = executionTime;
        this.documentFrequencies = documentFrequencies;
    }
}