package com.airlines.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@IdClass(FlightCompositeKey.class)
public class Flight {

    @Id
    public String flightNumber;

    @Id
    public String date;

    public String fromAirport;

    public String toAirport;

    public String departureDate;

    public int duration;

    public double price;

    public int availableSeats;

    @Temporal(TemporalType.TIMESTAMP)
    public Date dateTimeUTC;


}
