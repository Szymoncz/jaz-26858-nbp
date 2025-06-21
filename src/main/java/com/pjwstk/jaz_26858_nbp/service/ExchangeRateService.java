package com.pjwstk.jaz_26858_nbp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.pjwstk.jaz_26858_nbp.dto.ExchangeRateResponse;
import com.pjwstk.jaz_26858_nbp.dto.NbpApiResponse;
import com.pjwstk.jaz_26858_nbp.entity.ExchangeRateQuery;
import com.pjwstk.jaz_26858_nbp.exception.NbpApiException;
import com.pjwstk.jaz_26858_nbp.repository.ExchangeRateQueryRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ExchangeRateQueryRepository repository;
    private static final String NBP_API_URL = "http://api.nbp.pl/api/exchangerates/rates/A/{currency}/{startDate}/{endDate}/?format=json";

    public ExchangeRateResponse calculateAverageRate(String currency, String startDate, String endDate) {
        validateInput(currency, startDate, endDate);
        validateDates(startDate, endDate);

        try {
            String url = NBP_API_URL.replace("{currency}", currency)
                    .replace("{startDate}", startDate)
                    .replace("{endDate}", endDate);

            NbpApiResponse apiResponse = restTemplate.getForObject(url, NbpApiResponse.class);

            if (apiResponse == null || apiResponse.getRates() == null || apiResponse.getRates().isEmpty()) {
                throw new NbpApiException("No data available for the specified period", HttpStatus.NOT_FOUND);
            }

            double averageRate = apiResponse.getRates().stream()
                    .mapToDouble(NbpApiResponse.Rate::getMid)
                    .average()
                    .orElseThrow(() -> new NbpApiException("Unable to calculate average rate", HttpStatus.INTERNAL_SERVER_ERROR));

            ExchangeRateQuery query = saveQuery(currency, startDate, endDate, averageRate);

            return ExchangeRateResponse.builder()
                    .currency(currency)
                    .startDate(startDate)
                    .endDate(endDate)
                    .averageRate(averageRate)
                    .queryId(query.getId())
                    .build();

        } catch (HttpClientErrorException e) {
            handleNbpError(e);
            throw new NbpApiException("Unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateInput(String currency, String startDate, String endDate) {
        if (currency == null || !currency.matches("[A-Z]{3}")) {
            throw new NbpApiException("Currency must be a 3-letter code (e.g., USD)", HttpStatus.BAD_REQUEST);
        }
        if (startDate == null || !startDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new NbpApiException("Start date must be in YYYY-MM-DD format", HttpStatus.BAD_REQUEST);
        }
        if (endDate == null || !endDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new NbpApiException("End date must be in YYYY-MM-DD format", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateDates(String startDate, String endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);

            if (start.isAfter(end)) {
                throw new NbpApiException("Start date cannot be after end date", HttpStatus.BAD_REQUEST);
            }
            if (end.isAfter(LocalDate.now())) {
                throw new NbpApiException("End date cannot be in the future", HttpStatus.BAD_REQUEST);
            }
        } catch (DateTimeParseException e) {
            throw new NbpApiException("Invalid date format. Use YYYY-MM-DD", HttpStatus.BAD_REQUEST);
        }
    }

    private ExchangeRateQuery saveQuery(String currency, String startDate, String endDate, double averageRate) {
        ExchangeRateQuery query = new ExchangeRateQuery();
        query.setCurrency(currency);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setAverageRate(averageRate);
        query.setQueryTimestamp(LocalDateTime.now());
        return repository.save(query);
    }

    private void handleNbpError(HttpClientErrorException e) {
        switch (e.getStatusCode().value()) {
            case 400:
                throw new NbpApiException("Invalid request parameters", HttpStatus.BAD_REQUEST);
            case 404:
                throw new NbpApiException("Currency or data not found", HttpStatus.NOT_FOUND);
            case 429:
                throw new NbpApiException("Too many requests", HttpStatus.TOO_MANY_REQUESTS);
            default:
                throw new NbpApiException("NBP API error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
