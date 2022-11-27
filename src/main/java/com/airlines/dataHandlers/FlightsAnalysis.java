package com.airlines.dataHandlers;

import com.airlines.configuration.FlightInventoryConfiguration;
import com.airlines.model.Flight;
import com.airlines.model.Itinerary;
import com.airlines.repositories.FlightsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FlightsAnalysis {

    @Autowired
    private FlightsRepository flightsRepository;

    @Autowired
    private FlightInventoryConfiguration flightInventoryConfiguration;


    public Itinerary[] getPriceRoundTrip(String departureDate, String fromAirport, String returnDate, String toAirport) {

        List<Flight> departureAndReturnFlights =
                getDepartureAndReturnFlights(departureDate, fromAirport, returnDate, toAirport);
        List<Itinerary> itineraries =
                getDepartureAndReturnFlights(departureAndReturnFlights, departureDate, fromAirport, returnDate, toAirport);

        return itineraries.toArray(new Itinerary[itineraries.size()]);
    }

    private List<Itinerary> getDepartureAndReturnFlights(List<Flight> departureAndReturnFlights, String departureDate, String fromAirport, String returnDate, String toAirport) {

        List<Itinerary> itineraries = new ArrayList<>();

        Itinerary departureItinerary = getDepartureItinerary(departureAndReturnFlights, departureDate, fromAirport, toAirport);

        itineraries.add(departureItinerary);

        Itinerary returnItinerary = getDepartureItinerary(departureAndReturnFlights, returnDate, toAirport, fromAirport);

        itineraries.add(returnItinerary);
        return itineraries;
    }

    private Itinerary getDepartureItinerary(List<Flight> departureAndReturnFlights,
                                            String departureDate, String fromAirport, String toAirport) {
        return new Itinerary(
                departureAndReturnFlights
                        .stream()
                        .filter(flight ->
                                flight.date.equals(departureDate)
                                        && flight.fromAirport.equals(fromAirport)
                                        && flight.toAirport.equals(toAirport))
                        .collect(Collectors.toList()));
    }

    private List<Flight> getDepartureAndReturnFlights(String departureDate, String fromAirport, String returnDate, String toAirport) {

        String[] dates = new String[]{departureDate, returnDate};
        String[] destinations = new String[]{fromAirport, toAirport};

        return flightsRepository
                .findByDateInAndFromAirportInAndToAirportInAndAvailableSeatsGreaterThanEqual(dates, destinations, destinations, flightInventoryConfiguration.getMinimumAvailableSeats());
    }


    public Itinerary[] getPriceWithConnections(String date, String fromAirport, String toAirport) {

        List<Itinerary> itineraries = new ArrayList<>();

        List<Flight> flightsFromOrigin = flightsRepository.findByDateAndFromAirportAndAvailableSeatsGreaterThanEqual(date, fromAirport, flightInventoryConfiguration.getMinimumAvailableSeats());

        itineraries.addAll(getDirectFlights(flightsFromOrigin, toAirport));
        itineraries.addAll(getConnectionFlights(flightsFromOrigin, toAirport));

        return itineraries.toArray(new Itinerary[itineraries.size()]);
    }

    private List<Itinerary> getDirectFlights(List<Flight> flightsFromOrigin, String toAirport) {
        return flightsFromOrigin.stream()
                .filter(flight -> flight.toAirport.equals(toAirport))
                .map(Itinerary::new)
                .collect(Collectors.toList());
    }

    private List<Itinerary> getConnectionFlights(List<Flight> flightsFromOrigin, String toAirport) {
        return flightsFromOrigin.stream()
                .filter(flight -> !flight.toAirport.equals(toAirport))
                .map(connectionFlight -> {
                    Date startDateWithDuration =
                            Date.from(connectionFlight.dateTimeUTC.toInstant()
                                    .plus(Duration.ofMinutes(connectionFlight.duration)));
                    Date endDateWithWaitFactor =
                            Date.from(startDateWithDuration.toInstant()
                                    .plus(Duration.ofMinutes(flightInventoryConfiguration.getWaitingTimeBetweenFlights())));
                    List<Flight> nextConnections =
                            flightsRepository
                                    .findByDateTimeUTCBetweenAndFromAirportAndToAirportAndAvailableSeatsGreaterThanEqual(
                                            startDateWithDuration,
                                            endDateWithWaitFactor,
                                            connectionFlight.toAirport,
                                            toAirport,
                                            flightInventoryConfiguration.getMinimumAvailableSeats());
                    if (nextConnections.size() > 0) {
                        return new Itinerary(connectionFlight, nextConnections);
                    }
                    return null;
                })
                .filter(itinerary -> itinerary != null)
                .collect(Collectors.toList());
    }



    public Itinerary[] getPriceAllRoundTrip(String fromAirport, String toAirport) {

        List<Itinerary> itineraries = new ArrayList<>();

        List<Flight> flights = getFlightByyDestinations(fromAirport, toAirport);

        List<Flight> returnFlights = getReturnFlights(flights, fromAirport, toAirport);

        List<Flight> departureFlights = getDepartureFlights(flights, returnFlights, fromAirport, toAirport);

        Itinerary departureItinerary = new Itinerary(departureFlights);
        itineraries.add(departureItinerary);
        Itinerary returnItinerary = new Itinerary(returnFlights);
        itineraries.add(returnItinerary);
        return itineraries.toArray(new Itinerary[itineraries.size()]);
    }

    private List<Flight> getFlightByyDestinations(String fromAirport, String toAirport) {
        String[] destinations = new String[]{fromAirport, toAirport};
        return flightsRepository.findByFromAirportInAndToAirportInAndAvailableSeatsGreaterThanEqualOrderByDate(destinations, destinations, flightInventoryConfiguration.getMinimumAvailableSeats());
    }

    private List<Flight> getReturnFlights(List<Flight> flights, String fromAirport, String toAirport) {
        Flight earliestDepartureFlight =
                flights.stream()
                        .filter(flight ->
                                flight.fromAirport.equals(fromAirport)
                                        && flight.toAirport.equals(toAirport)).findFirst().get();

        Date returnFlightStartDate =
                Date.from(earliestDepartureFlight.dateTimeUTC.toInstant()
                        .plus(Duration.ofDays(flightInventoryConfiguration.getMinimumDaysForRoundTrip())));

        return flights.stream()
                .filter(flight ->
                        flight.fromAirport.equals(toAirport)
                                && flight.toAirport.equals(fromAirport)
                                && Date.from(flight.dateTimeUTC.toInstant()).after(returnFlightStartDate))
                .collect(Collectors.toList());
    }

    private List<Flight> getDepartureFlights(List<Flight> flights, List<Flight> returnFlights, String fromAirport, String toAirport) {
        Flight latestReturnFlight =
                returnFlights.stream()
                        .reduce((first, second) -> second).get();

        Date departureFlightEndDate =
                Date.from(latestReturnFlight.dateTimeUTC.toInstant()
                        .minus(Duration.ofDays(flightInventoryConfiguration.getMinimumDaysForRoundTrip())));

        return flights.stream()
                .filter(flight ->
                        flight.fromAirport.equals(fromAirport)
                                && flight.toAirport.equals(toAirport)
                                && Date.from(flight.dateTimeUTC.toInstant()).before(departureFlightEndDate))
                .collect(Collectors.toList());
    }


}
