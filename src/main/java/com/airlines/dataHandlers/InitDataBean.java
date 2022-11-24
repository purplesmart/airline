package com.airlines.dataHandlers;

import com.airlines.model.*;
import com.airlines.parsers.*;
import com.airlines.repositories.FlightsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class InitDataBean  implements ApplicationRunner {

    @Autowired
    private FlightParser flightParser;

    @Autowired
    private PricesParser pricesParser;

    @Autowired
    private FlightsRepository flightsRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LoadDataFromCSV();
    }

    private void LoadDataFromCSV() throws Exception {
        List<Flight> rawFlights = flightParser.parseFlightSource();
        List<Price> prices = pricesParser.parseFlightSource();

        List<Flight> flights = rawFlights.stream()
                .map(flight -> {
                    Price matchingPrice = prices.stream()
                            .filter(price ->
                                    flight.flightNumber.equals(price.flightNumber)
                                            && flight.date.equals(price.date))
                            .findFirst().get();
                    flight.availableSeats = matchingPrice.availableSeats;
                    flight.price = matchingPrice.price;
                    return flight;
                }).collect(Collectors.toList());

        flightsRepository.saveAll(flights);
        System.out.println("Data uploaded from the csv file to the repository");
    }
}
