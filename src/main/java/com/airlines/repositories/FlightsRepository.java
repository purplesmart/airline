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

    List<Flight> findByDateAndFromAirport(String date, String fromAirport);
    //List<Flight> findByDateAndFromAirportAndToAirport(String date, String fromAirport, String toAirport);
    List<Flight> findByDateTimeUTCBetweenAndFromAirportAndToAirport(Date dateTimeUTCStart, Date dateTimeUTCEnd, String fromAirport, String toAirport);
    List<Flight> findByDateInAndFromAirportIn(String[] dates, String[] fromAirports);
    List<Flight> findByDateTimeUTCIn(Date[] dates);
}
