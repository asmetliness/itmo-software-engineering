package com.artefact.api.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class CreateOrderRequest {
    private Long artifactId;
    private BigDecimal price;
    @JsonFormat(pattern = "dd.MM.yyyy")
    private Date completionDate;

    private String deliveryAddress;
}
