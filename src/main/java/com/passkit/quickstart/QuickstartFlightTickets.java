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
import com.passkit.grpc.Flights.BoardingPass.BoardingPassesResponse;
import com.passkit.grpc.Flights.BoardingPass;
import com.passkit.grpc.Flights.CarrierOuterClass;
import com.passkit.grpc.Flights.CarrierOuterClass.CarrierCode;
import com.passkit.grpc.Flights.FlightDesignatorOuterClass;
import com.passkit.grpc.Flights.FlightDesignatorOuterClass.FlightDesignatorRequest;
import com.passkit.grpc.Flights.FlightDesignatorOuterClass.FlightSchedule;
import com.passkit.grpc.Flights.FlightDesignatorOuterClass.FlightTimes;
import com.passkit.grpc.Flights.FlightOuterClass;
import com.passkit.grpc.Flights.FlightsGrpc;
import com.passkit.grpc.Flights.PassengerOuterClass;

import java.io.IOException;

/* Quickstart Flight Tickets runs through the high level steps required to create flight tickets from scratch using the PassKit gRPC Java SDK. 
 */
public class QuickstartFlightTickets {

        private static GrpcConnection conn;

        // Connection for pooling
        /**
         * private static GrpcConnectionPool connectionPool;
         * 
         * // Quickstart set up for pool connections
         * public QuickstartFlightTickets(int poolSize) {
         * try {
         * // Initialize the gRPC connection pool with the specified pool size
         * connectionPool = new GrpcConnectionPool(poolSize);
         * 
         * // Initialize stubs using channels from the pool
         * imagesStub = ImagesGrpc.newBlockingStub(connectionPool.getChannel());
         * templatesStub = TemplatesGrpc.newBlockingStub(connectionPool.getChannel());
         * flightsStub = FlightsGrpc.newBlockingStub(connectionPool.getChannel());
         * } catch (IOException e) {
         * e.printStackTrace();
         * shutdownPool();
         * System.exit(1);
         * }
         * }
         **/

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
        private static String appleCertificate = "pass.com.passkit.e2e"; // Replace with your apple certificate id

        /*
         * Quickstart will walk through the following steps:
         * - Create image assets
         * - Modify default template for a regular flight ticket
         * - Create a flight
         * - Create an airport
         * - Issue a basic ticket (auto create an event)
         * - Delete all ticket assets
         * 
         * Each method has the minimum information needed to execute the method, if you
         * would like to add more details please refer to
         * https://docs.passkit.io/protocols/boarding/
         * for fields that can be added.
         * If you would like to retain the assets created, set
         * delete.assets.timeout.seconds=-1 in the passkit.properties file.
         */

        // Public objects for testing purposes
        public static Image.ImageIds flightImageIds;
        public static CommonObjects.Id templateId;
        public static BoardingPassesResponse pass;

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

