package com.airlines.services;

import com.airlines.dataHandlers.FlightsAnalysis;
import com.airlines.model.Itinerary;
import com.airlines.validators.DateValidator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.text.ParseException;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FlightItineraryServiceTest {

    private FlightItineraryService flightItineraryService;

    @MockBean
    private FlightsAnalysis flightsAnalysis;

    @MockBean
    private DateValidator dateValidator;

    @BeforeAll
    public void setUp() {
        flightItineraryService = new FlightItineraryService(flightsAnalysis, dateValidator);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value =
            {"2022-01-12| JFK| 2022-01-14| MEX"})
    void getPriceRoundTripHappyPath(String departureDate, String fromAirport, String returnDate, String toAirport)  {

        Mockito.when(flightsAnalysis
                        .getPriceRoundTrip(departureDate, fromAirport, returnDate, toAirport))
                .thenReturn(new Itinerary[0]);

        flightItineraryService.getPriceRoundTrip(departureDate, fromAirport, returnDate, toAirport);

        verify(dateValidator, times(1))
                .validateDateRange(departureDate, returnDate);
        verify(flightsAnalysis, times(1))
                .getPriceRoundTrip(departureDate, fromAirport, returnDate, toAirport);
    }


    @ParameterizedTest
    @CsvSource(delimiter = '|', value =
            {"2022-08-14| TLV| MEX"})
    void getPriceWithConnections(String departureDate, String fromAirport, String toAirport) {
        Mockito.when(flightsAnalysis
                        .getPriceWithConnections(departureDate, fromAirport, toAirport))
                .thenReturn(new Itinerary[0]);

        flightItineraryService.getPriceWithConnections(departureDate, fromAirport, toAirport);

        verify(dateValidator, times(1))
                .validateDepartureDate(departureDate);
        verify(flightsAnalysis, times(1))
                .getPriceWithConnections(departureDate, fromAirport, toAirport);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value =
            {"JFK| MEX"})
    void getPriceAllRoundTrip(String fromAirport, String toAirport) {
        Mockito.when(flightsAnalysis
                        .getPriceAllRoundTrip(fromAirport, toAirport))
                .thenReturn(new Itinerary[0]);

        flightItineraryService.getPriceAllRoundTrip(fromAirport, toAirport);

        verify(flightsAnalysis, times(1))
                .getPriceAllRoundTrip(fromAirport, toAirport);
    }
}