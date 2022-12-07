package com.airlines.dataHandlers;

import com.airlines.configuration.FlightInventoryConfiguration;
import com.airlines.model.Flight;
import com.airlines.model.Itinerary;
import com.airlines.repositories.FlightsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class FlightsAnalysis {

    @Autowired
    private FlightsRepository flightsRepository;

    @Autowired
    private FlightInventoryConfiguration flightInventoryConfiguration;

    public FlightsAnalysis(FlightsRepository flightsRepository, FlightInventoryConfiguration flightInventoryConfiguration) {
        this.flightsRepository = flightsRepository;
        this.flightInventoryConfiguration = flightInventoryConfiguration;
    }

    public Itinerary[] getPriceRoundTrip(String departureDate, String fromAirport, String returnDate, String toAirport) {

        List<Flight> departureAndReturnFlights =
                getDepartureAndReturnFlights(departureDate, fromAirport, returnDate, toAirport);

        if (departureAndReturnFlights.size() > 0) {

            List<Itinerary> itineraries =
                    getDepartureAndReturnFlights(departureAndReturnFlights, departureDate, fromAirport, returnDate, toAirport);

            return itineraries.toArray(new Itinerary[itineraries.size()]);
        } else {
            return new Itinerary[]{};
        }
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

        Date earlyDate = getEarlyDateTimeFlightBound(flightsFromOrigin, toAirport);
        Date lateDate = getLateDateTimeFlightBound(flightsFromOrigin, toAirport);

        List<Flight> connectionFlights = flightsRepository
                .findByDateTimeUTCBetweenAndToAirportAndAvailableSeatsGreaterThanEqual(
                        earlyDate, lateDate, toAirport,
                        flightInventoryConfiguration.getMinimumAvailableSeats());

        return flightsFromOrigin.stream()
                .flatMap(departureFlight -> connectionFlights.stream()
                        .map(returnFlight ->
                                (isConnectionFlight(toAirport, departureFlight, returnFlight)) ?
                                        new Itinerary(departureFlight, returnFlight)
                                        : null))
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    private Date getEndDateWithWaitFactor(Date startDateWithDuration) {
        return Date.from(startDateWithDuration.toInstant()
                .plus(Duration.ofMinutes(flightInventoryConfiguration.getWaitingTimeBetweenFlights())));
    }

    private Date getStartDateWithDuration(Flight departureFlight) {
        return Date.from(departureFlight.dateTimeUTC.toInstant()
                .plus(Duration.ofMinutes(departureFlight.duration)));
    }

    private boolean isConnectionFlight(String toAirport, Flight departureFlight, Flight returnFlight) {
        Date startDateWithDuration = getStartDateWithDuration(departureFlight);
        Date endDateWithWaitFactor = getEndDateWithWaitFactor(startDateWithDuration);
        return returnFlight.dateTimeUTC.compareTo(startDateWithDuration) >= 0
                && returnFlight.dateTimeUTC.compareTo(endDateWithWaitFactor) <= 0
                && departureFlight.toAirport.equals(returnFlight.fromAirport)
                && returnFlight.toAirport.equals(toAirport);
    }

    private Date getLateDateTimeFlightBound(List<Flight> flightsFromOrigin, String toAirport) {
        OptionalLong lateDateAsLong = flightsFromOrigin.stream()
                .filter(flight -> !flight.toAirport.equals(toAirport))
                .mapToLong(connectionFlight -> Date.from(connectionFlight.dateTimeUTC.toInstant()
                        .plus(Duration.ofMinutes(connectionFlight.duration))).getTime())
                .max();
        if (lateDateAsLong.isPresent()) {
            return new Date(lateDateAsLong.getAsLong());
        } else {
            throw new IllegalArgumentException("Failed to compose late date time flight bound");
        }
    }

    private Date getEarlyDateTimeFlightBound(List<Flight> flightsFromOrigin, String toAirport) {
        OptionalLong earlyDateAsLong = flightsFromOrigin.stream()
                .filter(flight -> !flight.toAirport.equals(toAirport))
                .mapToLong(connectionFlight -> Date.from(connectionFlight.dateTimeUTC.toInstant()
                        .plus(Duration.ofMinutes(connectionFlight.duration))).getTime())
                .min();
        if (earlyDateAsLong.isPresent()) {
            return new Date(earlyDateAsLong.getAsLong());
        } else {
            throw new IllegalArgumentException("Failed to compose early date time flight bound");
        }
    }


    public Itinerary[] getPriceAllRoundTrip(String fromAirport, String toAirport) {

        List<Itinerary> itineraries = new ArrayList<>();

        List<Flight> flights = getFlightByDestinations(fromAirport, toAirport);

        if (flights.size() > 0) {

            List<Flight> returnFlights = getReturnFlights(flights, fromAirport, toAirport);
            List<Flight> departureFlights = getDepartureFlights(flights, returnFlights, fromAirport, toAirport);

            Itinerary departureItinerary = new Itinerary(departureFlights);
            itineraries.add(departureItinerary);
            Itinerary returnItinerary = new Itinerary(returnFlights);
            itineraries.add(returnItinerary);
            return itineraries.toArray(new Itinerary[itineraries.size()]);
        } else {
            return new Itinerary[]{};
        }
    }

    private List<Flight> getFlightByDestinations(String fromAirport, String toAirport) {
        String[] destinations = new String[]{fromAirport, toAirport};
        return flightsRepository.findByFromAirportInAndToAirportInAndAvailableSeatsGreaterThanEqualOrderByDate(destinations, destinations, flightInventoryConfiguration.getMinimumAvailableSeats());
    }

    private List<Flight> getReturnFlights(List<Flight> flights, String fromAirport, String toAirport) {
        Optional<Flight> earliestDepartureFlight =
                flights.stream()
                        .filter(flight ->
                                flight.fromAirport.equals(fromAirport)
                                        && flight.toAirport.equals(toAirport)).findFirst();

        if (earliestDepartureFlight.isEmpty()) {
            throw new NoSuchElementException(String.format("No flights from %s to %s found", fromAirport, toAirport));
        }

        Date returnFlightStartDate =
                Date.from(earliestDepartureFlight.get().dateTimeUTC.toInstant()
                        .plus(Duration.ofDays(flightInventoryConfiguration.getMinimumDaysForRoundTrip())));

        return flights.stream()
                .filter(flight ->
                        flight.fromAirport.equals(toAirport)
                                && flight.toAirport.equals(fromAirport)
                                && Date.from(flight.dateTimeUTC.toInstant()).after(returnFlightStartDate))
                .collect(Collectors.toList());
    }

    private List<Flight> getDepartureFlights(List<Flight> flights, List<Flight> returnFlights, String fromAirport, String toAirport) {

        Optional<Flight> latestReturnFlight =
                returnFlights.stream()
                        .reduce((first, second) -> second);

        if (latestReturnFlight.isEmpty()) {
            throw new NoSuchElementException(String.format("No flights from %s to %s found", fromAirport, toAirport));
        }

        Date departureFlightEndDate =
                Date.from(latestReturnFlight.get().dateTimeUTC.toInstant()
                        .minus(Duration.ofDays(flightInventoryConfiguration.getMinimumDaysForRoundTrip())));

        return flights.stream()
                .filter(flight ->
                        flight.fromAirport.equals(fromAirport)
                                && flight.toAirport.equals(toAirport)
                                && Date.from(flight.dateTimeUTC.toInstant()).before(departureFlightEndDate))
                .collect(Collectors.toList());
    }


}