                templateId = templatesStub.createTemplate(defaultTemplate);
        }

        private void createCarrier() {
                // Creates carrier
                System.out.println("creating carrier");
                // Modify carrier
                CarrierOuterClass.Carrier carrier = CarrierOuterClass.Carrier.newBuilder()
                                .setAirlineName("ABC Airline ")
                                .setIataCarrierCode("YY")
                                .setPassTypeIdentifier(appleCertificate)
                                .build();
                flightsStub.createCarrier(carrier);
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

                flightsStub.createPort(departureAirport);
                flightsStub.createPort(arrivalAirport);
        }

        private void createFlight() {
                // Creates flight
                System.out.println("creating flight");
                // Modify flight details below
                LocalDateTime flightDateTime = LocalDateTime.newBuilder()
                                .setDateTime("2022-04-25T13:00:00")
                                .build();
                FlightOuterClass.Flight flight = FlightOuterClass.Flight.newBuilder()
                                .setCarrierCode("YY")
                                .setFlightNumber("123")
                                .setBoardingPoint("YY4")
                                .setDeplaningPoint("ADP")
                                .setDepartureDate(CommonObjects.Date.newBuilder()
                                                .setDay(25)
                                                .setMonth(4)
                                                .setYear(2022)
                                                .build())
                                .setScheduledDepartureTime(flightDateTime)
                                .setPassTemplateId(templateId.getId())
                                .build();
                flightsStub.createFlight(flight);
        }

        private void createFlightDesignator() {
                // Creates flight designator
                // Modify flight designator below
                FlightDesignatorOuterClass.FlightDesignator flightDesignator = FlightDesignatorOuterClass.FlightDesignator
                                .newBuilder()
                                .setCarrierCode("YY")
                                .setFlightNumber("123")
                                .setRevision(2)
                                .setSchedule(FlightSchedule.newBuilder()
                                                .setMonday(FlightTimes.newBuilder()
                                                                .setScheduledDepartureTime(Time.newBuilder().setHour(13)
                                                                                .setMinute(00).setSecond(0).build())
                                                                .setBoardingTime(Time.newBuilder().setHour(12)
                                                                                .setMinute(15).setSecond(0).build())
                                                                .setGateClosingTime(Time.newBuilder().setHour(12)
                                                                                .setMinute(30).setSecond(0).build())
                                                                .setScheduledArrivalTime(Time.newBuilder().setHour(14)
                                                                                .setMinute(00).setSecond(0)))

                                                .setTuesday(FlightTimes.newBuilder()
                                                                .setScheduledDepartureTime(Time.newBuilder().setHour(13)
                                                                                .setMinute(00).setSecond(0).build())
                                                                .setBoardingTime(Time.newBuilder().setHour(12)
                                                                                .setMinute(15).setSecond(0).build())
                                                                .setGateClosingTime(Time.newBuilder().setHour(12)
                                                                                .setMinute(30).setSecond(0).build())
                                                                .setScheduledArrivalTime(Time.newBuilder().setHour(14)
                                                                                .setMinute(00).setSecond(0)))

                                                .setWednesday(FlightTimes.newBuilder()
                                                                .setScheduledDepartureTime(Time.newBuilder().setHour(13)
                                                                                .setMinute(00).setSecond(0).build())
                                                                .setBoardingTime(Time.newBuilder().setHour(12)
                                                                                .setMinute(15).setSecond(0).build())
                                                                .setGateClosingTime(Time.newBuilder().setHour(12)
                                                                                .setMinute(30).setSecond(0).build())
                                                                .setScheduledArrivalTime(Time.newBuilder().setHour(14)
                                                                                .setMinute(00).setSecond(0)))

                                                .setThursday(FlightTimes.newBuilder()
                                                                .setScheduledDepartureTime(Time.newBuilder().setHour(13)
                                                                                .setMinute(00).setSecond(0).build())
                                                                .setBoardingTime(Time.newBuilder().setHour(12)
                                                                                .setMinute(15).setSecond(0).build())
                                                                .setGateClosingTime(Time.newBuilder().setHour(12)
                                                                                .setMinute(30).setSecond(0).build())
                                                                .setScheduledArrivalTime(Time.newBuilder().setHour(14)
                                                                                .setMinute(00).setSecond(0)))

                                                .setFriday(FlightTimes.newBuilder()
                                                                .setScheduledDepartureTime(Time.newBuilder().setHour(13)
                                                                                .setMinute(00).setSecond(0).build())
                                                                .setBoardingTime(Time.newBuilder().setHour(12)
                                                                                .setMinute(15).setSecond(0).build())
                                                                .setGateClosingTime(Time.newBuilder().setHour(12)
                                                                                .setMinute(30).setSecond(0).build())
                                                                .setScheduledArrivalTime(Time.newBuilder().setHour(14)
                                                                                .setMinute(00).setSecond(0)))

                                                .setSaturday(FlightTimes.newBuilder()
                                                                .setScheduledDepartureTime(Time.newBuilder().setHour(13)
                                                                                .setMinute(00).setSecond(0).build())
                                                                .setBoardingTime(Time.newBuilder().setHour(12)
                                                                                .setMinute(15).setSecond(0).build())
                                                                .setGateClosingTime(Time.newBuilder().setHour(12)
                                                                                .setMinute(30).setSecond(0).build())
                                                                .setScheduledArrivalTime(Time.newBuilder().setHour(14)
                                                                                .setMinute(00).setSecond(0)))

                                                .setSunday(FlightTimes.newBuilder()
                                                                .setScheduledDepartureTime(Time.newBuilder().setHour(13)
                                                                                .setMinute(00).setSecond(0).build())
                                                                .setBoardingTime(Time.newBuilder().setHour(12)
                                                                                .setMinute(15).setSecond(0).build())
                                                                .setGateClosingTime(Time.newBuilder().setHour(12)
                                                                                .setMinute(30).setSecond(0).build())
                                                                .setScheduledArrivalTime(Time.newBuilder().setHour(14)
                                                                                .setMinute(00).setSecond(0)))
                                                .build())
                                .setOrigin("YY4")
                                .setDestination("ADP")
                                .setPassTemplateId(templateId.getId())
                                .build();
                flightsStub.createFlightDesignator(flightDesignator);
        }

        private void createBoardingPass() {
                // Creates boarding pass
                // Modify boarding pass below
                BoardingPass.BoardingPassRecord boardingPassRecord = BoardingPass.BoardingPassRecord.newBuilder()
                                .setOperatingCarrierPNR("P8F8R8")
                                .setBoardingPoint("YY4")
                                .setDeplaningPoint("ADP")
                                .setCarrierCode("YY")
                                .setFlightNumber("123")
                                .setDepartureDate(CommonObjects.Date.newBuilder()
                                                .setDay(25)
                                                .setMonth(4)
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
                pass = flightsStub.createBoardingPass(boardingPassRecord);
        }

        public static void cleanup() {
                flightsStub.deleteFlight(FlightOuterClass.FlightRequest.newBuilder()
                                .setCarrierCode("YY")
                                .setFlightNumber("123")
                                .setBoardingPoint("YY4")
                                .setDeplaningPoint("ADP")
                                .setDepartureDate(CommonObjects.Date.newBuilder()
                                                .setDay(25)
                                                .setMonth(4)
                                                .setYear(2022)
                                                .build())
                                .build());
                flightsStub.deleteFlightDesignator(FlightDesignatorRequest.newBuilder()
                                .setCarrierCode("YY")
                                .setFlightNumber("123")
                                .setRevision(2)
                                .build());
                flightsStub.deletePort(AirportCode.newBuilder()
                                .setAirportCode("YY4")
                                .build());
                flightsStub.deletePort(AirportCode.newBuilder()
                                .setAirportCode("ADP")
                                .build());
                // sleep to allow deleting of boarding passes for deleted flight to be processed
                try {
                        Thread.sleep(5 * 1000L);
                } catch (Exception e) {
                        e.printStackTrace();
                }
                flightsStub.deleteCarrier(CarrierCode.newBuilder()
                                .setCarrierCode("YY")
                                .build());
                templatesStub.deleteTemplate(templateId);
                imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(flightImageIds.getIcon()).build());
                imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(flightImageIds.getLogo()).build());
                imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(flightImageIds.getAppleLogo()).build());
                imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(flightImageIds.getEventStrip()).build());
                imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(flightImageIds.getHero()).build());
                imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(flightImageIds.getBackground()).build());
                imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(flightImageIds.getThumbnail()).build());

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
