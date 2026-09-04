package com.passkit.quickstart;

import org.junit.Test;
import org.junit.Assume;


import static org.junit.Assert.*;

public class QuickstartFlightTicketsTest {

        @Test
        public void quickstartFlightTicketsTest() {
                Assume.assumeTrue("Set PASSKIT_RUN_LIVE_TESTS=true to run API tests",
                                "true".equalsIgnoreCase(System.getenv("PASSKIT_RUN_LIVE_TESTS")));
                // Include pool size if using connection pooling e.g. 5
                QuickstartFlightTickets qs = new QuickstartFlightTickets();
                qs.quickStart();

                // Test image assets
                assertNotEquals("expect icon image id not to be empty", "",
                                QuickstartFlightTickets.flightImageIds.getIcon());
                assertNotEquals("expect logo image id not to be empty", "",
                                QuickstartFlightTickets.flightImageIds.getLogo());
                assertNotEquals("expect appleLogo image id not to be empty", "",
                                QuickstartFlightTickets.flightImageIds.getAppleLogo());
                // Expect all other image ids to be empty
                assertEquals("expect security image id to be empty", "",
                                QuickstartFlightTickets.flightImageIds.getStrip());

                // Test templates have been created
                assertNotNull("expect templateId not to be null", QuickstartFlightTickets.templateId);
                assertEquals("length of template id should be 22 characters", 22,
                                QuickstartFlightTickets.templateId.getId().length());

                // Test ticket has been created
                assertNotNull("expect base ticket not to be null", QuickstartFlightTickets.pass);

                // List URLs
                System.out.println(
                                "URLS: " + QuickstartFlightTickets.pass);

                try {
                        int timeout = AppConfig.load().cleanupDelaySeconds();
                        String pref = Integer.toString(timeout);
                        if (timeout == -1) {
                                System.exit(0);
                        }
                        System.out.println("Test execution paused for " + pref + " seconds to check URLs");
                        Thread.sleep(timeout * 1000L);
                        System.out.println("Testing resumed. Deleting all test assets...");
                } catch (Exception e) {
                        e.printStackTrace();
                }
                QuickstartFlightTickets.cleanup();
        }
}
