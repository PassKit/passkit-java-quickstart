package com.passkit.quickstart;

import com.passkit.grpc.*;
import com.passkit.grpc.Members.*;
import com.passkit.grpc.Members.MemberOuterClass.Member;
import com.passkit.grpc.Members.MemberOuterClass.MemberRecordByExternalIdRequest;
import com.passkit.grpc.Members.ProgramOuterClass.Program;
import com.passkit.grpc.Raw.Project;
import com.passkit.grpc.Distribution;

import java.io.IOException;
import java.util.Iterator;

/* Quickstart Loyalty runs through the high level steps required to create a Loyalty program from scratch, enrol a
   member and add some loyalty points using the PassKit gRPC Java SDK. You can skip to enrolling a member if you are
   using the GUI at https://app.passkit.com to design and configure the program.
 */
public class QuickstartLoyalty {

        // Regular connection
        private static GrpcConnection conn;

        // Connection for pooling
        /**
         * private static GrpcConnectionPool connectionPool;
         * 
         * // Quickstart set up for pool connections
         * public QuickstartLoyalty(int poolSize) {
         * try {
         * // Initialize the gRPC connection pool with the specified pool size
         * connectionPool = new GrpcConnectionPool(poolSize);
         * 
         * // Initialize stubs using channels from the pool
         * imagesStub = ImagesGrpc.newBlockingStub(connectionPool.getChannel());
         * templatesStub = TemplatesGrpc.newBlockingStub(connectionPool.getChannel());
         * membersStub = MembersGrpc.newBlockingStub(connectionPool.getChannel());
         * distributionStub =
         * DistributionGrpc.newBlockingStub(connectionPool.getChannel());
         * } catch (IOException e) {
         * e.printStackTrace();
         * shutdownPool();
         * System.exit(1);
         * }
         * }
         **/

        public QuickstartLoyalty() {
                // initiate client stubs
                try {
                        conn = new GrpcConnection();
                        imagesStub = ImagesGrpc.newBlockingStub(conn.getChannel());
                        templatesStub = TemplatesGrpc.newBlockingStub(conn.getChannel());
                        distributionStub = DistributionGrpc.newBlockingStub(conn.getChannel());
                        membersStub = MembersGrpc.newBlockingStub(conn.getChannel());
                        usersStub = UsersGrpc.newBlockingStub(conn.getChannel());
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
        private static MembersGrpc.MembersBlockingStub membersStub;
        private static TemplatesGrpc.TemplatesBlockingStub templatesStub;
        private static DistributionGrpc.DistributionBlockingStub distributionStub;
        private static UsersGrpc.UsersBlockingStub usersStub;

        /*
         * Quickstart will walk through the following steps:
         * - Create image assets
         * - Modify default template for base tier
         * - Modify default template for VIP tier
         * - Create base and VIP tiers
         * - Create a loyalty program with base and VIP tiers
         * - Enrol a member in each tier
         * - Check-in a member
         * - Check-out a member
         * - Add loyalty points to a member
         * - Delete all membership assets
         * 
         * Each method has the minimum information needed to execute the method, if you
         * would like to add more details please refer to
         * https://docs.passkit.io/protocols/member/
         * for fields that can be added.
         * If you would like to retain the assets created, set
         * delete.assets.timeout.seconds=-1 in the passkit.properties file.
         */

        // Public objects for testing purposes
        public static Image.ImageIds loyaltyImageIds;
        public static CommonObjects.Id baseTemplateId;
        public static CommonObjects.Id vipTemplateId;
        public static CommonObjects.Id programId;
        public static com.passkit.grpc.ProjectOuterClass.Project project;
        public static CommonObjects.Id baseTierId;
        public static CommonObjects.Id vipTierId;
        public static String vipShortCode;
        public static CommonObjects.Id memberId;
        public static CommonObjects.Id vipMemberId;
        public static MemberEventsOuterClass.MemberEvent checkInEvent;
        public static MemberEventsOuterClass.MemberEvent checkOutEvent;
        public static MemberOuterClass.MemberPoints memberPoints;
        public static Distribution.EnrolmentUrls enrolmentUrls;
        public static CommonObjects.Url smartPassUrl;
        public static String baseEmail = "loyal.larry@dummy.passkit.com"; // Change to your email to receive cards
        public static String vipEmail = "harry.highroller@dummy.passkit.com"; // Change to your email to receive cards
        public static Member externalId;

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
                getMemberByExternalId();
                listMemberEvents();
                getSmartPassLink();
        }

        private void createImages() {
                // Create the image assets we'll need for the pass design. A logo and icon are
                // mandatory.
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
                                                        .setStrip(strip))
                                        .build();

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
                                                .build())
                                .build();

