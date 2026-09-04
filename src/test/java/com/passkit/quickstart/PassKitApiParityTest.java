package com.passkit.quickstart;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

/** Locks the curated Java facade to the public method surface used by the other quickstarts. */
public class PassKitApiParityTest {
    private static final Map<String, String[]> METHODS = Map.ofEntries(
            Map.entry("loyalty", names(
                    "listPrograms listTiers listMembers listMemberEvents getMessageHistoryForMember " +
                    "getMetaKeysForProgram getMemberEventMetaKeysForProgram createProgram getProgram updateProgram " +
                    "deleteProgram createTier getTier updateTier deleteTier enrolMember getMemberRecordById " +
                    "getMemberRecordByExternalId updateMember patchPerson deleteMember countMembers changeMemberTier " +
                    "earnPoints burnPoints setPoints updateMemberExpiry renewMembersExpiry countMemberEvents " +
                    "checkInMember checkOutMember deleteMemberEvent deleteEventsForMember getProgramEnrolment")),
            Map.entry("coupons", names(
                    "listCouponCampaigns listCouponOffers listCouponsByCouponCampaign streamCouponUpdates " +
                    "streamCouponRedemptions getMetaKeysForCampaign createCouponCampaign getCouponCampaign " +
                    "updateCouponCampaign deleteCouponCampaign createCouponOffer getCouponOffer updateCouponOffer " +
                    "deleteCouponOffer createCoupon getCouponById getCouponByExternalId updateCoupon " +
                    "updateCouponExternalId patchPerson redeemCoupon voidCoupon countCouponsByCouponCampaign getAnalytics")),
            Map.entry("eventTickets", names(
                    "listProductions listVenues listEvents listTicketTypes listTickets createProduction getProduction " +
                    "updateProduction patchProduction deleteProduction createVenue getVenueById updateVenue patchVenue " +
                    "deleteVenue createEvent getEventById getEventByStartDateAndVenue updateEvent patchEvent deleteEvent " +
                    "createTicketType getTicketTypeById getTicketTypeByUserDefinedId updateTicketType patchTicketType " +
                    "deleteTicketType issueTicket issueTicketById getTicketById getTicketByTicketNumber " +
                    "getTicketsByOrderNumber getEventTicketPass updateTicket patchPerson validateTicket redeemTicket " +
                    "redeemTicketsByOrderNumber deleteTicket deleteTicketsByOrderNumber countTickets getAnalytics")),
            Map.entry("flights", names(
                    "createCarrier getCarrier updateCarrier deleteCarrier createPort getPort updatePort deletePort " +
                    "createFlightDesignator getFlightDesignator updateFlightDesignator deleteFlightDesignator " +
                    "createFlight getFlight updateFlight deleteFlight createBoardingPass getBoardingPass " +
                    "getBoardingPassRecord updateBoardingPass deleteBoardingPass")),
            Map.entry("templates", names(
                    "listTemplates listLocations listBeacons listLinks createTemplate getTemplate getDefaultTemplate " +
                    "updateTemplate copyTemplate deleteTemplate countTemplates createLocation getLocation updateLocation " +
                    "copyLocation deleteLocation countLocations createBeacon getBeacon updateBeacon copyBeacon " +
                    "deleteBeacon countBeacons createLink getLink updateLink copyLink deleteLink countLinks")),
            Map.entry("images", names(
                    "listImages createImages getImageBundle getImageData getImageURL getLocalizedImageURL updateImage " +
                    "deleteImage deleteLocalizedImage countImages getProfileImage getProfileImageById setProfileImage " +
                    "getStampImageConfig updateStampImageConfig getStampImagePreview getStampImageURL")),
            Map.entry("analytics", names("getAnalytics")),
            Map.entry("distribution", names(
                    "getSmartPassLink getDataCollectionPageFields validateBarcode sendWelcomeEmail addMessage getMessage " +
                    "getMessages updateMessage cancelMessage")),
            Map.entry("integrations", names(
                    "listSinkSubscriptions createSinkSubscription getSinkSubscription updateSinkSubscription " +
                    "deleteSinkSubscription getSampleSubscriptionEvent")),
            Map.entry("scanners", names("getScannerConfig createScannerConfig updateScannerConfig")),
            Map.entry("certificates", names("listAppleCertificates getAppleCertificateData countAppleCertificates")),
            Map.entry("raw", names(
                    "streamPassUpdates listPassesByPassProject listPassesByPassTemplate createPassProject " +
                    "getPassProject updatePassProject copyPassProject deletePassProject createPass getPassById " +
                    "getPassByExternalId updatePass deletePass"))
    );

    @Test
    public void exposesEveryCuratedQuickstartMethod() throws Exception {
        for (Map.Entry<String, String[]> group : METHODS.entrySet()) {
            Method accessor = PassKitApi.class.getMethod(group.getKey());
            Set<String> available = Arrays.stream(accessor.getReturnType().getMethods())
                    .map(Method::getName).collect(Collectors.toSet());
            for (String expected : group.getValue()) {
                assertTrue(group.getKey() + " is missing " + expected, available.contains(expected));
            }
        }
    }

    @Test
    public void exposesTheSameAdvancedBulkMethods() throws Exception {
        assertMethods(PassKitApi.Advanced.class.getMethod("loyalty").getReturnType(),
                names("batchUpdate bulkDeleteMembers updateMembersBySegment deleteMembersBySegment copyProgram"));
        assertMethods(PassKitApi.Advanced.class.getMethod("coupons").getReturnType(),
                names("bulkVoidCoupons copyCouponCampaign"));
        assertMethods(PassKitApi.Advanced.class.getMethod("eventTickets").getReturnType(),
                names("bulkDeleteTickets copyProduction"));
    }

    private static void assertMethods(Class<?> type, String[] expected) {
        Set<String> available = Arrays.stream(type.getMethods()).map(Method::getName).collect(Collectors.toSet());
        for (String method : expected) assertTrue("Missing advanced method " + method, available.contains(method));
    }

    private static String[] names(String value) { return value.trim().split("\\s+"); }
}
