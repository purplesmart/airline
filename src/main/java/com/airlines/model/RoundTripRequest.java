package com.airlines.model;

import javax.validation.constraints.Size;
import javax.validation.constraints.Pattern;

public class RoundTripRequest {

    @Size(min = 3, max = 3, message = "{Departure airport code is not valid}")
    public String fromAirport;

    @Size(min = 3, max = 3, message = "{Destination airport code is not valid}")
    public String toAirport;

    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "{Departure date format is not valid}")
    public String departureDate;

    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "{Destination date format is not valid}")
    public String returnDate;
}
