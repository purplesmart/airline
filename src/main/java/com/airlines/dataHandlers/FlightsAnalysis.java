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

    public Itinerary[] getPriceWithConnections(String date, String fromAirport, String toAirport) {

        List<Itinerary> itineraries = new ArrayList<>();

        List<Flight> flightsFromOrigin = flightsRepository.findByDateAndFromAirport(date, fromAirport);

        itineraries.addAll(getDirectFlights(flightsFromOrigin, toAirport));
        itineraries.addAll(getConnectionFlights(flightsFromOrigin, toAirport));

        return itineraries.toArray(new Itinerary[itineraries.size()]);
    }

    private List<Itinerary> getDirectFlights(List<Flight> flightsFromOrigin, String toAirport) {
        return flightsFromOrigin.stream()
                .filter(flight -> flight.toAirport.equals(toAirport) && flight.availableSeats > 0)
                .map(Itinerary::new)
                .collect(Collectors.toList());
    }

    private List<Itinerary> getConnectionFlights(List<Flight> flightsFromOrigin, String toAirport) {
        return flightsFromOrigin.stream()
                .filter(flight -> !flight.toAirport.equals(toAirport) && flight.availableSeats > 0)
                .map(connectionFlight -> {
                    Date startDateWithDuration =
                            Date.from(connectionFlight.dateTimeUTC.toInstant()
                                    .plus(Duration.ofMinutes(connectionFlight.duration)));
                    Date endDateWithWaitFactor =
                            Date.from(startDateWithDuration.toInstant()
                                    .plus(Duration.ofMinutes(Integer.parseInt(flightInventoryConfiguration.getWaitingTimeBetweenFlights()))));
                    List<Flight> nextConnections =
                            flightsRepository
                                    .findByDateTimeUTCBetweenAndFromAirportAndToAirport(
                                            startDateWithDuration,
                                            endDateWithWaitFactor,
                                            connectionFlight.toAirport,
                                            toAirport);
                    if (nextConnections.size() > 0) {
                        return new Itinerary(connectionFlight, nextConnections);
                    }
                    return null;
                })
                .filter(itinerary -> itinerary != null)
                .collect(Collectors.toList());
    }


}
