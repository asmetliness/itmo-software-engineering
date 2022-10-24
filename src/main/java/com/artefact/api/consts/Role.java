package com.artefact.api.consts;

import java.util.Arrays;

public class Role {
    public static final String Stalker = "Сталкер";
    public static final String Client = "Клиент";
    public static final String Huckster = "Барыга";
    public static final String Informer = "Информатор";
    public static final String WeaponDealer = "Продавец оружия";

    public static final String[] ALL = {
            Role.Stalker,
            Role.Client,
            Role.Huckster,
            Role.Informer,
            Role.WeaponDealer,
    };

    public static boolean isPresent(String name) {
        return Arrays.asList(ALL).contains(name);
    }
}
