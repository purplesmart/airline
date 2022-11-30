package com.airlines.validators;

import org.springframework.stereotype.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AirlineDateValidator implements DateValidator {

    private String DATE_FORMAT = "yyyy-MM-dd";
    private DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    Pattern datePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    @Override
    public boolean isValid(String dateStr) {
        Matcher dateMatcher = datePattern.matcher(dateStr);
        return dateMatcher.matches();
    }

    @Override
    public boolean isValid(String startDateStr, String endDateStr) {
        try {
            if (isValid(startDateStr) && isValid(endDateStr)) {
                Date startDate = this.dateFormat.parse(startDateStr);
                Date endDate = this.dateFormat.parse(endDateStr);
                return endDate.after(startDate);
            }
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public void validateDepartureDate(String departureDate) {
        if (!isValid(departureDate)) {
            String errorMsg = String.format("Departure date is not valid: %s", departureDate);
            throw new IllegalArgumentException (errorMsg);
        }
    }

    @Override
    public void validateDateRange(String departureDate, String returnDate){
        if (!isValid(departureDate, returnDate)) {
            StringBuilder ErrorMsgBuilder = new StringBuilder();
            if (!isValid(departureDate)) {
                ErrorMsgBuilder.append(String.format("Departure date is not valid: %s", departureDate));
            }
            if (!isValid(returnDate)) {
                ErrorMsgBuilder.append(String.format("Return date is not valid: %s", returnDate));
            }
            if (isValid(departureDate) && isValid(returnDate)) {
                ErrorMsgBuilder.append(String.format("Return date ( %s ) is not later then departure date ( %s )", departureDate, returnDate));
            }
            throw new IllegalArgumentException(ErrorMsgBuilder.toString());
        }
    }

}
