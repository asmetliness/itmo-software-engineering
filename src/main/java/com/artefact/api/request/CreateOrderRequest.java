package com.artefact.api.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class CreateOrderRequest {
    @NotNull(message = "Пожалуйста, укажите артефакт!")
    private Long artifactId;
    @NotNull(message = "Пожалуйста, укажите цену!")
    private BigDecimal price;

    @JsonFormat(pattern = "dd.MM.yyyy")
    private Date completionDate;

    @NotBlank(message = "Пожалуйста, укажите адрес доставки!")
    private String deliveryAddress;
}
