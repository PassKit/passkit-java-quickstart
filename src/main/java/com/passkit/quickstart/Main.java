package com.passkit.quickstart;

import java.util.Locale;

public final class Main {
    private Main() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 1 || "help".equalsIgnoreCase(args[0]) || "--help".equalsIgnoreCase(args[0])) {
            printUsage();
            return;
        }

        String workflow = args[0].toLowerCase(Locale.ROOT);
        System.out.printf("Running %s with a %d-channel gRPC pool.%n", workflow, AppConfig.load().poolSize());
        switch (workflow) {
            case "membership":
            case "loyalty":
                runLoyalty();
                break;
            case "coupons": runCoupons(); break;
            case "event-tickets":
            case "events": runEventTickets(); break;
            case "flights": runFlights(); break;
            default:
                System.err.println("Unknown workflow: " + args[0]);
                printUsage();
                System.exit(2);
        }
    }

    private static void runLoyalty() throws Exception {
        QuickstartLoyalty quickstart = new QuickstartLoyalty();
        quickstart.quickStart();
        if (shouldCleanup()) QuickstartLoyalty.cleanup(); else QuickstartLoyalty.close();
    }

    private static void runCoupons() throws Exception {
        QuickstartCoupons quickstart = new QuickstartCoupons();
        quickstart.quickStart();
        if (shouldCleanup()) QuickstartCoupons.cleanup(); else QuickstartCoupons.close();
    }

    private static void runEventTickets() throws Exception {
        QuickstartEventTickets quickstart = new QuickstartEventTickets();
        quickstart.quickStart();
        if (shouldCleanup()) QuickstartEventTickets.cleanup(); else QuickstartEventTickets.close();
    }

    private static void runFlights() throws Exception {
        QuickstartFlightTickets quickstart = new QuickstartFlightTickets();
        quickstart.quickStart();
        if (shouldCleanup()) QuickstartFlightTickets.cleanup(); else QuickstartFlightTickets.close();
    }

    private static boolean shouldCleanup() throws Exception {
        int seconds = AppConfig.load().cleanupDelaySeconds();
        if (seconds < 0) {
            System.out.println("Cleanup disabled; generated assets have been retained.");
            return false;
        }
        if (seconds > 0) {
            System.out.printf("Waiting %d seconds before cleanup...%n", seconds);
            Thread.sleep(seconds * 1000L);
        }
        System.out.println("Cleaning up generated assets...");
        return true;
    }

    private static void printUsage() {
        System.out.println("Usage: ./gradlew run --args='<membership|coupons|event-tickets|flights>'");
        System.out.println("Copy .env.example to .env and add your PassKit credentials first.");
    }
}
