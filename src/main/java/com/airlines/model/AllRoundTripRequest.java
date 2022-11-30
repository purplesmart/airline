package com.airlines.model;

import javax.validation.constraints.Size;

public class AllRoundTripRequest {

    @Size(min = 3, max = 3, message = "{Departure airport code is not valid}")
    public String fromAirport;

    @Size(min = 3, max = 3, message = "{Destination airport code is not valid}")
    public String toAirport;
}
