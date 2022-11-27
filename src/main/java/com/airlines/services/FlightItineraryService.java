package com.airlines.services;

import com.airlines.dataHandlers.FlightsAnalysis;
import com.airlines.model.Itinerary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FlightItineraryService {

    @Autowired
    private FlightsAnalysis flightsAnalysis;

    public Itinerary[] getPriceRoundTrip(String departureDate, String fromAirport, String returnDate, String toAirport) {

        try {
            return flightsAnalysis.getPriceRoundTrip(departureDate, fromAirport, returnDate, toAirport);
        } catch (Exception ex) {
            return null;
        }
    }

    public Itinerary[] getPriceWithConnections( String date, String fromAirport,String toAirport) {

        try {
            return flightsAnalysis.getPriceWithConnections(date, fromAirport, toAirport);
        } catch (Exception ex) {
            return null;
        }
    }

    public Itinerary[] getPriceAllRoundTrip(
            String fromAirport,
            String toAirport) {
        try {
            return flightsAnalysis.getPriceAllRoundTrip(
                    fromAirport,
                    toAirport);
        } catch (Exception ex) {
            return null;
        }
    }

}
