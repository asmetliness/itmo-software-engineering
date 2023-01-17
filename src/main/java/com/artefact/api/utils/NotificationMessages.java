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

        public static final String WasDelivered = "Заказ был доставлен!";

        public static final String CompletedByStalker = "Сталкер выполнил заказ и передал барыге!";
        public static final String CompletedByClient = "Клиент подтвердил завершение заказа!";

    }

    public static class Information {
        public static final String Bought = "Информация была куплена!";
    }

    public static class Weapon {
        public static final String Bought = "Оружие было куплено!";
        public static final String SuggestedToCourier = "Вам была предложена доставка оружия!";
        public static final String AcceptedByCourier = "Курьер принял доставку оружия!";

        public static final String DeclinedByCourier = "Курьер отклонил доставку оружия!";
        public static final String Delivered = "Курьер доставил заказ!";
        public static final String Confirmed = "Сталкер подтвердил доставку заказа!";

    }
}
