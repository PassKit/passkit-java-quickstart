package com.passkit.quickstart;

import com.passkit.grpc.*;
import com.passkit.grpc.SingleUseCoupons.*;
import com.passkit.grpc.SingleUseCoupons.Campaign;
import com.passkit.grpc.SingleUseCoupons.CouponOuterClass.CouponStatus;
import com.google.protobuf.Timestamp;

import java.io.IOException;

/* Quickstart Coupons runs through the high level steps required to create a Campaign from scratch, create a coupon and create an offer using the PassKit gRPC Java SDK. 
 */
public class QuickstartCoupons {

        private static GrpcConnection conn;

        public QuickstartCoupons() {
                // initiate client stubs
                try {
                        conn = new GrpcConnection();
                        imagesStub = ImagesGrpc.newBlockingStub(conn.getChannel());
                        templatesStub = TemplatesGrpc.newBlockingStub(conn.getChannel());
                        couponsStub = SingleUseCouponsGrpc.newBlockingStub(conn.getChannel());
                } catch (Exception e) {
                        e.printStackTrace();
                        conn.closeChannel();
                        System.exit(1);
                }
        }

        /*
         * Stubs are used to access PassKit gRPC Functions. Blocking stubs can process
         * both unary and streaming server
         * responses, and therefore can be used with all current SDK methods. You are
         * free to modify this implementation
         * to add service, async or futures stubs for more efficient operations.
         */
        private static ImagesGrpc.ImagesBlockingStub imagesStub;
        private static SingleUseCouponsGrpc.SingleUseCouponsBlockingStub couponsStub;
        private static TemplatesGrpc.TemplatesBlockingStub templatesStub;

        /*
         * Quickstart will walk through the following steps:
         * - Create image assets
         * - Create a campaign
         * - Modify a campaign
         * - Create an offer
         * - Enrol customer on offer
         * - Redeem a coupon
         * - Delete an offer
         * 
         * If you would like to retain the assets created, set
         * delete.assets.timeout.seconds=-1 in the passkit.properties file.
         */

        // Public objects for testing purposes
        public static Image.ImageIds campaignImageIds;
        public static CommonObjects.Id baseTemplateId;
        public static CommonObjects.Id vipTemplateId;
        public static CommonObjects.Id campaignId;
        public static CommonObjects.Id baseOfferId;
        public static CommonObjects.Id vipOfferId;
        public static CommonObjects.Id baseCouponId;
        public static CommonObjects.Id vipCouponId;

        public void quickStart() {
                createImages();
                createTemplate();
                createCampaign();
                createOffer();
                createCoupon();
                redeemCoupon(); // Optional
                listCoupons(); // Optional
        }

