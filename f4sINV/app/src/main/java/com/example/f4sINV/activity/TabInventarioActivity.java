package com.example.f4sINV.activity;

import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;

import com.example.f4sINV.Parametros;
import com.example.f4sINV.UhfInfo;
import com.example.f4sINV.R;
import com.example.f4sINV.fragment.UHFReadTagFragment2;
import com.example.f4sINV.fragment.UHFReadTagFragment4;
import com.example.f4sINV.fragment.UHFReadTagFragment5;
import com.example.f4sINV.tools.UIHelper;
import com.google.android.material.tabs.TabLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class TabInventarioActivity extends BaseAppActivity {
    UHFReadTagFragment2 uhfReadTagFragment2;
    UHFReadTagFragment4 uhfReadTagFragment4;
    UHFReadTagFragment5 uhfReadTagFragment5;

    // Para saber en qué fragmento está y activar el gatillo de C72 o C5, o el botón scan de C5
    private String fragmentoActivo = "";

    public UhfInfo uhfInfo = new UhfInfo();
    public ArrayList<HashMap<String, String>> tagList = new ArrayList<>();

    HashMap<Integer, Integer> soundMap = new HashMap<>();
    private SoundPool soundPool;
    private AudioManager am;

    // JFA Variables de PREFERENCIAS
    public static final String pref_PANTA = "panta";

    // Resumen de inventario en curso
    public int sesionesInv; // Sesiones inventario guardadas
    public int etiSesionesSave; // Etiquetas guardadas
    public int sesionInv; // Sesión inventario en curso
    public boolean iniSesion= true; // v.4.3 Para guardar Fecha/Hora en 1era lectura de inventario
    public String fecHor_IniInv; // v.4.3 Fecha/Hora de 1era lectura de inventario para control de INACTIVADAS con Inventario en curso en f4sGestion_Mon

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_inventario);

        // Guarda valor
        SharedPreferences preferencias = getSharedPreferences(Parametros.PREFERENCIAS_app, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putString(pref_PANTA, "PANTA2"); // Inicializa siempre con PANTA2
        editor.apply();

        sesionesInv = preferencias.getInt(Parametros.pref_SESIONES_INV, 0);
        sesionInv  = sesionesInv +1;   //Sigueinte sesion en curso
        etiSesionesSave = preferencias.getInt(Parametros.pref_ETI_SESIONES_SAVE, 0);

        // Pestaña
        // Código de 3 Tabs sin VewPager en versión Prueba 10.0
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setText("INVENTARIO").setTag("PANTA2"));
        tabs.addTab(tabs.newTab().setText("ETI. INACTIVAS").setTag("PANTA4"));
        tabs.addTab(tabs.newTab().setText("TRASPASO").setTag("PANTA5"));

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Saca la pestaña que corresponda
                selectFragment(Objects.requireNonNull(tab.getTag()).toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // v.3.0
        uhfInfo.setErrF4s(0); // Inicializa errores de traspaso entre fragments

        if (!readFileIni()){
            uhfInfo.setErrF4s(1); // Error en fic INV_f4s.ini
        }

        //Iniciar UHF y sonidos
        initUHF();
        initSound();

        // Recupera version SW Chainway
        UIHelper.writeLog("Versión SW Chainway: " + getSWVersion(), false);

        // Recupera Frecuencia de zona
        UIHelper.writeLog("Frecuencia: " + getFreq(), false);

        // Recupera Potencia
        UIHelper.writeLog("Potencia: " + getPower(), false);

        getParamEPC();  //Métdod para recuperar de tacada las descripciones de los param EPC sesion, target AB y Q
        UIHelper.writeLog("Id Sesion: " + sesion, false);
        UIHelper.writeLog("Target: " + target, false);
        UIHelper.writeLog("Valor Q: " + qVal, false);

        // Recupera RF Link
        UIHelper.writeLog("RF Link: " + getRFLink(), false);
    }

   @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
       if (keyCode == 4){   //4= atrás Android
           this.onBackPressed();
           return false; // Indicar a Android que hemos manejado el evento y no lo propague más
       }
       // Para escanear RFID con gatillo o pulsadores asegurar que estamos en el Fragmeent de escanear RFID = "PANTA2"
       if (!("PANTA2".equals(fragmentoActivo))) {
            return false; // Indicar a Android que no hemos manejado el evento y lo propague
       }
        // Códigos:
        // C72 y C5 -------------
        // Gatillo de pistola = 293,, Btn derecho/izquierdo scan =
        // C66 ------------- sin gatillo de pistola
        // Btn derecho = 293 / izquierdo = 291
        if (keyCode == 293 || keyCode == 291){
            // Lanzar el método público de iniciar/parar escaneo
            uhfReadTagFragment2.readTag();
            return true; // Indicar a Android que hemos manejado el evento y no lo propague más
        }
        return false; // Indicar a Android que no hemos manejado el evento y lo propague
    }

    @Override
    protected void onDestroy() {
        releaseSoundPool();
        if (mReader != null) {
            mReader.free();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Guarda valor
        SharedPreferences preferencias = getSharedPreferences(Parametros.PREFERENCIAS_app, MODE_PRIVATE);
        String panta = preferencias.getString(pref_PANTA, "PANTA2");

        // Recupera la pestaña que corresponda
        selectFragment(panta);
    }

    private void selectFragment(String keyTab){

        // Guarda el fragmento Activo para los eventos de gatillo y botones
        fragmentoActivo =keyTab;

        switch (keyTab){
            case "PANTA2":
                if(uhfReadTagFragment2 ==null) {
                    // Ejemplo de envio de dos parametros
                    uhfReadTagFragment2 = UHFReadTagFragment2.newInstance();
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragtab, uhfReadTagFragment2)
                        .commit();
                break;
            case "PANTA4":
                if(uhfReadTagFragment4 ==null) {
                    // Ejemplo de envio de dos parametros
                    uhfReadTagFragment4 = UHFReadTagFragment4.newInstance();
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragtab, uhfReadTagFragment4)
                        .commit();
                break;
            case "PANTA5":
                if(uhfReadTagFragment5 ==null) {
                    // Ejemplo de envio de dos parametros
                    uhfReadTagFragment5 = UHFReadTagFragment5.newInstance();
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragtab, uhfReadTagFragment5)
                        .commit();
                break;
        }

    }

    // v.3.0 - Por si alguna vez se quiere utilizar un fic .INI
    private boolean readFileIni(){
        FileInputStream is;

        final File fic = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "INV_f4s.ini");

        if (fic.exists())
        {
            try {
                is = new FileInputStream(fic);
            } catch (IOException e) {
                return false;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String linea;
                String reg;
                while ((linea = reader.readLine()) != null) {
                    if ( linea.length() > 7){
                        reg = linea.substring(0,6);
                        // Con la primera marca de "******" deja de leer el fichero
                        if (reg.equals("******")){
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        } else {
            return false;
        }

    }

    private void initSound() {
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
        soundMap.put(1, soundPool.load(this, R.raw.barcodebeep, 1));
        soundMap.put(2, soundPool.load(this, R.raw.beep_stop_read, 1));
        soundMap.put(3, soundPool.load(this, R.raw.beep_error, 1));
        soundMap.put(4, soundPool.load(this, R.raw.beep_ftp_ok, 1));
        soundMap.put(5, soundPool.load(this, R.raw.beep_warning_soft, 1));
        soundMap.put(6, soundPool.load(this, R.raw.beep_warning_hard, 1));

        am = (AudioManager) this.getSystemService(AUDIO_SERVICE); // AudioManager
    }

    public void playSound(int id) {
        float audioMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // AudioManager
        float audioCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);// AudioManager
        float volumnRatio = audioCurrentVolume / audioMaxVolume;
        try {
            soundPool.play(soundMap.get(id), volumnRatio,
                    volumnRatio,
                    1,
                    0,
                    1
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseSoundPool() {
        if(soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
