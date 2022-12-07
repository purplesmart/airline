package com.airlines.repositories;

import com.airlines.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;

@Repository
@EnableJpaRepositories
public interface FlightsRepository extends JpaRepository<Flight, String> {

    List<Flight> findByDateAndFromAirportAndAvailableSeatsGreaterThanEqual(String date, String fromAirport, int availableSeats);

    List<Flight> findByDateInAndFromAirportInAndToAirportInAndAvailableSeatsGreaterThanEqual(String[] departureDates, String[] fromAirports, String[] toAirports, int availableSeats);

    List<Flight> findByFromAirportInAndToAirportInAndAvailableSeatsGreaterThanEqualOrderByDate(String[] fromAirports, String[] toAirports, int availableSeats);

    List<Flight> findByDateTimeUTCBetweenAndToAirportAndAvailableSeatsGreaterThanEqual(Date dateTimeUTCStart, Date dateTimeUTCEnd, String toAirport, int availableSeats);

}
