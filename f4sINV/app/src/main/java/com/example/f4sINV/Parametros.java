package com.example.f4sINV;

public class Parametros {

    public static final String PREFERENCIAS_app = "preferencias_app";
    public static final String EXTRA_ACCION= "com.example.f4sINV.intent.accion";
    public static final String EXTRA_EDIT_TEXT= "dialog.editText";
    public static final String DIALOGO_ALERT_CON_BTN_ATRAS = "dialog_con_atras";
    public static final String DIALOGO_ALERT_SIN_BTN_ATRAS = "dialog_sin_atras";
    public static final String DIALOGO_ACCION_CLEAR = "dialog_clear_sesion";
    public static final String DIALOGO_ACCION_SAVE = "dialog_save_sesion";
    public static final String DIALOGO_ACCION_FTP = "dialog_ftp_inventario";

    // Claves de Preferencias
    public static final String pref_SESIONES_INV = "sesionesInv";
    public static final String pref_ETI_SESIONES_SAVE = "etiSesionesSave";
    public static final String pref_FECHA_HORA_SAVE = "fechaHora";
    public static final String pref_OBSERVACION_SAVE = "observacion";

    public static String hostFtp;
    public static String portFtp;
    public static String userFtp;
    public static String passFtp;
    public static String dirFtp;
    public static String ficInv;
    public static String ficLog;
    public static String epcROB;
    public static String epcINAC;
    public static Boolean enviosFtp;
    public static Boolean pitido;
    public static Boolean servicio;

    // Del reader
    public static String reader;
    public static int potencia;
    public static int idxSesion;
    public static int idxTarget;
    public static int idxRFLink;

}
