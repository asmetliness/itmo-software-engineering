package com.artefact.api.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class SuggestOrderRequest {
    @NotNull(message = "Пожалуйста, укажите идентификатор заказа!")
    private Long orderId;
    @NotNull(message = "Пожалуйста, укажите идентификатор пользователя!")
    private Long userId;
}
