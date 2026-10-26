package ro.ubm.sd.client;

public class Client {
    public static void main(String[] args) {
        String coordinator = "localhost:7001";

        // Parcurgem argumentele pentru a prelua coordonatorul transmis din linia de comandă
        for (int i = 0; i < args.length; i++) {
            if ("-coordinator".equals(args[i])) {
                coordinator = args[++i];
            }
        }

        // Afișăm configurația la pornire conform cerinței
        System.out.printf("[client] coordonator configurat: %s; comenzi: put, get, ls, rm, nodes, exit%n", coordinator);
    }
}