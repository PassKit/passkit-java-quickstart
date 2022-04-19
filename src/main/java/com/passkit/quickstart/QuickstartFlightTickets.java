package com.passkit.quickstart;

import com.passkit.grpc.CommonObjects;
import com.passkit.grpc.CommonObjects.LocalDateTime;
import com.passkit.grpc.CommonObjects.Time;
import com.passkit.grpc.Image;
import com.passkit.grpc.ImagesGrpc;
import com.passkit.grpc.Personal;
import com.passkit.grpc.Protocols;
import com.passkit.grpc.Template;
import com.passkit.grpc.TemplatesGrpc;
import com.passkit.grpc.Flights.Airport;
import com.passkit.grpc.Flights.Airport.AirportCode;
import com.passkit.grpc.Flights.BoardingPass;
import com.passkit.grpc.Flights.CarrierOuterClass;
import com.passkit.grpc.Flights.CarrierOuterClass.CarrierCode;
import com.passkit.grpc.Flights.FlightDesignatorOuterClass;
import com.passkit.grpc.Flights.FlightDesignatorOuterClass.FlightDesignatorRequest;
import com.passkit.grpc.Flights.FlightDesignatorOuterClass.FlightSchedule;
import com.passkit.grpc.Flights.FlightOuterClass;
import com.passkit.grpc.Flights.FlightsGrpc;
import com.passkit.grpc.Flights.PassengerOuterClass;

import java.io.IOException;
import java.util.ArrayList;

/* Quickstart Flight Tickets runs through the high level steps required to create flight tickets from scratch using the PassKit gRPC Java SDK. 
 */
public class QuickstartFlightTickets {

        private static GrpcConnection conn;

