package com.airlines.validators;

public interface DateValidator {
    boolean isValid(String date);
    boolean isValid(String startDate, String endDate);
    void validateDepartureDate(String departureDate);
    void validateDateRange(String departureDate, String returnDate) ;
}
