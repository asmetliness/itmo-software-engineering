package com.artefact.api.utils;

import com.artefact.api.response.ErrorResponse;

public class ApiErrors {

    public class Auth {
        public static final ErrorResponse UserAlreadyExists = new ErrorResponse("Пользователь с таким Email уже существует!");
        public static final ErrorResponse RoleNotFound = new ErrorResponse("Переданная роль не найдена!");
        public static final ErrorResponse UserNotFound = new ErrorResponse("Пользователь не найден!");
    }


    public class Information {
        public static final ErrorResponse CreateError = new ErrorResponse("Выставлять информацию может только информатор!");
        public static final ErrorResponse NotFound = new ErrorResponse("Информация не найдена!");
        public static final ErrorResponse AccessViolation = new ErrorResponse("Вы не можете модифицировать данную информацию!");
        public static final ErrorResponse UnauthorizedRole = new ErrorResponse("Покупка информации доступна только сталкерам!");

    }
    public class Order {
        public static final ErrorResponse CantStart = new ErrorResponse("Вы не можете начать процесс по данному заказу!");
        public static final ErrorResponse CantComplete = new ErrorResponse("Вы не можете завершить данный заказ!");
        public static final ErrorResponse CantDecline = new ErrorResponse("Вы не можете отклонить данный заказ!");
        public static final ErrorResponse CantAccept = new ErrorResponse("Вы не можете принять данный заказ!");

        public static final ErrorResponse CantDeliver = new ErrorResponse("Вы не можете доставить данный заказ!");

        public static final ErrorResponse CantCreate = new ErrorResponse("Вы не можете создавать заказ!");

        public static final ErrorResponse AccessError = new ErrorResponse("У вас нет доступа к данному заказу!");

        public static final ErrorResponse CantSuggestToUser = new ErrorResponse("Вы не можете предложить заказ данному пользователю!");

        public static final ErrorResponse NotFound = new ErrorResponse("Заказ не найден!");
    }


    public class User {
        public static final ErrorResponse ImageIsEmpty = new ErrorResponse("Изображение отсутствует!");
        public static final ErrorResponse UnknownImageType = new ErrorResponse("Неизвестный contentType!");

    }

    public class Weapon {
        public static final ErrorResponse CantDelete = new ErrorResponse("Вы не можете удалить это оружие!");

        public static final ErrorResponse CantCreate = new ErrorResponse("Выставлять оружие может только торговец оружием!");

        public static final ErrorResponse NotFound = new ErrorResponse("Оружие не найдено!");

        public static final ErrorResponse CantEdit = new ErrorResponse("Вы не можете редактировать данное оружие!");

        public static final ErrorResponse CantBuy = new ErrorResponse("Покупка оружия доступна только сталкерам!");

        public static final ErrorResponse CantConfirm = new ErrorResponse("Вы не можете подтвердить данный заказ!");
        public static final ErrorResponse CantDecline = new ErrorResponse("Вы не можете отклонить данный заказ!");

        public static final ErrorResponse CantSuggest = new ErrorResponse("Вы не можете предложить это оружие курьеру!");
        public static final ErrorResponse CantSuggestToUser = new ErrorResponse("Вы не можете предложить доставку этому пользователю!");


    }


    public static final ErrorResponse UnexpectedError = new ErrorResponse("Неизвестная ошибка!");


}
