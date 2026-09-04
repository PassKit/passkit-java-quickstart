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
import com.passkit.grpc.Flights.Barcode.FlightSchedule;
import com.passkit.grpc.Flights.Barcode.FlightTimes;
import com.passkit.grpc.Flights.CarrierOuterClass;
import com.passkit.grpc.Flights.CarrierOuterClass.CarrierCode;
import com.passkit.grpc.Flights.FlightDesignatorOuterClass;
import com.passkit.grpc.Flights.FlightDesignatorOuterClass.FlightDesignatorRequest;
import com.passkit.grpc.Flights.FlightOuterClass;
import com.passkit.grpc.Flights.FlightsGrpc;
import com.passkit.grpc.Flights.PassengerOuterClass;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.io.IOException;
import java.time.LocalDate;

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
                        appleCertificate = AppConfig.load().appleCertificateId();
                } catch (Exception e) {
                        if (conn != null) conn.closeChannel();
                        throw new IllegalStateException("Could not initialise the flights quickstart", e);
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
        private static String appleCertificate;
        private static final String carrierCode = "YY";
        private static final String FLIGHT_NUMBER = Long.toString(System.currentTimeMillis() % 900 + 100);
        private static final String origin = "YY4";
        private static final String destination = "ADP";
        private static final LocalDate DEPARTURE_DATE = LocalDate.now().plusDays(14);
        private static boolean createdCarrier;
        private static boolean createdOrigin;
        private static boolean createdDestination;

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

                        Image.CreateImageInput imageInput = Image.CreateImageInput.newBuilder()
                                        .setImageData(Image.ImageData.newBuilder()
                                                        .setIcon(icon)
                                                        .setLogo(logo)
                                                        .setAppleLogo(appleLogo))
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
                System.out.println("creating carrier");
                CarrierOuterClass.Carrier carrier = CarrierOuterClass.Carrier.newBuilder()
                                .setAirlineName("Quickstart Airline")
                                .setIataCarrierCode(carrierCode)
                                .setPassTypeIdentifier(appleCertificate)
                                .build();
                try {
                        flightsStub.createCarrier(carrier);
                        createdCarrier = true;
                } catch (StatusRuntimeException e) {
                        if (e.getStatus().getCode() != Status.Code.ALREADY_EXISTS) throw e;
                        System.out.println("Reusing existing carrier " + carrierCode + ".");
                }
        }

        private void createAirport() {
                System.out.println("creating departure airport");
                createdOrigin = createPort("Quickstart Departure Airport", "Origin", origin, "YYYY", "GB", "Europe/London");
                System.out.println("creating arrival airport");
                createdDestination = createPort("Quickstart Arrival Airport", "Destination", destination, "VHHH", "HK", "Asia/Hong_Kong");
        }

        private boolean createPort(String name, String city, String iata, String icao, String country, String timezone) {
                Airport.Port port = Airport.Port.newBuilder().setAirportName(name).setCityName(city)
                                .setIataAirportCode(iata).setIcaoAirportCode(icao)
                                .setCountryCode(country).setTimezone(timezone).build();
                try {
                        flightsStub.createPort(port);
                        return true;
                } catch (StatusRuntimeException e) {
                        if (e.getStatus().getCode() != Status.Code.ALREADY_EXISTS) throw e;
                        System.out.println("Reusing existing airport " + iata + ".");
                        return false;
                }
        }

        private void createFlight() {
                // Creates flight
                System.out.println("creating flight");
                // Modify flight details below
                LocalDateTime flightDateTime = LocalDateTime.newBuilder()
                                .setDateTime(DEPARTURE_DATE + "T13:00:00")
                                .build();
                FlightOuterClass.Flight flight = FlightOuterClass.Flight.newBuilder()
                                .setCarrierCode(carrierCode)
                                .setFlightNumber(FLIGHT_NUMBER)
                                .setBoardingPoint(origin)
                                .setDeplaningPoint(destination)
                                .setDepartureDate(CommonObjects.Date.newBuilder()
                                                .setDay(DEPARTURE_DATE.getDayOfMonth())
                                                .setMonth(DEPARTURE_DATE.getMonthValue())
                                                .setYear(DEPARTURE_DATE.getYear())
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
                                .setCarrierCode(carrierCode)
                                .setFlightNumber(FLIGHT_NUMBER)
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
                                .setOrigin(origin)
                                .setDestination(destination)
                                .setPassTemplateId(templateId.getId())
                                .build();
                flightsStub.createFlightDesignator(flightDesignator);
        }

        private void createBoardingPass() {
                // Creates boarding pass
                // Modify boarding pass below
                BoardingPass.BoardingPassRecord boardingPassRecord = BoardingPass.BoardingPassRecord.newBuilder()
                                .setOperatingCarrierPNR("P8F8R8")
                                .setBoardingPoint(origin)
                                .setDeplaningPoint(destination)
                                .setCarrierCode(carrierCode)
                                .setFlightNumber(FLIGHT_NUMBER)
                                .setDepartureDate(CommonObjects.Date.newBuilder()
                                                .setDay(DEPARTURE_DATE.getDayOfMonth())
                                                .setMonth(DEPARTURE_DATE.getMonthValue())
                                                .setYear(DEPARTURE_DATE.getYear())
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
                                .setCarrierCode(carrierCode)
                                .setFlightNumber(FLIGHT_NUMBER)
                                .setBoardingPoint(origin)
                                .setDeplaningPoint(destination)
                                .setDepartureDate(CommonObjects.Date.newBuilder()
                                                .setDay(DEPARTURE_DATE.getDayOfMonth())
                                                .setMonth(DEPARTURE_DATE.getMonthValue())
                                                .setYear(DEPARTURE_DATE.getYear())
                                                .build())
                                .build());
                flightsStub.deleteFlightDesignator(FlightDesignatorRequest.newBuilder()
                                .setCarrierCode(carrierCode)
                                .setFlightNumber(FLIGHT_NUMBER)
                                .setRevision(2)
                                .build());
                if (createdOrigin) flightsStub.deletePort(AirportCode.newBuilder()
                                .setAirportCode(origin).build());
                if (createdDestination) flightsStub.deletePort(AirportCode.newBuilder()
                                .setAirportCode(destination).build());
                // sleep to allow deleting of boarding passes for deleted flight to be processed
                try {
                        Thread.sleep(5 * 1000L);
                } catch (Exception e) {
                        e.printStackTrace();
                }
                if (createdCarrier) flightsStub.deleteCarrier(CarrierCode.newBuilder()
                                .setCarrierCode(carrierCode).build());
                templatesStub.deleteTemplate(templateId);
                imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(flightImageIds.getIcon()).build());
                imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(flightImageIds.getLogo()).build());
                imagesStub.deleteImage(CommonObjects.Id.newBuilder().setId(flightImageIds.getAppleLogo()).build());

                // always close the channel when there will be no further calls made.
                conn.closeChannel();

                // Shutdown if you are using the connection pool
                // shutdownPool();
        }

        public static void close() {
                if (conn != null) conn.closeChannel();
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
