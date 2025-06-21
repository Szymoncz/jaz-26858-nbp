package com.pjwstk.jaz_26858_nbp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "exchange_rate_queries")
public class ExchangeRateQuery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String currency;
    private String startDate;
    private String endDate;
    private Double averageRate;
    private LocalDateTime queryTimestamp;
}
