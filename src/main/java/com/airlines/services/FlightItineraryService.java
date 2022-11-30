package com.airlines.services;

import com.airlines.dataHandlers.FlightsAnalysis;
import com.airlines.model.Itinerary;
import com.airlines.validators.DateValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import java.text.ParseException;

@Service
@Validated
public class FlightItineraryService {

    @Autowired
    private FlightsAnalysis flightsAnalysis;

    @Autowired
    private DateValidator dateValidator;

    public FlightItineraryService(FlightsAnalysis flightsAnalysis, DateValidator dateValidator) {
        this.flightsAnalysis = flightsAnalysis;
        this.dateValidator = dateValidator;
    }

    public Itinerary[] getPriceRoundTrip(String departureDate, String fromAirport, String returnDate, String toAirport) {
        dateValidator.validateDateRange(departureDate, returnDate);
        return flightsAnalysis.getPriceRoundTrip(departureDate, fromAirport, returnDate, toAirport);
    }

    public Itinerary[] getPriceWithConnections(String departureDate, String fromAirport, String toAirport) {
        dateValidator.validateDepartureDate(departureDate);
        return flightsAnalysis.getPriceWithConnections(departureDate, fromAirport, toAirport);
    }

    public Itinerary[] getPriceAllRoundTrip(String fromAirport, String toAirport) {
        return flightsAnalysis.getPriceAllRoundTrip(fromAirport, toAirport);
    }
}