        private void createImages() {
                // Create the image assets we'll need for the pass design. A logo and icon are
                // mandatory.
                String logo, hero, strip, icon;
                try {
                        icon = Helpers.encodeFileToBase64("src/main/resources/images/eventTickets/icon.png");
                        logo = Helpers.encodeFileToBase64("src/main/resources/images/shared/logo.png");
                        hero = Helpers.encodeFileToBase64("src/main/resources/images/loyalty/hero.png");
                        strip = Helpers.encodeFileToBase64("src/main/resources/images/loyalty/strip.png");

                        Image.CreateImageInput imageInput = Image.CreateImageInput.newBuilder()
                                        .setImageData(Image.ImageData.newBuilder()
                                                        .setIcon(icon)
                                                        .setLogo(logo)
                                                        .setHero(hero)
                                                        .setStrip(strip))
                                        .build();

                        campaignImageIds = imagesStub.createImages(imageInput);
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        private void createTemplate() {
                // Get the default template
                Template.DefaultTemplateRequest templateRequest = Template.DefaultTemplateRequest.newBuilder()
                                .setProtocol(Protocols.PassProtocol.SINGLE_USE_COUPON)
                                .setRevision(1)
                                .build();
                Template.PassTemplate defaultTemplate = templatesStub.getDefaultTemplate(templateRequest);

                // Modify the default template for the base offer
                defaultTemplate = defaultTemplate.toBuilder()
                                .setName("Quickstart Before Redeem Campaign")
                                .setDescription("Quickstart Unredeemed Offer Pass")
                                .setImageIds(campaignImageIds)
                                .setTimezone("Europe/London")
                                .build();

                baseTemplateId = templatesStub.createTemplate(defaultTemplate);

                // Modify the vip template for the vip offer
                defaultTemplate = defaultTemplate.toBuilder()
                                .setName("Quickstart VIP Before Redeem Campaign")
                                .setDescription("Quickstart VIP Unredeemed Offer Pass")
                                .setColors(Template.Colors.newBuilder()
                                                .setBackgroundColor("#000000")
                                                .setLabelColor("#FFFFFF")
                                                .setTextColor("#FFFFFF")
                                                .build())
                                .build();
                vipTemplateId = templatesStub.createTemplate(defaultTemplate);

        }

        private void createCampaign() {
                // Create the campaign
                Campaign.CouponCampaign campaign = Campaign.CouponCampaign.newBuilder()
                                .setName("Quickstart Campaign")
                                .addStatus(ProjectOuterClass.ProjectStatus.PROJECT_DRAFT)
                                .addStatus(ProjectOuterClass.ProjectStatus.PROJECT_ACTIVE_FOR_OBJECT_CREATION)
                                .build();
                campaignId = couponsStub.createCouponCampaign(campaign);
        }

        private void createOffer() {
                // Create the base and vip offer
                Offer.CouponOffer offer = Offer.CouponOffer.newBuilder()
                                .setId("Base offer")
                                .setOfferTitle("Base offer")
                                .setOfferShortTitle("Base offer")
                                .setOfferDetails("Base Offer details")
                                .setOfferFinePrint("Base Offer fine print")
                                .setBeforeRedeemPassTemplateId(baseTemplateId.getId())
                                .setIssueStartDate(Timestamp.newBuilder()
                                                .setSeconds(System.currentTimeMillis() / 1000L)
                                                .build())
                                .setIssueEndDate(Timestamp.newBuilder()
                                                .setSeconds(System.currentTimeMillis() / 1000L + 360000)
                                                .build())
                                .setRedemptionSettings(Offer.RedemptionSettings.newBuilder()
                                                .setRedemptionStartDate(Timestamp.newBuilder()
                                                                .setSeconds(System.currentTimeMillis() / 1000L)
                                                                .build())
                                                .setRedemptionEndDate(Timestamp.newBuilder()
                                                                .setSeconds(System.currentTimeMillis() / 1000L + 360000)
                                                                .build()))
                                .setCouponExpirySettings(Offer.CouponExpirySettings.newBuilder()
                                                .setCouponExpiryType(
                                                                Offer.CouponExpiryType.AUTO_EXPIRE_REDEMPTION_END_DATE)
                                                .build())
                                .setCampaignId(campaignId.getId())
                                .setIanaTimezone("Europe/London")
                                .build();
                baseOfferId = couponsStub.createCouponOffer(offer);

                offer = offer.toBuilder()
                                .setId("VIP offer")
                                .setOfferTitle("VIP offer")
                                .setOfferShortTitle("VIP offer")
                                .setOfferDetails("VIP Offer details")
                                .setOfferFinePrint("VIP Offer fine print")
                                .setBeforeRedeemPassTemplateId(vipTemplateId.getId())
                                .build();
                vipOfferId = couponsStub.createCouponOffer(offer);
        }

        private void createCoupon() {
                // Enrols with a coupon
                CouponOuterClass.Coupon coupon = CouponOuterClass.Coupon.newBuilder()
                                .setOfferId(baseOfferId.getId())
                                .setCampaignId(campaignId.getId())
                                .setSku("sfsdg")
                                .setPerson(Personal.Person.newBuilder()
                                                .setDisplayName("Loyal Larry")
                                                // set to an email address that can receive mail to receive an enrolment
                                                // email.
                                                .setEmailAddress("loyal.larry@dummy.passkit.com")
                                                .build())
                                .setStatus(CouponStatus.UNREDEEMED)
                                .build();
                baseCouponId = couponsStub.createCoupon(coupon);
                coupon = coupon.toBuilder()
                                .setOfferId(vipOfferId.getId())
                                .setPerson(Personal.Person.newBuilder()
                                                .setDisplayName("Harry Highroller")
                                                // set to an email address that can receive mail to receive an enrolment
                                                // email.
                                                .setEmailAddress("harry.highroller@dummy.passkit.com")
                                                .build())
                                .build();
                vipCouponId = couponsStub.createCoupon(coupon);

        }

        private void redeemCoupon() {
                // Redeem base coupon, if redeemed pass url will no longer be valid

                CouponOuterClass.Coupon baseRequest = CouponOuterClass.Coupon.newBuilder()
                                .setId(baseCouponId.getId())
                                .setCampaignId(campaignId.getId())
                                .build();
                couponsStub.redeemCoupon(baseRequest);

                // Redeem vip coupon, if redeemed pass url will no longer be valid
                CouponOuterClass.Coupon vipRequest = CouponOuterClass.Coupon.newBuilder()
                                .setId(vipCouponId.getId())
                                .setCampaignId(campaignId.getId())
                                .build();
                couponsStub.redeemCoupon(vipRequest);
        }

        private void listCoupons() {
                // Lists any coupons under the selected campaign
                CouponOuterClass.ListRequest listRequest = CouponOuterClass.ListRequest.newBuilder()
                                .setCouponCampaignId(campaignId.getId())
                                .build();
                couponsStub.listCouponsByCouponCampaign(listRequest);

        }

        public static void cleanup() {
                couponsStub.deleteCouponCampaign(campaignId);
                templatesStub.deleteTemplate(baseTemplateId);
                templatesStub.deleteTemplate(vipTemplateId);
                imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(campaignImageIds.getIcon()).build());
                imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(campaignImageIds.getLogo()).build());
                imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(campaignImageIds.getAppleLogo()).build());
                imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(campaignImageIds.getStrip()).build());
                imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(campaignImageIds.getHero()).build());

                // always close the channel when there will be no further calls made.
                conn.closeChannel();
        }
}