package com.example.gohiking_cs310;

import android.content.Context;
import android.widget.Toast;

//prints toast message and records the last toast message
public class ToastUtil {
    private static String lastToastMessage = "";
    public static void showToast(Context context, String message) {
        lastToastMessage = message;
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static String getLastToastMessage() {
        return lastToastMessage;
    }
}