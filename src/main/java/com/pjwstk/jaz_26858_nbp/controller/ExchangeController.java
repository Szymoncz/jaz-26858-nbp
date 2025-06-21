package com.pjwstk.jaz_26858_nbp.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.pjwstk.jaz_26858_nbp.dto.ExchangeRateResponse;
import com.pjwstk.jaz_26858_nbp.service.ExchangeRateService;

@RestController
@RequestMapping("/api/exchange-rate")
@RequiredArgsConstructor
@Api(tags = "Exchange Rate Controller")
public class ExchangeController {

    private final ExchangeRateService exchangeRateService;

    @GetMapping
    @ApiOperation(value = "Calculate average exchange rate",
            notes = "Calculates average exchange rate for given currency and date range")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully calculated average rate"),
            @ApiResponse(code = 400, message = "Invalid input parameters"),
            @ApiResponse(code = 404, message = "Currency or data not found"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    public ResponseEntity<ExchangeRateResponse> calculateAverageRate(
            @RequestParam String currency,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        ExchangeRateResponse response = exchangeRateService.calculateAverageRate(currency, startDate, endDate);
        return ResponseEntity.ok(response);
    }
}