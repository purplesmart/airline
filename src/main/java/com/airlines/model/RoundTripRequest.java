package com.airlines.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class RoundTripRequest {

    public String fromAirport;
    public String toAirport;

    @JsonFormat(pattern="yyyy-MM-dd")
    public String departureDate;

    @JsonFormat(pattern="yyyy-MM-dd")
    public String returnDate;
}
