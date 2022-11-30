package com.airlines.dataHandlers;

import com.airlines.configuration.FlightInventoryConfiguration;
import com.airlines.model.Flight;
import com.airlines.repositories.FlightsRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.ArrayList;
import java.util.List;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FlightsAnalysisTest {

    private FlightsAnalysis flightsAnalysis;

    @MockBean
    private FlightsRepository flightsRepository;

    @MockBean
    private FlightInventoryConfiguration flightInventoryConfiguration;

    @BeforeAll
    public void setUp() {
        flightsAnalysis = new FlightsAnalysis(flightsRepository, flightInventoryConfiguration);
        Mockito.when(flightInventoryConfiguration.getMinimumAvailableSeats())
                .thenReturn(1);
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

    @Test
    void getPriceWithConnections() {

        List<Flight> flightList = new ArrayList<>();

        String departureDate = "";
        String fromAirport = "";
        String toAirport = "";

        Mockito.when(flightsRepository
                        .findByDateAndFromAirportAndAvailableSeatsGreaterThanEqual(
                                departureDate,
                                fromAirport,
                                flightInventoryConfiguration.getMinimumAvailableSeats()))
                .thenReturn(flightList);

        flightsAnalysis.getPriceWithConnections(departureDate, fromAirport, toAirport);

        verify(flightsRepository, times(1))
                .findByDateAndFromAirportAndAvailableSeatsGreaterThanEqual(
                        departureDate,
                        fromAirport,
                        flightInventoryConfiguration.getMinimumAvailableSeats());
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
}