package com.airlines.parsers;

import com.airlines.configuration.FlightInventoryConfiguration;
import com.airlines.model.Price;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component
public class PricesParser {

    @Autowired
    private FlightInventoryConfiguration flightInventoryConfiguration;

    public List<Price> parseFlightSource() throws URISyntaxException, FileNotFoundException {

        URL resource = getClass()
                .getClassLoader()
                .getResource(flightInventoryConfiguration.getPriceResourcePath());

        List<Price> prices = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(resource.toURI()));) {
            while (scanner.hasNextLine()) {
                Price record = getPricesFromLine(scanner.nextLine().trim());
                prices.add(record);
            }
        }
        return prices;
    }

    private Price getPricesFromLine(String line) {
        String[] parsedLine = line.split(",");
        Price price = new Price();
        price.date = parsedLine[0];
        price.flightNumber = parsedLine[1];
        price.availableSeats = Integer.parseInt(parsedLine[2]);
        price.price = Double.parseDouble(parsedLine[3]);
        return price;
    }


}