                vipTemplateId = templatesStub.createTemplate(defaultTemplate);
        }

        private void createProgram() {
                // Create the loyalty program
                ProgramOuterClass.Program program = ProgramOuterClass.Program.newBuilder()
                                .setName("Quickstart Loyalty Program")
                                .addStatus(ProjectOuterClass.ProjectStatus.PROJECT_DRAFT)
                                .addStatus(ProjectOuterClass.ProjectStatus.PROJECT_ACTIVE_FOR_OBJECT_CREATION)
                                .setPointsType(ProgramOuterClass.PointsType.newBuilder()
                                                .setBalanceType(ProgramOuterClass.BalanceType.BALANCE_TYPE_INT))
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
                                .setName(" VIP Tier")
                                .setTierIndex(10)
                                .setPassTemplateId(vipTemplateId.getId())
                                // Set allow tier enrolment will allow direct enrolment to the tier via a public
                                // link
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
                                .setExternalId("12345")
                                .setPerson(Personal.Person.newBuilder()
                                                .setDisplayName("Loyal Larry")
                                                // set to an email address that can receive mail to receive an enrolment
                                                // email.

                                                .setEmailAddress(baseEmail)
                                                .build())
                                .setPoints(88)
                                .build();

                memberId = membersStub.enrolMember(member);
                member = member.toBuilder()
                                .setTierId("vip")
                                .setPoints(9999)
                                .setExternalId("123456")
                                .setPerson(Personal.Person.newBuilder()
                                                .setDisplayName("Harry Highroller")
                                                // set to an email address that can receive mail to receive an enrolment
                                                // email.
                                                .setEmailAddress(vipEmail)
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
                                .setTierPoints(0)
                                .setSecondaryPoints(points)
                                .build();

                memberPoints = membersStub.earnPoints(request);
        }

        public Iterator<MemberEventsOuterClass.MemberEvent> listMemberEvents() {
                MemberOuterClass.ListRequest request = MemberOuterClass.ListRequest.newBuilder()
                                .setProgramId(programId.getId())
                                .build();
                return membersStub.listMemberEvents(request);
        }

        private void getMemberByExternalId() {
                MemberRecordByExternalIdRequest request = MemberRecordByExternalIdRequest.newBuilder()
                                .setExternalId("12345")
                                .setProgramId(programId.getId())
                                .build();

                externalId = membersStub.getMemberRecordByExternalId(request);
        }

        private void getSmartPassLink() {

                project = usersStub.getProjectByUuid(programId);
                String url = "https://pub1.pskt.io/c/" + project.getShortCode();

                CommonObjects.Url projectDistributionUrl = CommonObjects.Url.newBuilder()
                                .setUrl(url) // This is the PassKit Url found in the
                                                                         // settings section of the project under smart
                                                                         // pass links and then the tab command line
                                                                         // tools
                                .build();
                Distribution.SmartPassLinkRequest smartPassLink = Distribution.SmartPassLinkRequest.newBuilder()
                                .setProjectDistributionUrl(projectDistributionUrl)
                                .putFields("person.surname", "Anderson") // More possible fields can be found here:
                                                                         // https://github.com/PassKit/smart-pass-link-from-csv-generator#available-field-names
                                .putFields("person.emailAddress", baseEmail)
                                .build();
                smartPassUrl = distributionStub.getSmartPassLink(smartPassLink);
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

                // always close the channel when there will be no further calls made.
                conn.closeChannel();

                // Shutdown if you are using the connection pool
                // shutdownPool();
        }

        // Method to shut down the pool
        /**
         * private static void shutdownPool() {
         * if (connectionPool != null) {
         * connectionPool.shutdown();
         * }
         * }
         **/
}