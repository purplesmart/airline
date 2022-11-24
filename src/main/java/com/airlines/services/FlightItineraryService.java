package com.airlines.services;

import com.airlines.dataHandlers.FlightsAnalysis;
import com.airlines.model.Itinerary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FlightItineraryService {

    @Autowired
    private FlightsAnalysis flightsAnalyze;


    public Itinerary[] getPriceWithConnections( String date, String fromAirport,String toAirport) {

        try {
            return flightsAnalyze.getPriceWithConnections(date, fromAirport, toAirport);
        } catch (Exception ex) {
            return null;
        }
    }


}