        public QuickstartFlightTickets() {
                // initiate client stubs
                try {
                        conn = new GrpcConnection();
                        imagesStub = ImagesGrpc.newBlockingStub(conn.getChannel());
                        templatesStub = TemplatesGrpc.newBlockingStub(conn.getChannel());
                        flightsStub = FlightsGrpc.newBlockingStub(conn.getChannel());
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
        private static FlightsGrpc.FlightsBlockingStub flightsStub;
        private static TemplatesGrpc.TemplatesBlockingStub templatesStub;
        private static String appleCertificate = ""; // Change to your certificate id

        /*
         * Quickstart will walk through the following steps:
         * - Create image assets
         * - Modify default template for a regular flight ticket
         * - Modify default template for a vip flight ticket
         * - Create a flight
         * - Create an airport
         * - Create a basic ticket type
         * - Create a VIP ticket type
         * - Issue a basic ticket (auto create an event)
         * - Issue a VIP ticket
         * - Validate tickets
         * - Redeem tickets
         * - Delete all ticket assets
         * 
         * If you would like to retain the assets created, set
         * delete.assets.timeout.seconds=-1 in the passkit.properties file.
         */

        // Public objects for testing purposes
        public static Image.ImageIds flightImageIds;
        public static CommonObjects.Id baseTemplateId;
        public static CommonObjects.Id vipTemplateId;
        public static CommonObjects.Id baseTicketTypeId;
        public static CommonObjects.Id vipTicketTypeId;
        public static CommonObjects.Id baseTicket;
        public static CommonObjects.Id vipTicket;
        public static CommonObjects.Id basePass;
        public static CommonObjects.PassBundle vipPass;
        public static CommonObjects.Id baseTicketRedeemedId;
        public static CommonObjects.Id vipTicketRedeemedId;
        public static long eventTimeUnix;
        public static CarrierCode carrierCode;

        public void quickStart() {
                createImages();
                createTemplates();
                createCarrier();
                createAirport();
                createFlight();
                createFlightDesignator();
                createBoardingPass();

        }

        private void createImages() {
                // Create the image assets we'll need for the pass designs. A logo and icon are
                // mandatory.
                String icon, logo, appleLogo, background, thumbnail, hero, eventStrip;
                try {
                        icon = Helpers.encodeFileToBase64("src/main/resources/images/eventTickets/icon.png");
                        logo = Helpers.encodeFileToBase64("src/main/resources/images/eventTickets/logo.png");
                        appleLogo = Helpers.encodeFileToBase64("src/main/resources/images/eventTickets/appleLogo.png");
                        background = Helpers
                                        .encodeFileToBase64("src/main/resources/images/eventTickets/background.png");
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
                                                        .setEventStrip(eventStrip))
                                        .build();

                        flightImageIds = imagesStub.createImages(imageInput);
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        private void createTemplates() {
                // Get the default template - revision 1 is a template that contains
                // comprehensive fields
                Template.DefaultTemplateRequest templateRequest = Template.DefaultTemplateRequest.newBuilder()
                                .setProtocol(Protocols.PassProtocol.FLIGHT_PROTOCOL)
                                .setRevision(1)
                                .build();
                Template.PassTemplate defaultTemplate = templatesStub.getDefaultTemplate(templateRequest);

                // Modify the default template for the base tier
                defaultTemplate = defaultTemplate.toBuilder()
                                .setName("ABC Flight Ticket")
                                .setDescription("ABC - Economy Section Ticket ")
                                .setImageIds(Image.ImageIds.newBuilder()
                                                .setIcon(flightImageIds.getIcon())
                                                .setLogo(flightImageIds.getLogo())
                                                .setAppleLogo(flightImageIds.getAppleLogo())
                                                .setEventStrip(flightImageIds.getEventStrip())
                                                .setHero(flightImageIds.getHero())
                                                .build())
                                .setColors(Template.Colors.newBuilder()
                                                .setTextColor("000000")
                                                .setLabelColor("000000")
                                                .setStripColor("000000")
                                                .setBackgroundColor("FFEA6C")
                                                .build())
                                .setTimezone("Europe/London")
                                .build();

                baseTemplateId = templatesStub.createTemplate(defaultTemplate);

                ArrayList<Template.DataField> dataFields = new ArrayList<>(
                                defaultTemplate.getData().getDataFieldsList());
                Template.DataField firstField = dataFields.get(0);
                firstField = firstField.toBuilder()
                                .setAppleWalletFieldRenderOptions(firstField.getAppleWalletFieldRenderOptions()
                                                .toBuilder()
                                                .setPositionSettings(Template.PositionSettings.newBuilder()
                                                                .setSection(Template.FieldSection.PRIMARY_FIELDS)
                                                                .build()))
                                .build();
                dataFields.set(0, firstField);

                // Modify the default template for the VIP tier
                defaultTemplate = defaultTemplate.toBuilder()
                                .setName("ABC VIP Flight Ticket")
                                .setDescription("ABC - VIP section Ticket")
                                .setImageIds(Image.ImageIds.newBuilder()
                                                .setIcon(flightImageIds.getIcon())
                                                .setLogo(flightImageIds.getLogo())
                                                .setAppleLogo(flightImageIds.getAppleLogo())
                                                .setHero(flightImageIds.getHero())
                                                .setThumbnail(flightImageIds.getThumbnail())
                                                .setBackground(flightImageIds.getBackground())
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

        private void createAirport() {
                // Creates departure and arrival airports if not currently created
                System.out.println("creating departure airport");
                // Modify depature airport
                Airport.Port departureAirport = Airport.Port.newBuilder()
                                .setAirportName("ABC Airport")
                                .setCityName("ABC")
                                .setIataAirportCode("YY4")
                                .setIcaoAirportCode("YYYY")
                                .setCountryCode("IE")
                                .setTimezone("Europe/London")
                                .build();
                System.out.println(departureAirport);

                System.out.println("creating arrival airport");
                // Modify arrival airport
                Airport.Port arrivalAirport = Airport.Port.newBuilder()
                                .setAirportName("DEF Airport")
                                .setCityName("DEF")
                                .setIataAirportCode("ADP")
                                .setIcaoAirportCode("ADPY")
                                .setCountryCode("HK")
                                .setTimezone("Asia/Hong_Kong")
                                .build();
                System.out.println(arrivalAirport);

                flightsStub.createPort(departureAirport);
                flightsStub.createPort(arrivalAirport);
        }

        private void createCarrier() {
                // Creates carrier
                System.out.println("creating Carrier");
                // Modify carrier
                CarrierOuterClass.Carrier carrier = CarrierOuterClass.Carrier.newBuilder()
                                .setAirlineName("ABC Airline ")
                                .setIataCarrierCode("YY")
                                .setPassTypeIdentifier(appleCertificate)
                                .build();
                System.out.println(carrier);
                flightsStub.createCarrier(carrier);
        }

        private void createFlightDesignator() {
                // Creates flight designator
                // Modify flight designator below
                FlightDesignatorOuterClass.FlightDesignator flightDesignator = FlightDesignatorOuterClass.FlightDesignator
                                .newBuilder()
                                .setCarrierCode("YY")
                                .setFlightNumber("YY123")
                                .setRevision(2)
                                .setSchedule(FlightSchedule.newBuilder()
                                                .setMonday(FlightSchedule.newBuilder().getMondayBuilder()
                                                                .setScheduledDepartureTime(Time.newBuilder().setHour(13)
                                                                                .setMinute(00).setSecond(0).build())
                                                                .setGateClosingTime(Time.newBuilder().setHour(12)
                                                                                .setMinute(30).setSecond(0).build())
                                                                .setScheduledArrivalTime(Time.newBuilder().setHour(14)
                                                                                .setMinute(00).setSecond(0))
                                                                .setBoardingTime(Time.newBuilder().setHour(12)
                                                                                .setMinute(15).setSecond(0).build()))

                                                .setTuesday(FlightSchedule.newBuilder().getTuesdayBuilder()
                                                                .setBoardingTime(Time.newBuilder().setHour(0)
                                                                                .setMinute(0).setSecond(0).build())
                                                                .setGateClosingTime(Time.newBuilder().setHour(0)
                                                                                .setMinute(0).setSecond(0).build())
                                                                .setScheduledArrivalTime(Time.newBuilder().setHour(0)
                                                                                .setMinute(00).setSecond(0))
                                                                .setScheduledDepartureTime(Time.newBuilder().setHour(0)
                                                                                .setMinute(0).setSecond(0).build()))

                                                .setWednesday(FlightSchedule.newBuilder().getWednesdayBuilder()
                                                                .setBoardingTime(Time.newBuilder().setHour(0)
                                                                                .setMinute(0).setSecond(0).build())
                                                                .setGateClosingTime(Time.newBuilder().setHour(0)
                                                                                .setMinute(0).setSecond(0).build())
                                                                .setScheduledArrivalTime(Time.newBuilder().setHour(0)
                                                                                .setMinute(00).setSecond(0))
                                                                .setScheduledDepartureTime(Time.newBuilder().setHour(0)
                                                                                .setMinute(0).setSecond(0).build()))

                                                .setThursday(FlightSchedule.newBuilder().getThursdayBuilder()
                                                                .setBoardingTime(Time.newBuilder().setHour(0)
                                                                                .setMinute(0).setSecond(0).build())
                                                                .setGateClosingTime(Time.newBuilder().setHour(0)
                                                                                .setMinute(0).setSecond(0).build())
                                                                .setScheduledArrivalTime(Time.newBuilder().setHour(0)
                                                                                .setMinute(00).setSecond(0))
                                                                .setScheduledDepartureTime(Time.newBuilder().setHour(0)
                                                                                .setMinute(0).setSecond(0).build()))

                                                .setFriday(FlightSchedule.newBuilder().getFridayBuilder()
                                                                .setBoardingTime(Time.newBuilder().setHour(0)
                                                                                .setMinute(0).setSecond(0).build())
                                                                .setGateClosingTime(Time.newBuilder().setHour(0)
                                                                                .setMinute(0).setSecond(0).build())
                                                                .setScheduledArrivalTime(Time.newBuilder().setHour(0)
                                                                                .setMinute(00).setSecond(0))
                                                                .setScheduledDepartureTime(Time.newBuilder().setHour(0)
                                                                                .setMinute(0).setSecond(0).build()))

                                                .setSaturday(FlightSchedule.newBuilder().getSaturdayBuilder()
                                                                .setBoardingTime(Time.newBuilder().setHour(0)
                                                                                .setMinute(0).setSecond(0).build())
                                                                .setGateClosingTime(Time.newBuilder().setHour(0)
                                                                                .setMinute(0).setSecond(0).build())
                                                                .setScheduledArrivalTime(Time.newBuilder().setHour(0)
                                                                                .setMinute(00).setSecond(0))
                                                                .setScheduledDepartureTime(Time.newBuilder().setHour(0)
                                                                                .setMinute(0).setSecond(0).build()))

                                                .setSunday(FlightSchedule.newBuilder().getSundayBuilder()
                                                                .setBoardingTime(Time.newBuilder().setHour(0)
                                                                                .setMinute(0).setSecond(0).build())
                                                                .setGateClosingTime(Time.newBuilder().setHour(0)
                                                                                .setMinute(0).setSecond(0).build())
                                                                .setScheduledArrivalTime(Time.newBuilder().setHour(0)
                                                                                .setMinute(00).setSecond(0))
                                                                .setScheduledDepartureTime(Time.newBuilder().setHour(0)
                                                                                .setMinute(0).setSecond(0).build()))
                                                .build())
                                .setOrigin("Dublin")
                                .setDestination("London")
                                .setPassTemplateId(baseTemplateId.getId())
                                .build();
                flightsStub.createFlightDesignator(flightDesignator);
        }

        private void createFlight() {
                // Creates flight
                System.out.println("creating Flight");
                // Modify flight details below
                LocalDateTime flightDateTime = LocalDateTime.newBuilder()
                                .setDateTime("2022-02-28T18:00:00")
                                .build();
                FlightOuterClass.Flight flight = FlightOuterClass.Flight.newBuilder()
                                .setCarrierCode("YY")
                                .setFlightNumber("YY123")
                                .setBoardingPoint("YY4")
                                .setDeplaningPoint("ADP")
                                .setDepartureDate(CommonObjects.Date.newBuilder()
                                                .setDay(28)
                                                .setMonth(2)
                                                .setYear(2022)
                                                .build())
                                .setScheduledDepartureTime(flightDateTime)
                                .setPassTemplateId(baseTemplateId.getId())
                                .build();
                System.out.println(flight);
                flightsStub.createFlight(flight);
        }

        private void createBoardingPass() {
                // Creates boarding pass
                // Modify boarding pass below
                BoardingPass.BoardingPassRecord boardingPassRecord = BoardingPass.BoardingPassRecord.newBuilder()
                                .setOperatingCarrierPNR("P8F8R8")
                                .setBoardingPoint("YY4")
                                .setDeplaningPoint("ADP")
                                .setCarrierCode("YY")
                                .setFlightNumber("YY123")
                                .setDepartureDate(CommonObjects.Date.newBuilder()
                                                .setDay(28)
                                                .setMonth(2)
                                                .setYear(2022)
                                                .build())
                                .setPassenger(PassengerOuterClass.Passenger.newBuilder()
                                                .setPassengerDetails(Personal.Person.newBuilder()
                                                                .setForename("John")
                                                                .setSurname("Smith")
                                                                .build())
                                                .build())
                                .setSequenceNumber(123)
                                .build();
                System.out.println(boardingPassRecord.getId());
                flightsStub.createBoardingPass(boardingPassRecord);

        }

        public static void cleanup() {
                flightsStub.deleteFlight(FlightOuterClass.FlightRequest.newBuilder()
                                .setCarrierCode("YY")
                                .setFlightNumber("YY123")
                                .setBoardingPoint("YY4")
                                .setDeplaningPoint("ADP")
                                .build());
                flightsStub.deleteCarrier(CarrierCode.newBuilder()
                                .setCarrierCode("YY")
                                .build());
                flightsStub.deleteFlightDesignator(FlightDesignatorRequest.newBuilder()
                                .setCarrierCode("YY")
                                .setFlightNumber("YY123")
                                .build());
                flightsStub.deletePort(AirportCode.newBuilder()
                                .setAirportCode("YY4")
                                .build());
                flightsStub.deletePort(AirportCode.newBuilder()
                                .setAirportCode("ADP")
                                .build());
                templatesStub.deleteTemplate(baseTemplateId);
                templatesStub.deleteTemplate(vipTemplateId);
                imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(flightImageIds.getIcon()).build());
                imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(flightImageIds.getLogo()).build());
                imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(flightImageIds.getAppleLogo()).build());
                imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(flightImageIds.getEventStrip()).build());
                imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(flightImageIds.getHero()).build());
                imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(flightImageIds.getBackground()).build());
                imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(flightImageIds.getThumbnail()).build());

                // always close the channel when there will be no further calls made.
                conn.closeChannel();
        }
}
