package ro.ubm.sd.node;

import java.io.File;

public class StorageNode {
    public static void main(String[] args) {
        String id = "unknown";
        int port = 7000;
        String dirPath = "data/default";
        boolean isCoordinator = false;

        // Parcurgem argumentele primite din linia de comandă
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-id":
                    id = args[++i];
                    break;
                case "-port":
                    port = Integer.parseInt(args[++i]);
                    break;
                case "-dir":
                    dirPath = args[++i];
                    break;
                case "--coordinator":
                    isCoordinator = true;
                    break;
            }
        }

        // Creăm directorul local de stocare dacă nu există
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Afișăm configurația la pornire conform cerinței
        String role = isCoordinator ? "coordonator si nod de stocare" : "nod de stocare";
        System.out.printf("[%s] pornit: port %d, director %s, rol: %s%n", id, port, dirPath, role);
    }
}