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
    private String waitingTimeBetweenFlights;

    public String getFlightResourcePath(){
        return flightResourcePath;
    }

    public String getPriceResourcePath(){
        return priceResourcePath;
    }

    public String getWaitingTimeBetweenFlights(){
        return waitingTimeBetweenFlights;
    }
}
