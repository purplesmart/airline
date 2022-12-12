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

        List<Flight> departureFlights = getDepartureFlights(departureAndReturnFlights, departureDate, fromAirport, toAirport);
        List<Flight>  returnFlights = getDepartureFlights(departureAndReturnFlights, returnDate, toAirport, fromAirport);

        if(departureFlights.size() > 0 && returnFlights.size() > 0) {
           return departureFlights.stream().flatMap(departureFlight ->
                    returnFlights.stream().map(returnFlight ->
                            new Itinerary(departureFlight, returnFlight))).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private List<Flight> getDepartureFlights(List<Flight> departureAndReturnFlights,
                                               String departureDate, String fromAirport, String toAirport) {
        return departureAndReturnFlights
                .stream()
                .filter(flight ->
                        flight.date.equals(departureDate)
                                && flight.fromAirport.equals(fromAirport)
                                && flight.toAirport.equals(toAirport))
                .collect(Collectors.toList());
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

        if(flightsFromOrigin.size() > 0 ) {

            itineraries.addAll(getDirectFlights(flightsFromOrigin, toAirport));
            itineraries.addAll(getConnectionFlights(flightsFromOrigin, toAirport));

        }

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

        if(earlyDate != null && lateDate != null) {

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
        }else{
            return new ArrayList<>();
        }
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
            Date lateDate = new Date(lateDateAsLong.getAsLong());
            return Date.from(lateDate.toInstant()
                    .plus(Duration.ofMinutes(flightInventoryConfiguration.getWaitingTimeBetweenFlights())));
        } else {
            return null;
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
            return null;
        }
    }


    public Itinerary[] getPriceAllRoundTrip(String fromAirport, String toAirport) {

        List<Flight> flights = getFlightByDestinations(fromAirport, toAirport);

        if (flights.size() > 0) {

            List<Flight> returnFlights = getReturnFlights(flights, fromAirport, toAirport);
            List<Flight> departureFlights = getDepartureFlights(flights, returnFlights, fromAirport, toAirport);

            return departureFlights.stream()
                    .flatMap(departureFlight -> returnFlights.stream()
                            .map(returnFlight ->
                                    (isRoundFlight(departureFlight, returnFlight)) ?
                                            new Itinerary(departureFlight, returnFlight)
                                            : null))
                    .filter(Objects::nonNull).toArray(Itinerary[]::new);
        } else {
            return new Itinerary[]{};
        }
    }

    private boolean isRoundFlight(Flight departureFlight, Flight returnFlight) {

        Date startValidReturnDate = getValidReturnDateWithDurationAndMinimumDays(departureFlight);
        return returnFlight.dateTimeUTC.compareTo(startValidReturnDate) >= 0
                && departureFlight.toAirport.equals(returnFlight.fromAirport)
                && returnFlight.toAirport.equals(departureFlight.fromAirport);
    }

    private Date getValidReturnDateWithDurationAndMinimumDays(Flight departureFlight) {
        return Date.from(departureFlight.dateTimeUTC.toInstant()
                .plus(Duration.ofMinutes(departureFlight.duration))
                .plus(Duration.ofDays(flightInventoryConfiguration.getMinimumDaysForRoundTrip())));
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
            return new ArrayList<>();
        }

        Date returnFlightStartDate =
                Date.from(earliestDepartureFlight.get().dateTimeUTC.toInstant()
                        .plus(Duration.ofDays(flightInventoryConfiguration.getMinimumDaysForRoundTrip())));

        return flights.stream()
                .filter(flight ->
                        isValidReturnFlight(flight, fromAirport, toAirport, returnFlightStartDate))
                .collect(Collectors.toList());
    }

    private boolean isValidReturnFlight(Flight flight, String fromAirport, String toAirport, Date returnFlightStartDate) {
        return flight.fromAirport.equals(toAirport)
                && flight.toAirport.equals(fromAirport)
                && Date.from(flight.dateTimeUTC.toInstant()).after(returnFlightStartDate);
    }

    private List<Flight> getDepartureFlights(List<Flight> flights, List<Flight> returnFlights, String fromAirport, String toAirport) {

        Optional<Flight> latestReturnFlight =
                returnFlights.stream()
                        .reduce((first, second) -> second);

        if (latestReturnFlight.isEmpty()) {
            return new ArrayList<>();
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
