package com.example.f4sINV;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.f4sINV.activity.MainActivity;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);          // <- obligatorio
            // (opcional) si se quiere limpiar la pila:
            // i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(i);
        }
    }

}
