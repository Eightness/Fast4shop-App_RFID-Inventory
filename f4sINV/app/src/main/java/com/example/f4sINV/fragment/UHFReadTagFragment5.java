package com.example.f4sINV.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.f4sINV.MyFTPClientFunctions;
import com.example.f4sINV.Parametros;
import com.example.f4sINV.activity.TabInventarioActivity;
import com.example.f4sINV.R;
import com.example.f4sINV.tools.UIHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class UHFReadTagFragment5 extends Fragment {

    private static final String TAG = "UHFReadTagFragment5";

    private HashMap<String, Integer> mapTempDatas = new HashMap<String, Integer>();

    Button BtSave;
    Button BtFtp;
    TextView txtSesionesInv, txtEtiSave, txtSesionInv, txtCount;

    private TabInventarioActivity mContext;

    String fechaHora;
    String campo;

    // Objetos de la inclusión de FTP
    private ProgressDialog pd;
    private MyFTPClientFunctions ftpclient = null;
    String dirCreado = "";

    // Parámetros del DialogoFragment
    private static final String DIALOGO = "Dlg" + TAG; //Nombre único
    private static final int REQUEST_result = 0;

    private Handler handler = new Handler() {

        public void handleMessage(android.os.Message msg) {

            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }

            if (msg.what == 0) {

            } else if (msg.what == 1) {

            } else if (msg.what == 2) {
                // Guarda valores
                SharedPreferences preferencias = mContext.getSharedPreferences(Parametros.PREFERENCIAS_app, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferencias.edit();
                editor.putInt(Parametros.pref_ETI_SESIONES_SAVE, 0);
                editor.putInt(Parametros.pref_SESIONES_INV, 0);
                editor.putString(Parametros.pref_FECHA_HORA_SAVE, "");
                editor.putString(Parametros.pref_OBSERVACION_SAVE, "");
                mContext.sesionInv=0;   //Sigueinte sesion en curso
                editor.apply();

                mContext.playSound(4);

                // Aviso de inventario guardado
                salidaAlerta("Enviado",
                        "Inventario enviado al servidor",
                         "- TERMINE EL PROCESO DE ACTUALIACIÓN DEL INVENTARIO EN LA CONSOLA de fast4shop.\n\n"+
                                "- Si lo cree necesario puede reforzar el inventario con más lecturas de zonas y añadir nuevos envíos.",
                        R.drawable.logo_f4s, Parametros.DIALOGO_ALERT_CON_BTN_ATRAS);

            } else if (msg.what == 20) {

            } else if (msg.what == 3) {

            } else if (msg.what == -1) {
                mContext.playSound(3);
                salidaAlerta("Fallo Servidor",
                        "No hay conexión con servidor",
                        "Posibilidades:\n\n" +
                                "- Servidor apagado.\n"+
                                "- Servicio FTP parado.\n"+
                                "- Credenciales de usuario incorrectas.\n"+
                                "- Fallo general de la red.",
                        R.drawable.baseline_warning_amber_24, Parametros.DIALOGO_ALERT_SIN_BTN_ATRAS);
            } else if (msg.what == -2) {
                mContext.playSound(3);
                salidaAlerta("Error de envío",
                        "Ha fallado el envío de inventario",
                        "Posibilidades:\n\n\"+" +
                                "- Vuelva a intentarlo, si vuelve a fallar avise al Servicio Técnico",
                        R.drawable.baseline_warning_amber_24, Parametros.DIALOGO_ALERT_SIN_BTN_ATRAS);
            }
            else {

            }
        }
    };

    public UHFReadTagFragment5() {
        // Required empty public constructor
    }

    public static UHFReadTagFragment5 newInstance() {
        return new UHFReadTagFragment5();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uhf_readtag_fragment5, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContext = (TabInventarioActivity) getActivity();

        txtSesionesInv = getView().findViewById(R.id.tv_SesionesInv);
        txtEtiSave = getView().findViewById(R.id.tv_EtiSave);
        txtSesionInv = getView().findViewById(R.id.tv_SesionInv);
        txtCount = getView().findViewById(R.id.tv_Count);

        BtFtp = getView().findViewById(R.id.BtFtp);
        BtSave = getView().findViewById(R.id.BtSave);

        BtFtp.setOnClickListener(new View.OnClickListener(){
                 @Override
                 public void onClick(View v) {
                    FragmentManager manager = getFragmentManager();
                    MiDialogFragment dialog = MiDialogFragment.newInstance(TAG, Parametros.DIALOGO_ACCION_FTP);
                    dialog.setTargetFragment(UHFReadTagFragment5.this, REQUEST_result);
                    dialog.show(manager, DIALOGO);
                 }
        });

        BtSave.setOnClickListener(v -> {
            // Sal si no hay tags leidos
            if (mapTempDatas.isEmpty()){
                mContext.playSound(6);
                salidaAlerta("No hay lecturas",
                        "No hay etiquetas leidas en la sesión",
                        "- Realice lecturas de etiquetas.",
                        R.drawable.baseline_warning_amber_24, Parametros.DIALOGO_ALERT_SIN_BTN_ATRAS);
            } else {
                FragmentManager manager = getFragmentManager();
                MiDialogFragment dialog = MiDialogFragment.newInstance(TAG, Parametros.DIALOGO_ACCION_SAVE);
                dialog.setTargetFragment(UHFReadTagFragment5.this, REQUEST_result);
                dialog.show(manager, DIALOGO);
            }
        });

        txtSesionesInv.setText(mContext.sesionesInv +"");
        txtEtiSave.setText(mContext.etiSesionesSave +"");

        // Recupera el diccionario de EPCs que se han grabado en esta sesión o simplemente está vacio y fue creado por UHFReadTagFrangment2
        mapTempDatas = mContext.uhfInfo.getTempDatas();
        txtSesionInv.setText(mContext.sesionInv +"");
        txtCount.setText(mapTempDatas.size()+"");

        // Guarda valor
        SharedPreferences preferencias = mContext.getSharedPreferences(Parametros.PREFERENCIAS_app, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putString(mContext.pref_PANTA, "PANTA5");
        editor.apply();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Campos comunes
        String extra_accion = (String) data.getSerializableExtra(Parametros.EXTRA_ACCION);
        campo = (String) data.getSerializableExtra(Parametros.EXTRA_EDIT_TEXT);

        switch (Objects.requireNonNull(extra_accion)){
            case Parametros.DIALOGO_ALERT_CON_BTN_ATRAS:
                // Regresa a AcivityMain
                getActivity().onBackPressed();
                break;
            case Parametros.DIALOGO_ALERT_SIN_BTN_ATRAS:
                // No hacer nada
                break;
            case Parametros.DIALOGO_ACCION_SAVE:
                if (resultCode == Activity.RESULT_CANCELED){
                    return;
                }
                if (resultCode == Activity.RESULT_OK){
                    // Guardar inventario y mira si retorna sin errores
                    if(saveInventario()){
                        mContext.playSound(4);
                        // Aviso de inventario guardado
                        salidaAlerta("Guardado",
                                "Inventario parcial guardado",
                                "- Puede continuar con otra sesión de inventario más tarde.\n\n"+
                                        "- Todas las lecturas de las sesiones se acumulan hasta finalizar el inventario. No afecta que repita lecturas de etiquetas.",
                                R.drawable.logo_f4s, Parametros.DIALOGO_ALERT_CON_BTN_ATRAS);

                    } else {
                        // Ya avisado por pantalla del error en saveInventario()
                    };
                }
                break;
            case Parametros.DIALOGO_ACCION_FTP:
                if (resultCode == Activity.RESULT_CANCELED){
                    return;
                }
                if (resultCode == Activity.RESULT_OK){

                    File fic = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Parametros.ficInv);
                    if (fic.exists() || !mapTempDatas.isEmpty()) {
                        // Antes de enviar inventario mira si hay lecturas de sesion en curso y añadelas al fic de inventario
                        if (!mapTempDatas.isEmpty()){
                            // Guardar inventario en fic y mira si retorna sin errores
                            if (!saveInventario()) {
                                // Ya avisado por pantalla del error en saveInventario()
                                return;
                            }
                        }
                        // SIMULAR ENVÍO: En pruebas NO enviar por FTP
                        if (!Parametros.enviosFtp) {
                            // Fic Inventario
                            fic = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),Parametros.ficInv);
                            if (fic.exists()){
                                fic.delete();
                                // Fic Log
                                fic = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),Parametros.ficLog);
                                if (fic.exists()){
                                    fic.delete();
                                }
                                // Panta de aviso OK en Message
                                handler.sendEmptyMessage(2);
                            }
                        } else {
                            // Envia fic a host por FTP
                            if(!enviarInventarioFTP()){
                                salidaAlerta("Conexión de red",
                                        "Falla la conexión WiFi",
                                        "- Revise la conexión WiFi del terminal.",
                                        R.drawable.baseline_warning_amber_24, Parametros.DIALOGO_ALERT_SIN_BTN_ATRAS);
                            }
                        }
                    } else {
                        mContext.playSound(6);
                        salidaAlerta("No hay lecturas",
                                "No hay nada que enviar",
                                "- Realice lecturas de etiquetas.",
                                R.drawable.baseline_warning_amber_24, Parametros.DIALOGO_ALERT_SIN_BTN_ATRAS);
                    }
                }
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    //Estrategia elegida para bloquear la acción “Botón atrás” si hay inventario en marcha y puede ELIMINAR objetos en curso
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Así hay inventario en CURSO NO SE PUEDE ir Atrás ya que esto ELIMINARIA DATOS INVENTARIO
                if (mapTempDatas.isEmpty()){
                    this.setEnabled(false);
                    requireActivity().onBackPressed();
                } else {
                    // No se puede ir atrás si hay inventario en curso
                    mContext.playSound(5);
                    salidaAlerta("Atrás NO permitido",
                            "Abandonar solo si:",
                            "1.- Sesión de inventario guardada para seguir más tarde.\n\n" +
                                    "2.- Inventario finalizado y enviado al servidor.\n\n" +
                                    "3.- Sesión de inventario eliminada.",
                            R.drawable.baseline_warning_amber_24, Parametros.DIALOGO_ALERT_SIN_BTN_ATRAS);
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private boolean saveInventario(){

        String texto ="";

        // Fichero a guardar en el directorio estandard de Android \Download
        File fic = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),Parametros.ficInv);

        int etiLeidas=0;
        boolean errFic = false;

        try {
            // Strean de salida de tipo APPEND = true
            FileOutputStream f = new FileOutputStream(fic,true);

            Date date = new Date();
            DateFormat hourdateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss ");
            fechaHora = hourdateFormat.format(date);

            // "%FEC-" Debe ser el PRIMER registro obligado
            f.write("%FEC-".getBytes());
            f.write(fechaHora.getBytes());
            f.write("\n".getBytes());

            // version 4.3
            f.write("%FHI-".getBytes());
            f.write(mContext.fecHor_IniInv.getBytes()); // v.4.3 Fecha/Hora de 1era lectura de inventario para control de INACTIVADAS con Inventario en curso en f4sGestion_Mon
            f.write("\n".getBytes());

            f.write("%SES-".getBytes());
            f.write(String.valueOf(mContext.sesionInv).getBytes());
            f.write("\n".getBytes());

            f.write("%ETI-".getBytes());
            f.write(String.valueOf(mapTempDatas.size()).getBytes());
            f.write("\n".getBytes());

            SharedPreferences preferencias = mContext.getSharedPreferences(Parametros.PREFERENCIAS_app, MODE_PRIVATE);
            String selectedWarehouse = preferencias.getString("almacenSeleccionado", "");

            f.write("%ALM-".getBytes());
            f.write(selectedWarehouse.getBytes());
            f.write("\n".getBytes());

            f.write("%OBS-".getBytes());
            f.write(campo.getBytes());
            f.write("\n".getBytes());

            for (Map.Entry<String, Integer> entry : mapTempDatas.entrySet()) {
                f.write("%EPC-".getBytes());
                f.write(entry.getKey().getBytes());
                f.write("\n".getBytes());
                etiLeidas++;
            }

            f.write("INF--------------------".getBytes()); // Regsitro INF
            f.write("\n".getBytes()); // Salto de linea
            f.close();

            // Log de Actividad:
            texto = "%00%-" + fechaHora  + "->";
            UIHelper.writeLog(texto, false);
            texto = "%15%-Sesion: " + mContext.sesionInv;
            UIHelper.writeLog(texto, false);
            texto = "%16%-Etiquetas guardadas: " + etiLeidas;
            UIHelper.writeLog(texto, false);
            UIHelper.writeLog("%17%-Observación: " + campo, false);
            texto = "%18%---------------------";
            UIHelper.writeLog(texto, false);
        }
        catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage(), e);
            salidaAlerta("Error grave",
                    "No existe fichero a enviar",
                    "- Avise al Servicio Técnico.\n\n"+
                           "- Error: Envío FileNotFoundException",
                    R.drawable.baseline_warning_amber_24, Parametros.DIALOGO_ALERT_SIN_BTN_ATRAS);
            errFic = true;

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            salidaAlerta("Error grave",
                    "Excepción de fichero a enviar",
                    "- Avise al Servicio Técnico.\n\n"+
                            "- Error: Envío Exception",
                    R.drawable.baseline_warning_amber_24, Parametros.DIALOGO_ALERT_SIN_BTN_ATRAS);
            errFic = true;
        }

        if (errFic){
            salidaAlerta("ERROR de fichero",
                    "Problemas con fichero de inventario",
                    "- Avise al Servicio Técnico.",
                    R.drawable.baseline_warning_amber_24, Parametros.DIALOGO_ALERT_SIN_BTN_ATRAS);
            return false;
        }

        // Guarda valores
        SharedPreferences preferencias = mContext.getSharedPreferences(Parametros.PREFERENCIAS_app, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putInt(Parametros.pref_ETI_SESIONES_SAVE, mContext.etiSesionesSave+mapTempDatas.size());
        editor.putInt(Parametros.pref_SESIONES_INV, mContext.sesionInv);
        editor.putString(Parametros.pref_FECHA_HORA_SAVE, fechaHora);
        editor.putString(Parametros.pref_OBSERVACION_SAVE, campo);
        mContext.sesionInv ++; // Siguiente sesion en curso
        editor.apply();

        // Borrar la lista para ver qué EPCs han entrado nuevos en una sesión (si es lectura con Servicio hay .tagList y hay que borrar)
        mContext.tagList.clear();
        // Borra el diciconario que discrimian entre sesiones si un EPC no es repetido
        mapTempDatas.clear();

        // OK sin errores
        return true;
    }

    private boolean enviarInventarioFTP(){

        // 1º Conexion
        if (isOnline(mContext)) {
            connectToFTPAddress();
            return true;
        } else {
            return false; // Errores
        }
    }
    private void connectToFTPAddress(){

        ftpclient = new MyFTPClientFunctions();

        final String host = Parametros.hostFtp;
        final String username = Parametros.userFtp;
        final String password = Parametros.passFtp;

        pd = ProgressDialog.show(mContext, "", "Connecting...",
                true, false);

        new Thread(new Runnable() {
            public void run() {
                boolean status;
                status = ftpclient.ftpConnect(host, username, password, 21);
                if (status) {
                    // Asegura que existe o se crea un directorio en f4sFTP
                    if (ftpclient.ftpMakeDirectory(Parametros.dirFtp)){
                        dirCreado = "Carpeta " + Parametros.dirFtp + " creaada";
                    } else {
                        dirCreado = "Carpeta " + Parametros.dirFtp + " NO creaada";
                    }

                    // Envio

                    // JFA añade AQUI el CAMBIO de diretorio
                    // Importante!! -> ver en ftpChangeDirectory que PRIMERO se ha de partir de home (otra linea añadida por JFA)
                    // Y de ahi cambiar al dir siguiente indicado
                    ftpclient.ftpChangeDirectory(Parametros.dirFtp);

                    // Convierte el Parametros.ficInv INV_f4s.txt en un file tstamp => INV_f4s_aaammdd_hhmmss.txt
                    Calendar cal = Calendar.getInstance();
                    String stamp = "_"+
                            cal.get(Calendar.YEAR) +
                            String.format("%02d", cal.get(Calendar.MONTH)+1)+
                            String.format("%02d", cal.get(Calendar.DAY_OF_MONTH))+
                            "_"+
                            String.format("%02d", cal.get(Calendar.HOUR_OF_DAY))+
                            String.format("%02d", cal.get(Calendar.MINUTE))+
                            String.format("%02d", cal.get(Calendar.SECOND));

                    // Recupera el fic de inventario
                    File fic = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),Parametros.ficInv);
                    String ficStamp = Parametros.ficInv.replace(".txt", "") + stamp + ".txt";
                    // Envía a un file [tstamp].txt
                    status = ftpclient.ftpUpload(fic.getAbsoluteFile().toString(), ficStamp);

                    if (status) {
                        // Elimina fichero de inventario
                        UIHelper.writeLog("%60%-Inventario etiquetas = "+ (mContext.etiSesionesSave+mapTempDatas.size()), true);
                        UIHelper.writeLog("%61%-Fic EPCs borrado: " + fic.getName(), false);
                        UIHelper.writeLog("%62%-Fic EPCs enviado: " + ficStamp, false);
                        fic.delete();

                        // Panta de aviso OK en Message
                        handler.sendEmptyMessage(2);

                        // Aquí envía el fic de log - Recupera el fic
                        fic = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),Parametros.ficLog);
                        ficStamp = Parametros.ficLog.replace(".log", "") + stamp + ".log";

                        UIHelper.writeLog("%65%-Fic Log borrado: " + fic.getName(), false);
                        UIHelper.writeLog("%66%-Fic Log enviado: " + ficStamp, false);
                        UIHelper.writeLog("%67%---------------------", false);

                        // Envía a un file [tstamp].log
                        status = ftpclient.ftpUpload(fic.getAbsoluteFile().toString(), ficStamp);

                        if (status){
                            fic.delete();
                        }
                    } else {
                        handler.sendEmptyMessage(-2);
                    }

                } else {
                    handler.sendEmptyMessage(-1);
                }
            }
        }).start();
    }

    private boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    private void salidaAlerta(String argTitulo, String argSubTitulo, String argMsg, int image, String dialogo_alert){

        MiDialogAlert dialog = MiDialogAlert.newInstance(argTitulo, argSubTitulo, argMsg, image, dialogo_alert);
        FragmentManager manager = getFragmentManager();
        dialog.setTargetFragment(UHFReadTagFragment5.this, REQUEST_result);
        dialog.show(manager, DIALOGO);
    }
}


