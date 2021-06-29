package com.passkit.quickstart;

import com.passkit.grpc.*;
import com.passkit.grpc.EventTickets.*;

import com.google.protobuf.Timestamp;

import java.io.IOException;
import java.time.*;
import java.util.ArrayList;


public class QuickstartEventTickets {

    private static GrpcConnection conn;

    public QuickstartEventTickets() {
        // initiate client stubs
        try {
            conn = new GrpcConnection();
            imagesStub = ImagesGrpc.newBlockingStub(conn.getChannel());
            templatesStub = TemplatesGrpc.newBlockingStub(conn.getChannel());
            eventsStub = EventTicketsGrpc.newBlockingStub(conn.getChannel());
        } catch (Exception e) {
            e.printStackTrace();
            conn.closeChannel();
            System.exit(1);
        }
    }

    /* Stubs are used to access PassKit gRPC Functions. Blocking stubs can process both unary and streaming server
       responses, and therefore can be used with all current SDK methods. You are free to modify this implementation
       to add service, async or futures stubs for more efficient operations.
     */
    private static ImagesGrpc.ImagesBlockingStub imagesStub;
    private static EventTicketsGrpc.EventTicketsBlockingStub eventsStub;
    private static TemplatesGrpc.TemplatesBlockingStub templatesStub;

    /* Quickstart will walk through the following steps:
       - Create image assets
       - Modify default template for a regular event ticket
       - Modify default template for a vip event ticket
       - Create a venue
       - Create a production
       - Create a basic ticket type
       - Create a VIP ticket type
       - Issue a basic ticket (auto create an event)
       - Issue a VIP ticket
       - Validate tickets
       - Redeem tickets
       - Delete all ticket assets

       If you would like to retain the assets created, set delete.assets.timeout.seconds=-1 in the passkit.properties file.
     */

    // Public objects for testing purposes
    public static Image.ImageIds eventImageIds;
    public static CommonObjects.Id baseTemplateId;
    public static CommonObjects.Id vipTemplateId;
    public static CommonObjects.Id venueId;
    public static CommonObjects.Id productionId;
    public static CommonObjects.Id baseTicketTypeId;
    public static CommonObjects.Id vipTicketTypeId;
    public static CommonObjects.Id baseTicket;
    public static CommonObjects.Id vipTicket;
    public static CommonObjects.PassBundle basePass;
    public static CommonObjects.PassBundle vipPass;
    public static TicketOuterClass.ValidateTicketResponse baseTicketValidation;
    public static TicketOuterClass.ValidateTicketResponse vipTicketValidation;
    public static CommonObjects.Id baseTicketRedeemedId;
    public static CommonObjects.Id vipTicketRedeemedId;
    public static long eventTimeUnix;

    public void quickStart() {
        createImages();
        createTemplates();
        createVenue();
        createProduction();
        createTicketTypes();
        createTickets();
        validateTickets();
        redeemTickets();
    }

