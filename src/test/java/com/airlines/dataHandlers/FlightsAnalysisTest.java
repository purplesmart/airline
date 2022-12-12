package com.airlines.dataHandlers;

import com.airlines.configuration.FlightInventoryConfiguration;
import com.airlines.model.Flight;
import com.airlines.model.Itinerary;
import com.airlines.repositories.FlightsRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FlightsAnalysisTest {

    private final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter
                    .ofPattern("uuuu-MM-dd'T'HH:mmXXXXX");
    private int flightNumber  = 1;

    private FlightsAnalysis flightsAnalysis;

    @MockBean
    private FlightsRepository flightsRepository;

    @MockBean
    private FlightInventoryConfiguration flightInventoryConfiguration;

    @BeforeAll
    public void setUp() {
        flightsAnalysis = new FlightsAnalysis(flightsRepository, flightInventoryConfiguration);
    }

    @Test
    void getPriceRoundTrip() {

        List<Flight> flightList = new ArrayList<>();

        String departureDate = "";
        String fromAirport = "";
        String returnDate = "";
        String toAirport = "";

        String[] dates = new String[]{departureDate,returnDate };
        String[] destinations = new String[]{fromAirport, toAirport};

        Mockito.when(flightsRepository
                        .findByDateInAndFromAirportInAndToAirportInAndAvailableSeatsGreaterThanEqual(
                                dates,
                                destinations,
                                destinations,
                                flightInventoryConfiguration.getMinimumAvailableSeats()))
                .thenReturn(flightList);

        flightsAnalysis.getPriceRoundTrip(departureDate, fromAirport, returnDate, toAirport);

        verify(flightsRepository, times(1))
                .findByDateInAndFromAirportInAndToAirportInAndAvailableSeatsGreaterThanEqual(
                dates,
                destinations,
                destinations,
                flightInventoryConfiguration.getMinimumAvailableSeats());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value =
            {
                    "2022-09-14 | TLV |2022-09-18 | JFK | 1",
                    "2022-09-14 | TLV |2022-09-18 | MED | 2",
                    "2022-09-15 | TLV |2022-09-18 | MED | 0",
                    "2022-09-14 | JFK |2022-09-18 | MED | 0",
                    "2022-09-14 | TLV |2022-09-18 | MED | 2",
                    "2022-09-14 | TLV |2022-09-19 | MED | 0",
                    "2022-09-14 | TLV |2022-09-18 | MEX | 0"})
    void getPriceRoundTripAnalyse(String departureDate, String fromAirport, String returnDate, String toAirport, int itinerariesCount) {
        buildFlightInventoryConfiguration();
        buildPriceRoundTripRepositoryMockResponse();
        Itinerary[] itineraries = flightsAnalysis.getPriceRoundTrip(departureDate, fromAirport, returnDate, toAirport);
        Assertions.assertEquals(itinerariesCount, itineraries.length);
    }

    private void buildPriceRoundTripRepositoryMockResponse() {

        String departureDate = "2022-09-14";
        String fromAirport = "TLV";
        String returnDate = "2022-09-18";
        String toAirport = "JFK";
        String anotherToAirport = "MED";
        String dateTimeUTC = "09:00Z";

        Mockito.when(flightsRepository
                        .findByDateInAndFromAirportInAndToAirportInAndAvailableSeatsGreaterThanEqual(
                                new String[]{departureDate, returnDate},
                                new String[]{fromAirport, toAirport},
                                new String[]{fromAirport, toAirport},
                                flightInventoryConfiguration.getMinimumAvailableSeats()))
                .thenReturn(Arrays.asList(
                        getFlight(departureDate, fromAirport, toAirport, dateTimeUTC),
                        getFlight(returnDate, toAirport, fromAirport, dateTimeUTC)));

        Mockito.when(flightsRepository
                        .findByDateInAndFromAirportInAndToAirportInAndAvailableSeatsGreaterThanEqual(
                                new String[]{departureDate, returnDate},
                                new String[]{fromAirport, anotherToAirport},
                                new String[]{fromAirport, anotherToAirport},
                                flightInventoryConfiguration.getMinimumAvailableSeats()))
                .thenReturn(Arrays.asList(
                        getFlight(departureDate, fromAirport, anotherToAirport, dateTimeUTC),
                        getFlight(returnDate, anotherToAirport, fromAirport, dateTimeUTC),
                        getFlight(returnDate, anotherToAirport, fromAirport, dateTimeUTC)));
    }

    @Test
    void getPriceWithConnections() {

        List<Flight> flightList = new ArrayList<>();

        String departureDate = "";
        String fromAirport = "";
        String toAirport = "";

        Mockito.when(flightsRepository
                        .findByDateAndFromAirportAndAvailableSeatsGreaterThanEqual(departureDate, fromAirport,
                                flightInventoryConfiguration.getMinimumAvailableSeats()))
                .thenReturn(flightList);

        flightsAnalysis.getPriceWithConnections(departureDate, fromAirport, toAirport);

        verify(flightsRepository, times(1))
                .findByDateAndFromAirportAndAvailableSeatsGreaterThanEqual(departureDate, fromAirport,
                        flightInventoryConfiguration.getMinimumAvailableSeats());
    }

    void buildFlightInventoryConfiguration() {
        Mockito.when(flightInventoryConfiguration.getMinimumAvailableSeats())
                .thenReturn(1);
        Mockito.when(flightInventoryConfiguration.getWaitingTimeBetweenFlights())
                .thenReturn(360);
        Mockito.when(flightInventoryConfiguration.getMinimumDaysForRoundTrip())
                .thenReturn(1);
    }

    private void buildPriceWithConnectionsRepositoryMockResponse() {

        String departureDate = "2022-09-14";
        String fromAirport = "TLV";
        String toAirport = "JFK";
        String toConnectionAirport = "MEX";
        String dateTimeUTC = "09:00Z";
        String connectionDateTimeUTC = "16:00Z";

        buildFindByDateAndFromAirportAndAvailableSeatsGreaterThanEqual(
                departureDate, dateTimeUTC, fromAirport, toAirport, toConnectionAirport);

        buildFindByDateTimeUTCBetweenAndToAirportAndAvailableSeatsGreaterThanEqual(
                departureDate, dateTimeUTC, toAirport, toConnectionAirport, connectionDateTimeUTC);

    }

    private void buildFindByDateAndFromAirportAndAvailableSeatsGreaterThanEqual(
            String departureDate, String dateTimeUTC, String fromAirport,String toAirport,String toConnectionAirport){
        Mockito.when(flightsRepository
                        .findByDateAndFromAirportAndAvailableSeatsGreaterThanEqual(departureDate, fromAirport,
                                flightInventoryConfiguration.getMinimumAvailableSeats()))
                .thenReturn(Arrays.asList(
                        getFlight(departureDate, fromAirport, toConnectionAirport, dateTimeUTC),
                        getFlight(departureDate, fromAirport, toAirport, dateTimeUTC)));
    }

    private void buildFindByDateTimeUTCBetweenAndToAirportAndAvailableSeatsGreaterThanEqual(
            String departureDate, String dateTimeUTC,String toAirport,String toConnectionAirport,String connectionDateTimeUTC) {
        Date departureDateTimeUTC = Date.from(OffsetDateTime
                .parse(departureDate + "T" + dateTimeUTC, DATE_TIME_FORMATTER)
                .withOffsetSameInstant(ZoneOffset.UTC).toInstant());

        Date startDate = Date.from(departureDateTimeUTC.toInstant().plus(Duration.ofMinutes(300)));
        Date endDate = Date.from(startDate.toInstant().plus(Duration.ofMinutes(360)));

        Mockito.when(flightsRepository
                        .findByDateTimeUTCBetweenAndToAirportAndAvailableSeatsGreaterThanEqual(
                                startDate, endDate, toAirport,
                                flightInventoryConfiguration.getMinimumAvailableSeats()))
                .thenReturn(List.of(
                        getFlight(departureDate, toConnectionAirport, toAirport, connectionDateTimeUTC)));
    }

    private Flight getFlight(String date, String fromAirport, String toAirport, String departureDate){

        if(date == null || fromAirport == null || toAirport == null || departureDate == null){
            return null;
        }

        Flight flight = new Flight();
        flightNumber += 1;
        flight.flightNumber = String.valueOf(flightNumber);
        flight.date = date;
        flight.fromAirport = fromAirport;
        flight.toAirport = toAirport;
        flight.departureDate = departureDate;
        flight.duration = 300;
        flight.price = 1;
        flight.availableSeats = 1;
        flight.dateTimeUTC = Date.from(OffsetDateTime
                .parse(flight.date + "T" + flight.departureDate, DATE_TIME_FORMATTER)
                .withOffsetSameInstant(ZoneOffset.UTC).toInstant());

        return flight;
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value =
            {
                    "2022-09-14 | TLV | MEX | 1",
                    "2022-09-14 | MED | MEX | 0",
                    "2022-09-14 | TLV | MED | 0",
                    "2022-09-14 | TLV | JFK | 2"})
    void getPriceWithConnectionsAnalyse(String departureDate,String fromAirport,String toAirport, int itinerariesCount) {

        buildFlightInventoryConfiguration();
        buildPriceWithConnectionsRepositoryMockResponse();

        Itinerary[] itineraries = flightsAnalysis.getPriceWithConnections(departureDate, fromAirport, toAirport);

        Assertions.assertEquals(itinerariesCount, itineraries.length);

    }


    @Test
    void getPriceAllRoundTrip() {

        List<Flight> flightList = new ArrayList<>();

        String fromAirport = "";
        String toAirport = "";

        String[] destinations = new String[]{fromAirport, toAirport};

        Mockito.when(flightsRepository
                        .findByFromAirportInAndToAirportInAndAvailableSeatsGreaterThanEqualOrderByDate(
                                destinations,
                                destinations,
                                flightInventoryConfiguration.getMinimumAvailableSeats()))
                .thenReturn(flightList);

        flightsAnalysis.getPriceAllRoundTrip(fromAirport, toAirport);

        verify(flightsRepository, times(1))
                .findByFromAirportInAndToAirportInAndAvailableSeatsGreaterThanEqualOrderByDate(
                        destinations,
                        destinations,
                        flightInventoryConfiguration.getMinimumAvailableSeats());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value =
            {
                    "2022-09-14 |TLV |2022-09-16 | JFK |  | | 1",
                    "2022-09-14 |TLV |2022-09-14 | JFK |  | | 0",
                    "2022-09-14 |TLV |2022-09-16 | JFK | 2022-09-17 | | 2",
                    "2022-09-14 |TLV |2022-09-14 | JFK | 2022-09-17 | | 1",
                    "2022-09-14 |TLV |2022-09-14 | JFK | 2022-09-17 | 2022-09-18| 2",
                    "2022-09-14 |TLV |2022-09-16 | JFK | 2022-09-17 | 2022-09-18| 3"})
    void getPriceAllRoundTripAnalyse(String departureDate, String fromAirport, String returnDate, String toAirport, String anotherReturnDate, String yetAnotherReturnDate, int itinerariesCount) {
        buildFlightInventoryConfiguration();
        buildPriceAllRoundTripRepositoryMockResponse(departureDate, fromAirport, returnDate, toAirport,anotherReturnDate,  yetAnotherReturnDate);

        Itinerary[] itineraries = flightsAnalysis.getPriceAllRoundTrip(fromAirport, toAirport);

        Assertions.assertEquals(itinerariesCount, itineraries.length);
    }

    private void buildPriceAllRoundTripRepositoryMockResponse(String departureDate, String fromAirport, String returnDate, String toAirport, String anotherReturnDate, String yetAnotherReturnDate) {
        String dateTimeUTC = "09:00Z";

        String[] fromAirports = new String[]{fromAirport, toAirport};
        String[] toAirports = new String[]{fromAirport, toAirport};

        Mockito.when(flightsRepository
                        .findByFromAirportInAndToAirportInAndAvailableSeatsGreaterThanEqualOrderByDate(fromAirports, toAirports,
                                flightInventoryConfiguration.getMinimumAvailableSeats()))
                .thenReturn(Stream.of(
                                getFlight(departureDate, fromAirport, toAirport, dateTimeUTC),
                                getFlight(returnDate, toAirport, fromAirport, dateTimeUTC),
                                getFlight(anotherReturnDate, toAirport, fromAirport, dateTimeUTC),
                                getFlight(yetAnotherReturnDate, toAirport, fromAirport, dateTimeUTC))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
    }

}