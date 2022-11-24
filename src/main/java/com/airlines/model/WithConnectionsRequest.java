package com.airlines.model;

import com.fasterxml.jackson.annotation.JsonFormat;

public class WithConnectionsRequest {
    public String fromAirport;
    public String toAirport;

    @JsonFormat(pattern="yyyy-MM-dd")
    public String date;
}
