package com.pjwstk.jaz_26858_nbp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExchangeRateResponse {
    private String currency;
    private String startDate;
    private String endDate;
    private Double averageRate;
    private Long queryId;
}
