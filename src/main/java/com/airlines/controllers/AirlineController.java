package com.airlines.controllers;

import com.airlines.model.*;
import com.airlines.services.FlightItineraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.Optional;

@Validated
@RestController
@RequestMapping("/itinerary")
public class AirlineController {

    @Autowired
    private FlightItineraryService flightItineraryService;


    @GetMapping("/priceRoundTrip")
    public ResponseEntity<Itinerary[]> priceRoundTrip(
            @RequestBody @Valid RoundTripRequest roundTripRequest) {
        try {
            return Optional
                    .ofNullable(flightItineraryService.getPriceRoundTrip(
                            roundTripRequest.departureDate,
                            roundTripRequest.fromAirport,
                            roundTripRequest.returnDate,
                            roundTripRequest.toAirport))
                    .map(flightItinerary -> ResponseEntity.ok().body(flightItinerary))
                    .orElseGet(() -> ResponseEntity.internalServerError().build());
        }catch (Exception ex){
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "General error", ex);
        }
    }

    @GetMapping("/priceWithConnections")
    public ResponseEntity<Itinerary[]> priceWithConnections(
            @RequestBody @Valid WithConnectionsRequest withConnectionsRequest) {
        try {
            return Optional
                    .ofNullable(flightItineraryService.getPriceWithConnections(
                            withConnectionsRequest.date,
                            withConnectionsRequest.fromAirport,
                            withConnectionsRequest.toAirport))
                    .map(flightItinerary -> ResponseEntity.ok().body(flightItinerary))
                    .orElseGet(() -> ResponseEntity.internalServerError().build());
        }catch (Exception ex){
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "General error", ex);
        }
    }

    @GetMapping("/priceAllRoundTrip")
    public ResponseEntity<Itinerary[]> priceAllRoundTrip(
            @RequestBody @Valid AllRoundTripRequest allRoundTripRequest) {
        try {
            return Optional
                    .ofNullable(flightItineraryService.getPriceAllRoundTrip(
                            allRoundTripRequest.fromAirport,
                            allRoundTripRequest.toAirport))
                    .map(flightItinerary -> ResponseEntity.ok().body(flightItinerary))
                    .orElseGet(() -> ResponseEntity.internalServerError().build());
        }catch (Exception ex){
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "General error", ex);
        }
    }

}
