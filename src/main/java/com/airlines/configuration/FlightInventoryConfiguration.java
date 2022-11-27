package com.airlines.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlightInventoryConfiguration {

    @Value( "${flightresourcepath}" )
    private String flightResourcePath;

    @Value( "${priceresourcepath}" )
    private String priceResourcePath;

    @Value( "${waitingtimebetweenflights}" )
    private int waitingTimeBetweenFlights;

    @Value( "${minimumavailableseats}" )
    private int minimumAvailableSeats;

    @Value( "${minimumdaysforroundtrip}" )
    private int minimumDaysForRoundTrip;

    public String getFlightResourcePath(){
        return flightResourcePath;
    }

    public String getPriceResourcePath(){
        return priceResourcePath;
    }

    public int getWaitingTimeBetweenFlights(){
        return waitingTimeBetweenFlights;
    }

    public int getMinimumAvailableSeats(){
        return minimumAvailableSeats;
    }

    public int getMinimumDaysForRoundTrip(){
        return minimumDaysForRoundTrip;
    }

}
