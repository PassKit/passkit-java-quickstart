package com.passkit.quickstart;

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class QuickstartFlightTicketsTest {

        @Test
        public void quickstartFlightTicketsTest() {
                QuickstartFlightTickets qs = new QuickstartFlightTickets();
                qs.quickStart();

                // Test image assets
                assertNotEquals("expect icon image id not to be empty", "",
                                QuickstartFlightTickets.flightImageIds.getIcon());
                assertNotEquals("expect logo image id not to be empty", "",
                                QuickstartFlightTickets.flightImageIds.getLogo());
                assertNotEquals("expect appleLogo image id not to be empty", "",
                                QuickstartFlightTickets.flightImageIds.getAppleLogo());
                assertNotEquals("expect strip image id not to be empty", "",
                                QuickstartFlightTickets.flightImageIds.getEventStrip());
                assertNotEquals("expect hero image id not to be empty", "",
                                QuickstartFlightTickets.flightImageIds.getHero());
                assertNotEquals("expect thumbnail image id not to be empty", "",
                                QuickstartFlightTickets.flightImageIds.getThumbnail());
                assertNotEquals("expect background image id not to be empty", "",
                                QuickstartFlightTickets.flightImageIds.getBackground());
                // Expect all other image ids to be empty
                assertEquals("expect security image id to be empty", "",
                                QuickstartFlightTickets.flightImageIds.getStrip());

                // Test templates have been created
                assertNotNull("expect baseTemplateId not to be null", QuickstartFlightTickets.baseTemplateId);
                assertNotNull("expect vipTemplateId not to be null", QuickstartFlightTickets.vipTemplateId);
                assertEquals("length of template id should be 22 characters", 22,
                                QuickstartFlightTickets.baseTemplateId.getId().length());
                assertEquals("length of template id should be 22 characters", 22,
                                QuickstartFlightTickets.vipTemplateId.getId().length());

                // List URLs
                // System.out.println("Base Pass URL: " +
                // QuickstartEventTickets.basePass.getUrl());
                // System.out.println(

                try {
                        Properties properties = new Properties();
                        properties.load(GrpcConnection.class.getResourceAsStream("/passkit.properties"));
                        String pref = properties.getProperty("delete.assets.timeout.seconds", "0");
                        int timeout = Integer.parseInt(pref);
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
