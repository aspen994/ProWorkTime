package com.example.ogrdapp.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MyBroadcastReceiver extends BroadcastReceiver {

    // Tworzymy zmienną statyczną, w której będzie przechowywana jedyna instancja klasy
    private static MyBroadcastReceiver instance;

    // Prywatny konstruktor uniemożliwia utworzenie instancji poza klasą
    private MyBroadcastReceiver() {
    }

    // Metoda statyczna, która zwraca istniejącą instancję lub tworzy nową, jeśli jeszcze nie istnieje
    public static synchronized MyBroadcastReceiver getInstance() {
        if (instance == null) {
            instance = new MyBroadcastReceiver();
        }
        return instance;
    }
    @Override
    public void onReceive(Context context, Intent intent) {

    }
}
