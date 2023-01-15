package com.artefact.api.utils;

public class NotificationMessages {

    public static class Order {
        public static final String Created = "Был создан заказ!";
        public static final String AcceptedByHuckster = "Заказ был принят барыгой!";
        public static final String AcceptedByStalker = "Заказ был принят сталкером!";

        public static final String TransferredToCourier = "Заказ был передан курьеру!";

        public static final String Suggested = "Вам был предложен заказ!";

        public static final String DeclinedByHuckster = "Заказ был отклонен барыгой!";
        public static final String DeclinedByStalker = "Заказ был отклонен сталкером!";
        public static final String DeclinedByCourier = "Заказ был отклонен курьером!";

    }
}
