package com.airlines.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.airlines.model.AllRoundTripRequest;
import com.airlines.model.Itinerary;
import com.airlines.model.RoundTripRequest;
import com.airlines.model.WithConnectionsRequest;
import com.airlines.services.FlightItineraryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AirlineController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AirlineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlightItineraryService flightItineraryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    AirlineControllerTest() {
    }

    private String getRoundTripRequest(String departureDate, String fromAirport, String returnDate, String toAirport) throws JsonProcessingException {
        RoundTripRequest roundTripRequest = new RoundTripRequest();
        roundTripRequest.departureDate = departureDate;
        roundTripRequest.fromAirport = fromAirport;
        roundTripRequest.returnDate = returnDate;
        roundTripRequest.toAirport = toAirport;
        return objectMapper.writeValueAsString(roundTripRequest);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value =
            {"2022-01-12| JFK| 2022-01-14| MEX"})
    void priceRoundTripHappyPath(String departureDate, String fromAirport, String returnDate, String toAirport) throws Exception {

        Mockito.when(flightItineraryService.getPriceRoundTrip(departureDate, fromAirport, returnDate, toAirport))
                .thenReturn(new Itinerary[1]);
        String roundTripRequestAsString =
                getRoundTripRequest(departureDate, fromAirport, returnDate, toAirport);

        this.mockMvc.perform(get("/itinerary/priceRoundTrip")
                        .content(roundTripRequestAsString)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value =
            {"2022-0112| JFK| 2022-01-14| MEX",
                    "2022-01-12| FK| 2022-01-14| MEX",
                    "2022-01-12| JFK| 2022-0114| MEX",
                    "2022-01-12| JFK| 2022-01-14| MX"})
    void priceRoundTripValidationError(String departureDate, String fromAirport, String returnDate, String toAirport) throws Exception {

        String roundTripRequestAsString =
                getRoundTripRequest(departureDate, fromAirport, returnDate, toAirport);

        this.mockMvc.perform(get("/itinerary/priceRoundTrip")
                        .content(roundTripRequestAsString)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    private String getWithConnectionsRequest(String date, String fromAirport, String toAirport) throws JsonProcessingException {
        WithConnectionsRequest withConnectionsRequest = new WithConnectionsRequest();
        withConnectionsRequest.date = date;
        withConnectionsRequest.fromAirport = fromAirport;
        withConnectionsRequest.toAirport = toAirport;
        return objectMapper.writeValueAsString(withConnectionsRequest);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value =
            {"2022-08-14| TLV| MEX"})
    void priceWithConnectionsHappyPath(String departureDate, String fromAirport, String toAirport) throws Exception {

        Mockito.when(flightItineraryService.getPriceWithConnections(departureDate, fromAirport, toAirport))
                .thenReturn(new Itinerary[1]);
        String withConnectionsRequestAsString =
                getWithConnectionsRequest(departureDate, fromAirport, toAirport);

        this.mockMvc.perform(get("/itinerary/priceWithConnections")
                        .content(withConnectionsRequestAsString)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value =
            {"2022-0814| TLV| MEX",
                    "2022-08-14| LV| MEX",
                    "2022-08-14| TLV| EX"})
    void priceWithConnectionsValidationError(String departureDate, String fromAirport, String toAirport) throws Exception {

        String withConnectionsRequestAsString =
                getWithConnectionsRequest(departureDate, fromAirport, toAirport);

        this.mockMvc.perform(get("/itinerary/priceWithConnections")
                        .content(withConnectionsRequestAsString)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private String getAllRoundTripRequest(String fromAirport, String toAirport) throws JsonProcessingException {
        AllRoundTripRequest allRoundTripRequest = new AllRoundTripRequest();
        allRoundTripRequest.fromAirport = fromAirport;
        allRoundTripRequest.toAirport = toAirport;
        return objectMapper.writeValueAsString(allRoundTripRequest);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value =
            {"JFK| MEX"})
    void priceAllRoundTripHappyPath(String fromAirport, String toAirport) throws Exception {

        Mockito.when(flightItineraryService.getPriceAllRoundTrip(fromAirport, toAirport))
                .thenReturn(new Itinerary[1]);
        String allRoundTripRequestAsString =
                getAllRoundTripRequest(fromAirport, toAirport);

        this.mockMvc.perform(get("/itinerary/priceAllRoundTrip")
                        .content(allRoundTripRequestAsString)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value =
            {"FK| MEX",
                    "JFK| EX"})
    void priceAllRoundTripValidationError(String fromAirport, String toAirport) throws Exception {

        String allRoundTripRequestAsString =
                getAllRoundTripRequest(fromAirport, toAirport);

        this.mockMvc.perform(get("/itinerary/priceAllRoundTrip")
                        .content(allRoundTripRequestAsString)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

}