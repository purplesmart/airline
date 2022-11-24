package com.airlines.parsers;

import com.airlines.configuration.FlightInventoryConfiguration;
import com.airlines.model.Flight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class FlightParser {

    DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter
                    .ofPattern("uuuu-MM-dd'T'HH:mmXXXXX");

    @Autowired
    private FlightInventoryConfiguration flightInventoryConfiguration;

    public List<Flight> parseFlightSource() throws URISyntaxException, FileNotFoundException {

        URL resource = getClass()
                .getClassLoader()
                .getResource(flightInventoryConfiguration.getFlightResourcePath());

        List<Flight> flights = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(resource.toURI()));) {
            while (scanner.hasNextLine()) {
                Flight record = getFlightFromLine(scanner.nextLine().trim());
                flights.add(record);
            }
        }
        return flights;
    }

    private Flight getFlightFromLine(String line) {
        String[] parsedLine = line.split(",");
        Flight flight = new Flight();
        flight.date = parsedLine[0];
        flight.flightNumber = parsedLine[1];
        flight.fromAirport = parsedLine[2];
        flight.toAirport = parsedLine[3];
        flight.departureDate = parsedLine[4];
        flight.duration = Integer.parseInt(parsedLine[5]);
        flight.dateTimeUTC =
                Date.from(OffsetDateTime
                        .parse(flight.date + "T" + flight.departureDate, DATE_TIME_FORMATTER)
                        .withOffsetSameInstant(ZoneOffset.UTC).toInstant());
        return flight;
    }
}
