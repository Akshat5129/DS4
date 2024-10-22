package csc435.app;

public class BenchmarkWorker implements Runnable {
    private ClientProcessingEngine engine;
    private String serverIP;
    private int serverPort;
    private String datasetPath;

    public BenchmarkWorker(String serverIP, String serverPort, String datasetPath) {
        this.engine = new ClientProcessingEngine();
        this.serverIP = serverIP;
        this.serverPort = Integer.parseInt(serverPort); // Convert serverPort to int
        this.datasetPath = datasetPath;
    }

    @Override
    public void run() {
        engine.connect(serverIP, serverPort);
        IndexResult indexResult = engine.indexFiles(datasetPath);

        System.out.println("Client indexed " + datasetPath + 
                           " in " + indexResult.executionTime + " seconds, " +
                           "total bytes read: " + indexResult.totalBytesRead);
    }

    public void search() {
        // ... (Add code to perform search queries using engine.searchFiles() if needed)
    }

    public void disconnect() {
        engine.disconnect();
    }
}