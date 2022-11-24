package com.airlines.model;

import java.util.List;

public class Itinerary {
    public Flight[] flightsItinerary;

    public Itinerary(Flight flight){
        flightsItinerary = new Flight[1];
        flightsItinerary[0] = flight;
    }

    public Itinerary(Flight flightStart, Flight flightConnection){
        flightsItinerary = new Flight[2];
        flightsItinerary[0] = flightStart;
        flightsItinerary[1] = flightConnection;
    }

    public Itinerary(List<Flight> flights){
        flightsItinerary =  flights.toArray(new Flight[flights.size()]);
    }

    public Itinerary(Flight flightStart,List<Flight> connections){
        flightsItinerary =  connections.toArray(new Flight[connections.size() + 1]);
        flightsItinerary[flightsItinerary.length -1] = flightStart;
    }
}
