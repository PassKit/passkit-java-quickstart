package com.passkit.quickstart;

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class QuickstartCouponsTest {

        @Test
        public void quickstartCouponsTest() {
                // Include pool size if using connection pooling e.g. 5
                QuickstartCoupons qs = new QuickstartCoupons();
                qs.quickStart();

                // Test image assets
                assertNotEquals("expect icon image id not to be empty", "",
                                QuickstartCoupons.campaignImageIds.getIcon());
                assertNotEquals("expect logo image id not to be empty", "",
                                QuickstartCoupons.campaignImageIds.getLogo());
                assertNotEquals("expect appleLogo image id not to be empty", "",
                                QuickstartCoupons.campaignImageIds.getAppleLogo());
                assertNotEquals("expect strip image id not to be empty", "",
                                QuickstartCoupons.campaignImageIds.getStrip());
                assertNotEquals("expect hero image id not to be empty", "",
                                QuickstartCoupons.campaignImageIds.getHero());

                // Test templates have been created
                assertNotNull("expect baseTemplateId not to be null", QuickstartCoupons.baseTemplateId);
                assertEquals("length of base template id should be 22 characters", 22,
                                QuickstartCoupons.baseTemplateId.getId().length());
                assertNotNull("expect vipTemplateId not to be null", QuickstartCoupons.vipTemplateId);
                assertEquals("length of vip template id should be 22 characters", 22,
                                QuickstartCoupons.vipTemplateId.getId().length());

                // Test campaign has been created
                assertNotNull("expect campaignId not to be null", QuickstartCoupons.campaignId);
                assertEquals("length of program id should be 22 characters", 22,
                                QuickstartCoupons.campaignId.getId().length());

                // Test base offer and vip offer has been created
                assertNotNull("expect baseOfferId not to be null", QuickstartCoupons.baseOfferId);
                assertEquals("length of base offer id should be 22 characters", 22,
                                QuickstartCoupons.baseOfferId.getId().length());
                assertNotNull("expect vipOfferId not to be null", QuickstartCoupons.vipOfferId);
                assertEquals("length of vip offer id should be 22 characters", 22,
                                QuickstartCoupons.vipOfferId.getId().length());

                // Test base coupon and vip coupon has been created
                assertNotNull("expect baseCouponId not to be null", QuickstartCoupons.baseCouponId);
                assertEquals("length of base coupon id should be 22 characters", 22,
                                QuickstartCoupons.baseCouponId.getId().length());
                assertNotNull("expect vipCouponId not to be null", QuickstartCoupons.vipCouponId);
                assertEquals("length of vip coupon id should be 22 characters", 22,
                                QuickstartCoupons.vipCouponId.getId().length());

                // List URLs
                System.out.println(
                                "Base Coupon URL: " + "https://pub1.pskt.io/" + QuickstartCoupons.baseCouponId.getId());
                System.out.println(
                                "VIP Coupon URL: " + "https://pub1.pskt.io/" + QuickstartCoupons.vipCouponId.getId());

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
                QuickstartCoupons.cleanup();
        }
}
