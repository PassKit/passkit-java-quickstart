package com.passkit.quickstart;

import com.passkit.grpc.Members.MemberEventsOuterClass;

import org.junit.Test;

import java.util.Iterator;
import java.util.Properties;

import static org.junit.Assert.*;

public class QuickstartLoyaltyTest {

    @Test
    public void quickstartLoyaltyTest() {
        QuickstartLoyalty qs = new QuickstartLoyalty();
        qs.quickStart();

        // Test image assets
        assertNotEquals("expect icon image id not to be empty", "", QuickstartLoyalty.loyaltyImageIds.getIcon());
        assertNotEquals("expect logo image id not to be empty","", QuickstartLoyalty.loyaltyImageIds.getLogo());
        assertNotEquals("expect appleLogo image id not to be empty","", QuickstartLoyalty.loyaltyImageIds.getAppleLogo());
        assertNotEquals("expect strip image id not to be empty","", QuickstartLoyalty.loyaltyImageIds.getStrip());
        assertNotEquals("expect hero image id not to be empty","", QuickstartLoyalty.loyaltyImageIds.getHero());
        // Expect all other image ids to be empty
        assertEquals("expect security image id to be empty","", QuickstartLoyalty.loyaltyImageIds.getThumbnail());

        // Test templates have been created
        assertNotNull("expect baseTemplateId not to be null", QuickstartLoyalty.baseTemplateId);
        assertNotNull("expect vipTemplateId not to be null", QuickstartLoyalty.vipTemplateId);
        assertEquals("length of template id should be 22 characters", 22, QuickstartLoyalty.baseTemplateId.getId().length());
        assertEquals("length of template id should be 22 characters", 22, QuickstartLoyalty.vipTemplateId.getId().length());

        // Test program has been created
        assertNotNull("expect programId not to be null", QuickstartLoyalty.programId);
        assertEquals("length of program id should be 22 characters", 22, QuickstartLoyalty.programId.getId().length());

        // Test tiers have been created
        assertNotNull("expect baseTierId not to be null", QuickstartLoyalty.baseTierId);
        assertEquals("id of base tier should be 'base'", "base", QuickstartLoyalty.baseTierId.getId());
        assertNotNull("expect baseTierId not to be null", QuickstartLoyalty.vipTierId);
        assertEquals("id of base tier should be 'vip'", "vip", QuickstartLoyalty.vipTierId.getId());

        // Test member has been created
        assertNotNull("expect memberId not to be null", QuickstartLoyalty.memberId);
        assertEquals("length of program id should be 22 characters", 22, QuickstartLoyalty.memberId.getId().length());

        // Test checkin event has been created
        assertNotNull("expect checkInEvent not to be null", QuickstartLoyalty.checkInEvent);
        assertEquals("length of checkInEvent id should be 22 characters", 22, QuickstartLoyalty.checkInEvent.getId().length());
        assertEquals("check checkout event member id equals member id", QuickstartLoyalty.memberId.getId(), QuickstartLoyalty.checkInEvent.getMember().getId());
        assertEquals("check checkout latitude", 51.5014, QuickstartLoyalty.checkInEvent.getLat(), 0);
        assertEquals("check checkout longitude", 0.1419, QuickstartLoyalty.checkInEvent.getLon(), 0);
        assertEquals("check checkout address", "Buckingham Palace, Westminster, London SW1A 1AA", QuickstartLoyalty.checkInEvent.getAddress());
        assertEquals("check checkout externalId", "7253300199294", QuickstartLoyalty.checkInEvent.getExternalId());
        java.util.Map<String, String> meta = QuickstartLoyalty.checkInEvent.getMetaDataMap();
        assertEquals("check checkout meta booking reference", "4929910033527", meta.get("bookingReference"));
        assertEquals("check checkout meta ticket type", "royalDayOut", meta.get("ticketType"));

        // Test checkout event has been created
        assertNotNull("expect checkOutEvent not to be null", QuickstartLoyalty.checkOutEvent);
        assertEquals("length of checkInEvent id should be 22 characters", 22, QuickstartLoyalty.checkOutEvent.getId().length());
        assertEquals("check checkout event member id equals member id", QuickstartLoyalty.memberId.getId(), QuickstartLoyalty.checkInEvent.getMember().getId());
        assertEquals("check checkout latitude", 51.5014, QuickstartLoyalty.checkOutEvent.getLat(), 0);
        assertEquals("check checkout longitude", 0.1419, QuickstartLoyalty.checkOutEvent.getLon(), 0);
        assertEquals("check checkout address", "Buckingham Palace, Westminster, London SW1A 1AA", QuickstartLoyalty.checkOutEvent.getAddress());
        assertEquals("check checkout externalId", "7253300199492", QuickstartLoyalty.checkOutEvent.getExternalId());
        meta = QuickstartLoyalty.checkOutEvent.getMetaDataMap();
        assertEquals("check checkout meta booking reference", "4929910033527", meta.get("bookingReference"));
        assertEquals("check checkout meta ticket type", "royalDayOut", meta.get("ticketType"));
        assertEquals("check checkout meta corgis seen", "6", meta.get("corgisSeen"));
        assertEquals("check checkout meta corgis seen", "9", meta.get("visitorSatisfactionRating"));

        // Test Member Earn Points
        assertNotNull("expect memberPoints not to be null", QuickstartLoyalty.memberPoints);
        assertEquals("check member points balance", 188,QuickstartLoyalty.memberPoints.getPoints(),0);

        // Test List Events
        Iterator<MemberEventsOuterClass.MemberEvent> events = qs.listMemberEvents();
        int eventRecord = 1;
        while(events.hasNext()) {
            MemberEventsOuterClass.MemberEvent event = events.next();
            switch (eventRecord) {
                case 1:
                    assertEquals("check first listed event matches checked in event", QuickstartLoyalty.checkInEvent, event);
                    break;
                case 2:
                    assertEquals("check second listed event matches checked out event", QuickstartLoyalty.checkOutEvent, event);
                    break;
                case 3:
                    assertEquals("check third listed event is a points earned event", MemberEventsOuterClass.MemberEvents.EVENT_MEMBER_POINTS_EARNED, event.getEventType());
                    meta = event.getMetaDataMap();
                    assertEquals("check points change value", "100.00", meta.get("points-change-value"));
                    assertEquals("check points before change", "88.00", meta.get("points-before-change"));
                    assertEquals("check points after change", "188.00", meta.get("points-after-change"));
                    break;
                default:
                    assertNull("if there is an unexpected 4th object, this will trigger", event);
                    break;
            }
            eventRecord++;
        }

        // List URLs
        System.out.println("Enrolment URL: " + QuickstartLoyalty.enrolmentUrls.getPageUrl());
        System.out.println("Enrolment QR Code URL: " + QuickstartLoyalty.enrolmentUrls.getQrCodeUrl());
        System.out.println("VIP Direct Enrolment URL: " + QuickstartLoyalty.enrolmentUrls.getTierEnrolmentUrlsMap().get(QuickstartLoyalty.vipTierId.getId()));
        System.out.println("VIP Direct Enrolment QR Code URL: " + QuickstartLoyalty.enrolmentUrls.getTierEnrolmentQRsMap().get(QuickstartLoyalty.vipTierId.getId()));
        System.out.println("Pass URL: " + "https://" + QuickstartLoyalty.enrolmentUrls.getPageUrl().split("/")[2] + "/" + QuickstartLoyalty.memberId.getId());
        System.out.println("VIP Pass URL: " + "https://" + QuickstartLoyalty.enrolmentUrls.getPageUrl().split("/")[2] + "/" + QuickstartLoyalty.vipMemberId.getId());

        try {
            Properties properties = new Properties();
            properties.load(GrpcConnection.class.getResourceAsStream("/passkit.properties"));
            String pref = properties.getProperty("delete.assets.timeout.seconds", "0");
            int timeout = Integer.parseInt(pref);
            if (timeout == -1) {
                System.exit(0);
            }
            System.out.println("Test execution paused for " + pref + " seconds to check URLs");
            Thread.sleep(timeout * 1000);
            System.out.println("Testing resumed. Deleting all test assets...");
        } catch (Exception e) {
            e.printStackTrace();
        }
        QuickstartLoyalty.cleanup();
    }
}
