package com.passkit.quickstart;

import com.passkit.grpc.Members.MemberEventsOuterClass;
import org.junit.Test;

import java.util.Iterator;
import java.util.Properties;

import static org.junit.Assert.*;

public class QuickstartEventTicketsTest {

    @Test
    public void quickstartEventTicketsTest() {
        QuickstartEventTickets qs = new QuickstartEventTickets();
        qs.quickStart();

        // Test image assets
        assertNotEquals("expect icon image id not to be empty", "", QuickstartEventTickets.eventImageIds.getIcon());
        assertNotEquals("expect logo image id not to be empty", "", QuickstartEventTickets.eventImageIds.getLogo());
        assertNotEquals("expect appleLogo image id not to be empty", "", QuickstartEventTickets.eventImageIds.getAppleLogo());
        assertNotEquals("expect strip image id not to be empty", "", QuickstartEventTickets.eventImageIds.getEventStrip());
        assertNotEquals("expect hero image id not to be empty", "", QuickstartEventTickets.eventImageIds.getHero());
        assertNotEquals("expect thumbnail image id not to be empty", "", QuickstartEventTickets.eventImageIds.getThumbnail());
        assertNotEquals("expect background image id not to be empty", "", QuickstartEventTickets.eventImageIds.getBackground());
        // Expect all other image ids to be empty
        assertEquals("expect security image id to be empty", "", QuickstartEventTickets.eventImageIds.getStrip());

        // Test templates have been created
        assertNotNull("expect baseTemplateId not to be null", QuickstartEventTickets.baseTemplateId);
        assertNotNull("expect vipTemplateId not to be null", QuickstartEventTickets.vipTemplateId);
        assertEquals("length of template id should be 22 characters", 22, QuickstartEventTickets.baseTemplateId.getId().length());
        assertEquals("length of template id should be 22 characters", 22, QuickstartEventTickets.vipTemplateId.getId().length());

        // Test program has been created
        assertNotNull("expect venueId not to be null", QuickstartEventTickets.venueId);
        assertEquals("length of venue id should be 22 characters", 22, QuickstartEventTickets.venueId.getId().length());

        // Test ticket types have been created
        assertNotNull("expect ticket type not to be null", QuickstartEventTickets.baseTicketTypeId);
        assertEquals("length of template id should be 22 characters", 22, QuickstartEventTickets.baseTicketTypeId.getId().length());
        assertNotNull("expect ticket type not to be null", QuickstartEventTickets.vipTicketTypeId);
        assertEquals("length of template id should be 22 characters", 22, QuickstartEventTickets.vipTicketTypeId.getId().length());

        // Test regular ticket has been created
        assertNotNull("expect base ticket not to be null", QuickstartEventTickets.baseTicket);
        assertEquals("length of program id should be 22 characters", 22, QuickstartEventTickets.baseTicket.getId().length());
        assertNotNull("expect base pass not to be null", QuickstartEventTickets.basePass);
        // Test vip ticket has been created
        assertNotNull("expect base ticket not to be null", QuickstartEventTickets.vipTicket);
        assertEquals("length of program id should be 22 characters", 22, QuickstartEventTickets.vipTicket.getId().length());
        assertNotNull("expect vip pass not to be null", QuickstartEventTickets.vipPass);

        // List URLs
        System.out.println("Base Pass URL: " + QuickstartEventTickets.basePass);
        System.out.println("Base Pass Multi-pass URL: " + QuickstartEventTickets.basePass.getMultiplePassesURL());
        System.out.println("Vip Pass URL: " + QuickstartEventTickets.vipPass.getUrl());
        System.out.println("Vip Pass Multi-pass URL: " + QuickstartEventTickets.vipPass.getMultiplePassesURL());

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
        QuickstartEventTickets.cleanup();
    }
}
