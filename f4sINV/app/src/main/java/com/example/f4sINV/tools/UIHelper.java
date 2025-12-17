package com.example.f4sINV.tools;

import com.example.f4sINV.Parametros;
import com.example.f4sINV.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class UIHelper {

	public static void ToastMessage(Context cont, String msg) {
		Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
	}

    public static void writeLog(String msg, boolean fecHor) {

        Date date;
        String fechaHora;

        File ficLog = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Parametros.ficLog);

        try {
            // Log de Actividad salida de tipo APPEND = true
            FileOutputStream f;
            f = new FileOutputStream(ficLog,true);

            if (fecHor){
                // De momento no usar
                date = new Date();
                DateFormat hourdateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss ");
                fechaHora = "%01%-" + hourdateFormat.format(date);
                f.write(fechaHora.getBytes());
                f.write("->".getBytes());
                f.write("\n".getBytes());
            }
            f.write(msg.getBytes());
            f.write("\n".getBytes());

            f.close();
        }
        catch (Exception ignored) {
            // No hacer nada de momento - NO graba nada
        }
    }
}

