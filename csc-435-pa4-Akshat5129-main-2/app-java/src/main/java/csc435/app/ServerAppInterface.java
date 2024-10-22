package csc435.app;

import java.util.List;
import java.util.Scanner;

public class ServerAppInterface {
    private ServerProcessingEngine engine;

    public ServerAppInterface(ServerProcessingEngine engine) {
        this.engine = engine;
    }

    public void readCommands() {
        Scanner sc = new Scanner(System.in);
        String command;

        while (true) {
            System.out.print("> ");
            command = sc.nextLine(); 

            if (command.compareTo("quit") == 0) {
                engine.shutdown();
                break;
            } else if (command.compareTo("list") == 0) {
                List<String> clientInfo = engine.getConnectedClients();
                if (clientInfo != null) {
                    for (String info : clientInfo) {
                        System.out.println(info);
                    }
                }
            } else {
                System.out.println("unrecognized command!");
            }
        }

        sc.close();
    }
}