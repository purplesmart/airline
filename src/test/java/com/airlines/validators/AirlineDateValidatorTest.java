package com.airlines.validators;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AirlineDateValidatorTest {

    private DateValidator dateValidator;

    @BeforeAll
    public void setUp() {
        dateValidator = new AirlineDateValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = "2022-01-12")
    void isValidTrue(String dateStr) {
        assertTrue(dateValidator.isValid(dateStr));
    }

    @ParameterizedTest
    @ValueSource(strings =
            {
                    "202-01-12",
                    "2022-0-12",
                    "2022-01-2",
                    "202201-12",
                    "2022-0112",
                    "20220112",
                    "20A2-01-12",
                    "2022-0A-12",
                    "2022-01-A2",
                    "2022-0-12",
                    "202-01-12",
                    "2022-01-1",})
    void isValidFalse(String dateStr) {
        assertFalse(dateValidator.isValid(dateStr));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value =
            {"2022-01-12| 2022-01-15"})
    void testIsValidTrue(String startDateStr, String endDateStr) {
        assertTrue(dateValidator.isValid(startDateStr,  endDateStr));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value =
            {
                    "2022-01-15| 2022-01-12",
                    "202-01-12| 2022-01-15",
                    "2022-1-12| 2022-01-15",
                    "2022-01-2| 2022-01-15",
                    "2022-01-12| 202-01-15",
                    "2022-01-12| 2022-1-15",
                    "2022-01-12| 2022-01-1",
                    "20A2-01-12| 2022-01-15",
                    "2022-0A-12| 2022-01-15",
                    "2022-01-1A| 2022-01-15",
                    "2022-01-12| 20A2-01-15",
                    "2022-01-12| 2022-0A-15",
                    "2022-01-12| 2022-01-A5"})
    void testIsValidFalse(String startDateStr, String endDateStr) {
        assertFalse(dateValidator.isValid(startDateStr,  endDateStr));
    }

    @ParameterizedTest
    @ValueSource(strings = "2022-01-12")
    void validateDepartureDate(String startDateStr) {
        dateValidator.validateDepartureDate(startDateStr);
    }

    @ParameterizedTest
    @ValueSource(strings =
            {
                    "202-01-12",
                    "2022-0-12",
                    "2022-01-2",
                    "202201-12",
                    "2022-0112",
                    "20220112",
                    "20A2-01-12",
                    "2022-0A-12",
                    "2022-01-A2",
                    "2022-0-12",
                    "202-01-12",
                    "2022-01-1",})
    void validateDepartureDateThrowException(String startDateStr) {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class,
                        () -> dateValidator.validateDepartureDate(startDateStr));

        String expectedMessage = "Departure date is not valid: " + startDateStr;
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value =
            {"2022-01-12| 2022-01-15"})
    void validateDateRange(String startDateStr, String endDateStr) {
        dateValidator.validateDateRange(startDateStr,endDateStr);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value =
            {
                    "2022-01-15| 2022-01-12",})
    void validateDateRangeThrowException(String startDateStr, String endDateStr) {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class,
                        () -> dateValidator.validateDateRange(startDateStr,endDateStr));

        String expectedMessage = String.format("Return date ( %s ) is not later then departure date ( %s )",startDateStr,  endDateStr);
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value =
            {
                    "202-01-12| 2022-01-15",
                    "2022-1-12| 2022-01-15",
                    "2022-01-2| 2022-01-15",
                    "20A2-01-12| 2022-01-15",
                    "2022-0A-12| 2022-01-15",
                    "2022-01-1A| 2022-01-15"})
    void validateDateRangeDepartureThrowException(String startDateStr, String endDateStr) {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class,
                        () -> dateValidator.validateDateRange(startDateStr,endDateStr));

        String expectedMessage = String.format("Departure date is not valid: %s",startDateStr);
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value =
            {
                    "2022-01-12| 202-01-15",
                    "2022-01-12| 2022-1-15",
                    "2022-01-12| 2022-01-1",
                    "2022-01-12| 20A2-01-15",
                    "2022-01-12| 2022-0A-15",
                    "2022-01-12| 2022-01-A5"})
    void validateDateRangeReturnThrowException(String startDateStr, String endDateStr) {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class,
                        () -> dateValidator.validateDateRange(startDateStr,endDateStr));

        String expectedMessage = String.format("Return date is not valid: %s",  endDateStr);
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }
}