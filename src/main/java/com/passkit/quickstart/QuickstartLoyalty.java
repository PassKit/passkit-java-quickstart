package com.passkit.quickstart;

import com.passkit.grpc.*;
import com.passkit.grpc.Members.*;

import io.grpc.ManagedChannel;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

/* Quickstart Loyalty runs through the high level steps required to create a Loyalty program from scratch, enrol a
   member and add some loyalty points using the PassKit gRPC Java SDK. You can skip to enrolling a member if you are
   using the GUI at https://app.passkit.com to design and configure the program.
 */
public class QuickstartLoyalty {

    public QuickstartLoyalty() {
        // initiate client stubs
        try {
            ManagedChannel channel = new GrpcConnection().getChannel();
            imagesStub = ImagesGrpc.newBlockingStub(channel);
            templatesStub = TemplatesGrpc.newBlockingStub(channel);
            membersStub = MembersGrpc.newBlockingStub(channel);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /* Stubs are used to access PassKit gRPC Functions. Blocking stubs can process both unary and streaming server
       responses, and therefore can be used with all current SDK methods. You are free to modify this implementation
       to add service, async or futures stubs for more efficient operations.
     */
    private static ImagesGrpc.ImagesBlockingStub imagesStub;
    private static MembersGrpc.MembersBlockingStub membersStub;
    private static TemplatesGrpc.TemplatesBlockingStub templatesStub;

    /* Quickstart will walk through the following steps:
       - Create image assets
       - Modify default template for base tier
       - Modify default template for VIP tier
       - Create base and VIP tiers
       - Create a loyalty program with base and VIP tiers
       - Enrol a member in each tier
       - Check-in a member
       - Check-out a member
       - Add loyalty points to a member
       - Delete all membership assets

       If you would like to retain the assets created, set delete.assets.timeout.seconds=-1 in the passkit.properties file.
     */

    // Public objects for testing purposes
    public static Image.ImageIds loyaltyImageIds;
    public static CommonObjects.Id baseTemplateId;
    public static CommonObjects.Id vipTemplateId;
    public static CommonObjects.Id programId;
    public static CommonObjects.Id baseTierId;
    public static CommonObjects.Id vipTierId;
    public static String vipShortCode;
    public static CommonObjects.Id memberId;
    public static CommonObjects.Id vipMemberId;
    public static MemberEventsOuterClass.MemberEvent checkInEvent;
    public static MemberEventsOuterClass.MemberEvent checkOutEvent;
    public static MemberOuterClass.MemberPoints memberPoints;
    public static Distribution.EnrolmentUrls enrolmentUrls;

    public void quickStart() {
        createImages();
        createTemplates();
        createProgram();
        createTiers();
        enrolMember();
        checkInMember();
        checkOutMember();
        addPoints(100);
        getDistribution();
    }

    private void createImages() {
        // Create the image assets we'll need for the pass design. A logo and icon are mandatory.
        String icon, logo, hero, strip;
        try {
            icon = Helpers.encodeFileToBase64("src/main/resources/images/shared/icon.png");
            logo = Helpers.encodeFileToBase64("src/main/resources/images/shared/logo.png");
            hero = Helpers.encodeFileToBase64("src/main/resources/images/loyalty/hero.png");
            strip = Helpers.encodeFileToBase64("src/main/resources/images/loyalty/strip.png");

            Image.CreateImageInput imageInput = Image.CreateImageInput.newBuilder()
                    .setImageData(Image.ImageData.newBuilder()
                            .setIcon(icon)
                            .setLogo(logo)
                            .setHero(hero)
                            .setStrip(strip)
                    ).build();

            loyaltyImageIds = imagesStub.createImages(imageInput);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createTemplates() {
        // Get the default template
        Template.DefaultTemplateRequest templateRequest = Template.DefaultTemplateRequest.newBuilder()
                .setProtocol(Protocols.PassProtocol.MEMBERSHIP)
                .setRevision(1)
                .build();
        Template.PassTemplate defaultTemplate = templatesStub.getDefaultTemplate(templateRequest);

        // Modify the default template for the base tier
        defaultTemplate = defaultTemplate.toBuilder()
                .setName("Quickstart Base Tier")
                .setDescription("Quickstart Base Tier Pass")
                .setImageIds(loyaltyImageIds)
                .setTimezone("Europe/London")
                .build();

        baseTemplateId = templatesStub.createTemplate(defaultTemplate);

        // Modify the default template for VIP tier
        defaultTemplate = defaultTemplate.toBuilder()
                .setName("Quickstart VIP Tier")
                .setDescription("Quickstart VIP Tier Pass")
                .setColors(Template.Colors.newBuilder()
                        .setBackgroundColor("#000000")
                        .setLabelColor("#FFFFFF")
                        .setTextColor("#FFFFFF")
                        .build()
                )
                .build();

        vipTemplateId = templatesStub.createTemplate(defaultTemplate);
    }

    private void createProgram() {
        // Create the loyalty program
        ProgramOuterClass.Program program = ProgramOuterClass.Program.newBuilder()
                .setName("Quickstart Loyalty Program")
                .addStatus(ProjectOuterClass.ProjectStatus.PROJECT_DRAFT)
                .addStatus(ProjectOuterClass.ProjectStatus.PROJECT_ACTIVE_FOR_OBJECT_CREATION)
                .setPointsType(ProgramOuterClass.PointsType.newBuilder().setBalanceType(ProgramOuterClass.BalanceType.BALANCE_TYPE_INT))
                .build();

        programId = membersStub.createProgram(program);
    }

    private void createTiers() {
        // Create the base and VIP tiers
        TierOuterClass.Tier tier = TierOuterClass.Tier.newBuilder()
                .setId("base")
                .setName("Quickstart Base Tier")
                .setTierIndex(1)
                .setPassTemplateId(baseTemplateId.getId())
                .setProgramId(programId.getId())
                .setTimezone("Europe/London")
                .build();

        baseTierId = membersStub.createTier(tier);

        tier = tier.toBuilder()
                .setId("vip")
                .setName("Quickstart VIP Tier")
                .setTierIndex(10)
                .setPassTemplateId(vipTemplateId.getId())
                // Set allow tier enrolment will allow direct enrolment to the tier via a public link
                .setAllowTierEnrolment(CommonObjects.PkBool.newBuilder().setOk(true).build())
                .build();

        vipTierId = membersStub.createTier(tier);

        TierOuterClass.Tier vipTier = membersStub.getTier(TierOuterClass.TierRequestInput.newBuilder()
                .setProgramId(programId.getId())
                .setTierId("vip")
                .build());

        vipShortCode = vipTier.getShortCode();
    }

    private void enrolMember() {

        MemberOuterClass.Member member = MemberOuterClass.Member.newBuilder()
                .setTierId("base")
                .setProgramId(programId.getId())
                .setPerson(Personal.Person.newBuilder()
                        .setDisplayName("Loyal Larry")
                        // set to an email address that can receive mail to receive an enrolment email.
                        .setEmailAddress("loyal.larry@dummy.passkit.com")
                        .build())
                .setPoints(88)
                .build();

        memberId = membersStub.enrolMember(member);

        member = member.toBuilder()
                .setTierId("vip")
                .setPoints(9999)
                .setPerson(Personal.Person.newBuilder()
                        .setDisplayName("Barry Big-boy")
                        // set to an email address that can receive mail to receive an enrolment email.
                        .setEmailAddress("barry.big-boy@dummy.passkit.com")
                        .build())
                .build();
        vipMemberId = membersStub.enrolMember(member);
    }

    private void checkInMember() {
        MemberOuterClass.MemberCheckInOutRequest request = MemberOuterClass.MemberCheckInOutRequest.newBuilder()
                .setMemberId(memberId.getId())
                .setLat(51.5014)
                .setLon(0.1419)
                .setAddress("Buckingham Palace, Westminster, London SW1A 1AA")
                .putMetaData("ticketType", "royalDayOut")
                .putMetaData("bookingReference", "4929910033527")
                .setExternalEventId("7253300199294")
                .build();

        checkInEvent = membersStub.checkInMember(request);
    }

    private void checkOutMember() {
        MemberOuterClass.MemberCheckInOutRequest request = MemberOuterClass.MemberCheckInOutRequest.newBuilder()
                .setMemberId(memberId.getId())
                .setLat(51.5014)
                .setLon(0.1419)
                .setAddress("Buckingham Palace, Westminster, London SW1A 1AA")
                .putMetaData("ticketType", "royalDayOut")
                .putMetaData("bookingReference", "4929910033527")
                .putMetaData("corgisSeen", "6")
                .putMetaData("visitorSatisfactionRating", "9")
                .setExternalEventId("7253300199492")
                .build();

        checkOutEvent = membersStub.checkOutMember(request);
    }

    private void addPoints(float points) {
        MemberOuterClass.EarnBurnPointsRequest request = MemberOuterClass.EarnBurnPointsRequest.newBuilder()
                .setId(memberId.getId())
                .setPoints(points)
                .build();

        memberPoints = membersStub.earnPoints(request);
    }

    public Iterator<MemberEventsOuterClass.MemberEvent> listMemberEvents() {
        MemberOuterClass.ListRequest request = MemberOuterClass.ListRequest.newBuilder()
                .setProgramId(programId.getId())
                .build();
        return membersStub.listMemberEvents(request);
    }

    private void getDistribution() {
        enrolmentUrls = membersStub.getProgramEnrolment(programId);
    }

    public static void cleanup() {
        membersStub.deleteProgram(programId);
        templatesStub.deleteTemplate(baseTemplateId);
        templatesStub.deleteTemplate(vipTemplateId);
        imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(loyaltyImageIds.getIcon()).build());
        imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(loyaltyImageIds.getLogo()).build());
        imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(loyaltyImageIds.getAppleLogo()).build());
        imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(loyaltyImageIds.getStrip()).build());
        imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(loyaltyImageIds.getHero()).build());
    }
}