package com.pjwstk.jaz_26858_nbp.dto;

import lombok.Data;

@Data
public class ExchangeRateRequest {
    private String currency;
    private String startDate;
    private String endDate;
}