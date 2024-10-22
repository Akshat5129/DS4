package csc435.app;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FileRetrievalBenchmark {
    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: FileRetrievalBenchmark <serverIP> <serverPort> <numClients> <datasetPath1> [<datasetPath2> ...]");
            return;
        }

        String serverIP = args[0];
        int serverPort = Integer.parseInt(args[1]);
        int numClients = Integer.parseInt(args[2]);
        List<String> datasetPaths = new ArrayList<>();
        for (int i = 3; i < args.length; i++) {
            datasetPaths.add(args[i]);
        }

        long startTime = System.currentTimeMillis();

        ExecutorService benchmarkPool = Executors.newFixedThreadPool(numClients);
        List<BenchmarkWorker> workers = new ArrayList<>();

        for (int i = 0; i < numClients; i++) {
            String datasetPath = datasetPaths.get(i % datasetPaths.size()); // Cycle through dataset paths if needed
            BenchmarkWorker worker = new BenchmarkWorker(serverIP, String.valueOf(serverPort), datasetPath);
            workers.add(worker);
            benchmarkPool.execute(worker);
        }

        benchmarkPool.shutdown();
        try {
            benchmarkPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS); 
        } catch (InterruptedException e) {
            System.err.println("Benchmark interrupted: " + e.getMessage());
        }

        long endTime = System.currentTimeMillis();
        double totalTime = (endTime - startTime) / 1000.0; 

        System.out.println("Total indexing time: " + totalTime + " seconds");

        // Perform search queries on the first client (optional)
        if (!workers.isEmpty()) {
            BenchmarkWorker firstWorker = workers.get(0);
            // ... (add code to perform search queries on firstWorker)
        }

        // Disconnect all clients
        for (BenchmarkWorker worker : workers) {
            worker.disconnect();
        }
    }
}