    private void createImages() {
        // Create the image assets we'll need for the pass designs. A logo and icon are mandatory.
        String icon, logo, appleLogo, background, thumbnail, hero, eventStrip;
        try {
            icon = Helpers.encodeFileToBase64("src/main/resources/images/eventTickets/icon.png");
            logo = Helpers.encodeFileToBase64("src/main/resources/images/eventTickets/logo.png");
            appleLogo = Helpers.encodeFileToBase64("src/main/resources/images/eventTickets/appleLogo.png");
            background = Helpers.encodeFileToBase64("src/main/resources/images/eventTickets/background.png");
            thumbnail = Helpers.encodeFileToBase64("src/main/resources/images/eventTickets/thumbnail.png");
            hero = Helpers.encodeFileToBase64("src/main/resources/images/eventTickets/hero.png");
            eventStrip = Helpers.encodeFileToBase64("src/main/resources/images/eventTickets/strip.png");

            Image.CreateImageInput imageInput = Image.CreateImageInput.newBuilder()
                    .setImageData(Image.ImageData.newBuilder()
                            .setIcon(icon)
                            .setLogo(logo)
                            .setAppleLogo(appleLogo)
                            .setBackground(background)
                            .setThumbnail(thumbnail)
                            .setHero(hero)
                            .setEventStrip(eventStrip)
                    ).build();

            eventImageIds = imagesStub.createImages(imageInput);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createTemplates() {
        // Get the default template - revision 1 is a template that contains comprehensive fields
        Template.DefaultTemplateRequest templateRequest = Template.DefaultTemplateRequest.newBuilder()
                .setProtocol(Protocols.PassProtocol.EVENT_TICKETING)
                .setRevision(1)
                .build();
        Template.PassTemplate defaultTemplate = templatesStub.getDefaultTemplate(templateRequest);

        // Modify the default template for the base tier
        defaultTemplate = defaultTemplate.toBuilder()
                .setName("BK5 Ticket")
                .setDescription("The Bangkok Five - Category B Ticket")
                .setImageIds(Image.ImageIds.newBuilder()
                        .setIcon(eventImageIds.getIcon())
                        .setLogo(eventImageIds.getLogo())
                        .setAppleLogo(eventImageIds.getAppleLogo())
                        .setEventStrip(eventImageIds.getEventStrip())
                        .setHero(eventImageIds.getHero())
                        .build())
                .setColors(Template.Colors.newBuilder()
                        .setTextColor("000000")
                        .setLabelColor("000000")
                        .setStripColor("000000")
                        .setBackgroundColor("FFEA6C")
                        .build())
                .setTimezone("Asia/Bangkok")
                .build();

        baseTemplateId = templatesStub.createTemplate(defaultTemplate);

        ArrayList<Template.DataField> dataFields = new ArrayList<>(defaultTemplate.getData().getDataFieldsList());
        Template.DataField firstField = dataFields.get(0);
        firstField = firstField.toBuilder()
                .setAppleWalletFieldRenderOptions(firstField.getAppleWalletFieldRenderOptions().toBuilder()
                .setPositionSettings(Template.PositionSettings.newBuilder()
                        .setSection(Template.FieldSection.PRIMARY_FIELDS)
                        .build()))
                .build();
        dataFields.set(0, firstField);


        defaultTemplate = defaultTemplate.toBuilder()
                .setName("BK5 VIP Ticket")
                .setDescription("The Bangkok Five - VIP Ticket")
                .setImageIds(Image.ImageIds.newBuilder()
                        .setIcon(eventImageIds.getIcon())
                        .setLogo(eventImageIds.getLogo())
                        .setAppleLogo(eventImageIds.getAppleLogo())
                        .setHero(eventImageIds.getHero())
                        .setThumbnail(eventImageIds.getThumbnail())
                        .setBackground(eventImageIds.getBackground())
                        .clearEventStrip()
                        .build())
                .setColors(Template.Colors.newBuilder()
                        .setTextColor("FFFFFF")
                        .setLabelColor("FFEA6C")
                        .setBackgroundColor("000000")
                        .clearStripColor()
                        .build())
                .setTimezone("Asia/Bangkok")
                .setData(Template.Data.newBuilder()
                        .addAllDataFields(dataFields)
                        .build())
                .build();

        vipTemplateId = templatesStub.createTemplate(defaultTemplate);
    }

    private void createVenue() {
        VenueOuterClass.Venue venue = VenueOuterClass.Venue.newBuilder()
                .setName("Impact Arena, Muang Thong Thani")
                .setAddress("Popular 3 Rd,\nPak Kret District,\nNonthaburi 11120,\nThailand")
                .setTimezone("Asia/Bangkok")
                .setLocalizedName(Localization.LocalizedString.newBuilder()
                        .putTranslations("TH", "อิมแพ็ค เมืองทองธานี")
                        .build())
                .addGpsCoords(Proximity.GPSLocation.newBuilder()
                        .setLat(13.913)
                        .setLon(100.5478)
                        .build())
                //.setUid("IMPACT-AREBKK")
                .build();

        venueId = eventsStub.createVenue(venue);
    }

    private void createProduction() {
        ProductionOuterClass.Production production = ProductionOuterClass.Production.newBuilder()
                .setName("The Bangkok Five")
                .setFinePrint("1. In case of postpone/cancellation, please bring the ticket(s) to refund at 14 Main " +
                        "Outlets and will receive credit return within 2 months.\n" +
                        "2. Lost of ticket(s) of fixed seats, please present police report with a signed copy of " +
                        "photo ID or passport at the venue on event day.\n" +
                        "3. Lost of ticket(s) of non-fixed seats, standing. Thaiticketmajor reserve the right to " +
                        "refuse issue new ticket(s) in any circumstances.\n" +
                        "4. The transaction is completed cannot be changed round(s), seat(s), event and refundable.")
                .setLocalizedFinePrint(Localization.LocalizedString.newBuilder()
                        .putTranslations("TH", "1. ถ้าการแสดงเลื่อน, ยกเลิก ท่านต้องนำบัตรคืนที่ 14 สาขาหลัก ภายในวันที่กำหนด " +
                                "โดยจะได้รับวงเงินคืนกลับที่บัตรเครดิตภายใน 2 เดือน\n" +
                                "2. บัตรสูญหาย ซึ่งเป็นบัตรระบุที่นั่ง กรุณาแสดงใบแจ้งความ พร้อมสำเนาบัตรประชาชน ติดต่อที่หน้างานในวันแสดง\n" +
                                "3. บัตรสูญหาย ซึ่งเป็นบัตรไม่ระบุที่นั่ง หรือ เป็นบัตรยืน ทางบริษัทขอสงวนสิทธิ์ไม่ออกบัตรให้ใหม่ ในทุกกรณี\n" +
                                "4. เมื่อทำรายการเสร็จสมบูรณ์แล้ว ไม่สามารถเปลี่ยนรอบ, เปลี่ยนที่นั่ง, เปลี่ยนการแสดง หรือ ขอคืนเงิน")
                        .build())
                //.setUid("BK5-IMPACT")
                .setAutoInvalidateTicketsUponEventEnd(CommonObjects.Toggle.ON)
                .build();

        productionId = eventsStub.createProduction(production);
    }

    private void createTicketTypes() {
        TicketTypeOuterClass.TicketType ticketType = TicketTypeOuterClass.TicketType.newBuilder()
                .setBeforeRedeemPassTemplateId(baseTemplateId.getId())
                .setName("BK5 CAT B")
                .setProductionId(productionId.getId())
                .setUid("BK5-SEAT")
                .build();

        baseTicketTypeId = eventsStub.createTicketType(ticketType);

        ticketType = ticketType.toBuilder()
                .setBeforeRedeemPassTemplateId(vipTemplateId.getId())
                .setName("BK5 VIP")
                .setUid("BK5-VIP")
                .build();

        vipTicketTypeId = eventsStub.createTicketType(ticketType);
    }

    private void createTickets() {

        // set the event date to 14 days from today in Asia/Bangkok Timezone
        LocalTime eightPM = LocalTime.of(20, 0);
        LocalDate eventDate = LocalDate.now(ZoneId.of("Asia/Bangkok")).plusDays(14);
        LocalDateTime eventDateTime = LocalDateTime.of(eventDate, eightPM);
        eventTimeUnix = eventDateTime.toEpochSecond(ZoneId.of("Asia/Bangkok").getRules().getOffset(eventDateTime));

        TicketOuterClass.IssueTicketRequest ticket = TicketOuterClass.IssueTicketRequest.newBuilder()
                .setTicketTypeId(baseTicketTypeId.getId())
                .setEvent(EventOuterClass.EventLimitedFieldsRequest.newBuilder()
                        .setProductionId(productionId.getId())
                        .setVenueId(venueId.getId())
                        .setScheduledStartDate(Timestamp.newBuilder()
                                .setSeconds(eventTimeUnix)
                                .build())
                        .setDoorsOpen(Timestamp.newBuilder()
                                .setSeconds(eventTimeUnix-7200)
                                .build())
                        .setEndDate(Timestamp.newBuilder()
                                .setSeconds(eventTimeUnix+7200)
                                .build())
                        .setRelevantDate(Timestamp.newBuilder()
                                .setSeconds(eventTimeUnix)
                                .build()))
                .setExpiryDate(Timestamp.newBuilder()
                        .setSeconds(eventTimeUnix+10800)
                        .build())
                .setOrderNumber("2940571")
                .setFaceValue(TicketOuterClass.FaceValue.newBuilder()
                        .setAmount(3000)
                        .setCurrency("THB")
                        .build())
                .setPerson(Personal.Person.newBuilder()
                        .setDisplayName("Nangsao Kor")
                        .build())
                .setSeatInfo(TicketOuterClass.Seat.newBuilder()
                        .setGate("9")
                        .setSection("SC")
                        .setRow("F")
                        .setSeat("22")
                        .build())
                .setTicketNumber("4929910033527")
                .build();

        baseTicket = eventsStub.issueTicket(ticket);

        ticket = ticket.toBuilder()
                .setTicketTypeId(vipTicketTypeId.getId())
                .setFaceValue(TicketOuterClass.FaceValue.newBuilder()
                        .setAmount(10000)
                        .setCurrency("THB")
                        .build())
                .setPerson(Personal.Person.newBuilder()
                        .setDisplayName("Nai Kor")
                        .build())
                .setSeatInfo(TicketOuterClass.Seat.newBuilder()
                        .setGate("1")
                        .setSection("VP")
                        .setRow("A")
                        .setSeat("88")
                        .build())
                .setTicketNumber("4929910033528")
                .build();

        vipTicket = eventsStub.issueTicket(ticket);

        TicketOuterClass.EventTicketPassRequest passRequest = TicketOuterClass.EventTicketPassRequest.newBuilder()
                .setTicketId(CommonObjects.Id.newBuilder()
                        .setId(baseTicket.getId())
                        .build())
                .addFormat(com.passkit.grpc.CommonObjects.PassBundleFormat.PASS_URL)
                .addFormat(com.passkit.grpc.CommonObjects.PassBundleFormat.MULTI_LINK)
                .build();
        CommonObjects.PassBundles passes = eventsStub.getEventTicketPass(passRequest);
        basePass = passes.getPasses(0);

        passRequest = TicketOuterClass.EventTicketPassRequest.newBuilder()
                .setTicketId(CommonObjects.Id.newBuilder()
                        .setId(vipTicket.getId())
                        .build())
                .addFormat(com.passkit.grpc.CommonObjects.PassBundleFormat.PASS_URL)
                .addFormat(com.passkit.grpc.CommonObjects.PassBundleFormat.MULTI_LINK)
                .build();
        passes = eventsStub.getEventTicketPass(passRequest);
        vipPass = passes.getPasses(0);
    }

    private void validateTickets() {
        TicketOuterClass.ValidateTicketRequest request = TicketOuterClass.ValidateTicketRequest.newBuilder()
                .setMaxNumberOfValidations(1)
                .setTicket(TicketOuterClass.TicketId.newBuilder()
                        .setTicketId(baseTicket.getId()).build())
                .setValidateDetails(TicketOuterClass.ValidateDetails.newBuilder()
                        .setValidateDate(Timestamp.newBuilder()
                                .setSeconds(System.currentTimeMillis() / 1000L)
                                .build())
                        .setLat(47.50846)
                        .setLon(11.14335)
                        .setAlt(550)
                        .setValidateSource("PassReader App")
                        .setValidateReference("888")
                        .build())
                .build();

        baseTicketValidation = eventsStub.validateTicket(request);

        request = TicketOuterClass.ValidateTicketRequest.newBuilder()
                .setMaxNumberOfValidations(1)
                .setTicket(TicketOuterClass.TicketId.newBuilder()
                        .setTicketNumber(TicketOuterClass.TicketNumber.newBuilder()
                                .setProductionId(productionId.getId())
                                .setTicketNumber("4929910033528")
                                .build())
                )
                .setValidateDetails(TicketOuterClass.ValidateDetails.newBuilder()
                        .setValidateDate(Timestamp.newBuilder()
                                .setSeconds(System.currentTimeMillis() / 1000L)
                                .build())
                        .setLat(47.50846)
                        .setLon(11.14335)
                        .setAlt(550)
                        .setValidateSource("PassReader App")
                        .setValidateReference("777")
                        .build())
                .build();

        vipTicketValidation = eventsStub.validateTicket(request);
    }

    private void redeemTickets() {
        TicketOuterClass.RedeemTicketRequest request = TicketOuterClass.RedeemTicketRequest.newBuilder()
                .setTicket(TicketOuterClass.TicketId.newBuilder()
                        .setTicketId(baseTicket.getId()).build())
                .setRedemptionDetails(TicketOuterClass.RedemptionDetails.newBuilder()
                        .setRedemptionDate(Timestamp.newBuilder()
                                .setSeconds(System.currentTimeMillis() / 1000L)
                                .build())
                        .setLat(47.50846)
                        .setLon(11.14335)
                        .setAlt(550)
                        .setRedemptionSource("PassReader App")
                        .setRedemptionReference("888")
                        .build())
                .build();

        baseTicketRedeemedId = eventsStub.redeemTicket(request);

        request = TicketOuterClass.RedeemTicketRequest.newBuilder()
                .setTicket(TicketOuterClass.TicketId.newBuilder()
                        .setTicketNumber(TicketOuterClass.TicketNumber.newBuilder()
                                .setProductionId(productionId.getId())
                                .setTicketNumber("4929910033528")
                                .build())
                )
                .setRedemptionDetails(TicketOuterClass.RedemptionDetails.newBuilder()
                        .setRedemptionDate(Timestamp.newBuilder()
                                .setSeconds(System.currentTimeMillis() / 1000L)
                                .build())
                        .setLat(47.50846)
                        .setLon(11.14335)
                        .setAlt(550)
                        .setRedemptionSource("PassReader App")
                        .setRedemptionReference("777")
                        .build())
                .build();

        vipTicketRedeemedId = eventsStub.redeemTicket(request);
    }

    public static void cleanup() {
        eventsStub.deleteEvent(EventOuterClass.Event.newBuilder()
                .setProduction(ProductionOuterClass.Production.newBuilder()
                        .setId(productionId.getId())
                        .build())
                .setVenue(VenueOuterClass.Venue.newBuilder()
                        .setId(venueId.getId())
                        .build())
                .setScheduledStartDate(Timestamp.newBuilder()
                        .setSeconds(eventTimeUnix)
                        .build())
                .build());
        eventsStub.deleteTicketType(TicketTypeOuterClass.TicketType.newBuilder()
                .setProductionId(productionId.getId())
                .setUid("BK5-SEAT")
                .build());
        eventsStub.deleteTicketType(TicketTypeOuterClass.TicketType.newBuilder()
                .setProductionId(productionId.getId())
                .setUid("BK5-VIP")
                .build());
        eventsStub.deleteProduction(ProductionOuterClass.Production.newBuilder()
                .setId(productionId.getId())
                .build());
        eventsStub.deleteVenue(VenueOuterClass.Venue.newBuilder()
                .setId(venueId.getId())
                .build());
        templatesStub.deleteTemplate(baseTemplateId);
        templatesStub.deleteTemplate(vipTemplateId);
        imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(eventImageIds.getIcon()).build());
        imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(eventImageIds.getLogo()).build());
        imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(eventImageIds.getAppleLogo()).build());
        imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(eventImageIds.getEventStrip()).build());
        imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(eventImageIds.getHero()).build());
        imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(eventImageIds.getBackground()).build());
        imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(eventImageIds.getThumbnail()).build());

        // always close the channel when there will be no further calls made.
        conn.closeChannel();
    }
}
