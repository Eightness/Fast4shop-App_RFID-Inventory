package com.example.f4sINV.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.Menu;
import android.widget.Toast;

import com.example.f4sINV.Parametros;
import com.example.f4sINV.fragment.FragmentApp;
import com.example.f4sINV.fragment.FragmentInvCurso;
import com.example.f4sINV.fragment.PreferenciasFragment;
import com.example.f4sINV.fragment.PreferenciasReaderFragment;
import com.example.f4sINV.tools.UIHelper;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;

import com.example.f4sINV.R;

import java.io.File;


public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration mAppBarConfiguration;

    private DrawerLayout drawer;
    private NavigationView navigationView;

    private long mLastPress = 0; // Cuándo se pulsó atrás por última vez
    private String pantaActiva; // Pantalla Activity o Fragment lanzada
    
    private long mTimeLimit = 2000; // Límite de tiempo entre pulsaciones, en ms

    private boolean ultPreferencias = false; // Inidcador que la anterior tareas fue Preferencias -> actualizar el POJO
    private boolean ultPreferenciasReader = false; // Inidcador que la anterior tareas fue Preferencias del reader -> actualizar el POJO
    private boolean primeraVez = true; // Primera vez por arranque

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view_View);

        navigationView.setNavigationItemSelectedListener(item -> {
            // Si la anterior tarea fue en Preferencias, actualiza todas en el POJO por si ha cambiado alguna
            if (ultPreferencias) {
                actaPreferencias();
                ultPreferencias = false;
            }
            
            // Si la anterior tarea fue en Preferencias del Reader, actualiza todas en el POJO por si ha cambiado alguna
            if (ultPreferenciasReader){
                actaPreferenciasReader();
                ultPreferenciasReader = false;
            }

            displaySelectedScreen(item.getItemId());
            
            return true;
        });

        // Pedir permisos en Manifest.xml al usuario
        checkReadWritePermission();

        // Al inicio Carga en Parametros todas las preferencias
        actaPreferencias();
        actaPreferenciasReader();
    }

    private void displaySelectedScreen(int itemId){
        Fragment fragment = null;

        // Inicializa el objeto fragment seleccionado
        if (itemId == R.id.nav_invCurso) {
            pantaActiva = "nav_invCurso";
            fragment = new FragmentInvCurso();
        } else if (itemId == R.id.nav_inventario) {
            pantaActiva = "nav_inventario";
            Intent intent = new Intent(getApplicationContext(),TabInventarioActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.nav_tool) {
            pantaActiva = "nav_tool";
            Intent intent = new Intent(getApplicationContext(),TabToolsActivity.class);
            startActivity(intent);
        }else if (itemId == R.id.nav_app) {
            pantaActiva = "nav_app";
            fragment = new FragmentApp();
        } else if (itemId == R.id.nav_tagFinder) { // Tag Finder
            Intent intent = new Intent(getApplicationContext(), TagFinderActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.nav_warehouseSelector) { // Tag Finder
            Intent intent = new Intent(getApplicationContext(), RegisterWarehouseActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.nav_gestion) {
            Intent intent = new Intent(getApplicationContext(), GestionActivity.class);
            startActivity(intent);
        }

        // Reemplaza el fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.nav_host_fragment_content_main, fragment);
            ft.commit();
        }

        // Cierra siempre salvo la 1era vez que arranca
        if (primeraVez) {
            navigationView.getMenu().getItem(0).setChecked(true); // Queda seleccionado en pantalla
            drawer.openDrawer(GravityCompat.START);
            primeraVez =false;
        } else {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Destruye el proceso
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cuando vuelve de Actividades retorna abriendo la Bienvenida con datos de Inventario en cursp
        displaySelectedScreen(R.id.nav_invCurso);
        navigationView.getMenu().getItem(0).setChecked(true); // Queda seleccionado en pantalla Inv en curso
    }

    @Override
    public boolean onNavigateUp() {
        return super.onNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);
        // Habilita mostrar divisores.
        MenuCompat.setGroupDividerEnabled(menu, true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.opc_gral) {
            pantaActiva = "opc_gral";
            ultPreferencias = true; // Actualizar luego Preferencias
            Fragment fragment = new PreferenciasFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.nav_host_fragment_content_main, fragment);
            ft.commit();
        } else if (id == R.id.opc_reader) {
            pantaActiva = "opc_reader";
            ultPreferenciasReader = true; // Actualizar luego Preferencias
            Fragment fragment = new PreferenciasReaderFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.nav_host_fragment_content_main, fragment);
            ft.commit();
        } else if (id == R.id.opc_borrar_inv) {
            pantaActiva = "opc_borrar_inv";
            File fic = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Parametros.ficLog);
            if (fic.exists()) {

                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("¿Eliminar el inventario en curso?");
                dialog.setIcon(R.drawable.logo_f4s);
                dialog.setMessage("Se eliminarán los ficheros de inventario en curso y se reincia un nuevo inventario.");
                dialog.setCancelable(false);

                dialog.setPositiveButton(
                        R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                File fic = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),Parametros.ficLog);
                                fic.delete();
                                //Fic Log
                                fic = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),Parametros.ficInv);
                                if (fic.exists()){
                                    fic.delete();
                                }
                                salidaDialogo("Borrado Inventario en curso.", "- Eliminados los ficheros de inventario\n\n" + "- Inicie una nueva sesión de lecturas.",R.drawable.logo_f4s);

                                // Guarda valores de inicialización
                                SharedPreferences preferencias = MainActivity.this.getSharedPreferences(Parametros.PREFERENCIAS_app, MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferencias.edit();
                                editor.putInt(Parametros.pref_ETI_SESIONES_SAVE, 0);
                                editor.putInt(Parametros.pref_SESIONES_INV, 0);
                                editor.putString(Parametros.pref_FECHA_HORA_SAVE, "");
                                editor.putString(Parametros.pref_OBSERVACION_SAVE, "");
                                editor.apply();

                                // Reinicia el Fragment de Inventario en curso para que actualice en pantalla el inventario inicializado
                                displaySelectedScreen(R.id.nav_invCurso);
                                navigationView.getMenu().getItem(0).setChecked(true); // Queda seleccionado en pantalla Inventario en curso

                                dialog.cancel();
                            }
                        });

                dialog.setNegativeButton(
                        R.string.close,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                //dialog.dismiss();
                            }
                        });

                dialog.show();
                // Fundamental poner True para que NO repita 2 veces la salida de dialogAlert en menu Opciones
            } else {
                salidaDialogo("No hay Inventario en curso", "No se realiza ninguna acción",R.drawable.baseline_warning_amber_24);
                // Fundamental poner True para que NO repita 2 veces la salida de dialogAlert en menu Opciones
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void salidaDialogo(String tit, String msg, int img){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(tit)
                            .setMessage(msg)
                            .setCancelable(false)
                            .setIcon(img)
                            .setPositiveButton("ok", (dialog, which) -> {
                                // No hacer nada, se autocancela el dialogo
                            }).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if ("opc_gral".equals(pantaActiva) || "opc_reader".equals(pantaActiva)){
            // Si vuelve de opciones ir a panta inicial nav_invCurso
            displaySelectedScreen(R.id.nav_invCurso);
            navigationView.getMenu().getItem(0).setChecked(true); // Queda seleccionado en pantalla Inv en curso
        } else {
            if (mTimeLimit>1999){
                Toast onBackPressedToast = Toast.makeText(this,"Pulsa otra vez para salir de la aplicación", Toast.LENGTH_SHORT);
                long currentTime = System.currentTimeMillis();
                if (currentTime - mLastPress > mTimeLimit) {
                    onBackPressedToast.show();
                    mLastPress = currentTime;
                    // No ejecuta super.onBackPressed() => NO HAY SALIDA DE LA ACTIVITY
                } else {
                    onBackPressedToast.cancel();
                    moveTaskToBack(true); // Accion equivalente al boton Home de Android = simplemente baja la tarea
                    super.onBackPressed(); // Salida de la Activity
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    private void actaPreferencias(){
        // Ojo Las preferencias de usuario se acceden con PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences preferencias = PreferenceManager.getDefaultSharedPreferences(this);
        Parametros.hostFtp = preferencias.getString("hostFtp","192.168.1.180");
        Parametros.portFtp = preferencias.getString("portFtp","21");
        Parametros.userFtp = preferencias.getString("userFtp","f4sFTP");
        Parametros.passFtp = preferencias.getString("passFtp","f4sFTP2601");
        Parametros.dirFtp = preferencias.getString("dirFtp","Invf4s");
        Parametros.ficInv  = preferencias.getString("ficInv","INV_f4s.txt");
        Parametros.ficLog  = preferencias.getString("ficLog","INV_f4s.log");
        Parametros.enviosFtp  = preferencias.getBoolean("enviosFtp",true); // Por defecto envía por FTP
        Parametros.pitido  = preferencias.getBoolean("pitido",true); // Por defecto pita con nuevo EPC no repetido
        Parametros.servicio  = preferencias.getBoolean("servicio",false); // Por defecto trabajo con usuario
        Parametros.epcROB  = preferencias.getString("epcROB","3008"); // Máscara EPC 2 bytes HEX para detectar robo en puerta
        Parametros.epcINAC  = preferencias.getString("epcINAC","1111"); // Máscara EPC 2 bytes HEX de EPC inactivados

        //Vista
        vistaPreferencias();
    }

    private void actaPreferenciasReader(){
        // Ojo Las preferencias de usuario del Reader se acceden con PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences preferencias = PreferenceManager.getDefaultSharedPreferences(this);
        Parametros.reader = preferencias.getString("reader","Chainway");
        Parametros.potencia =  Integer.parseInt(preferencias.getString("potencia","30")); // Default 30 dBm
        Parametros.idxSesion = Integer.parseInt(preferencias.getString("idxSesion","1")); // Default idx array = S1
        Parametros.idxTarget = Integer.parseInt(preferencias.getString("idxTarget","0")); // Default udx array = A
        Parametros.idxRFLink = Integer.parseInt(preferencias.getString("idxRFLink","2")); // Default udx array = PR_ASK/Miller4/300KHz

        // Vista
        vistaPreferenciasReader();
    }

    private void vistaPreferencias(){
        UIHelper.writeLog("AJUSTES GENERALES ---------------", false);
        UIHelper.writeLog("hostFtp = " + Parametros.hostFtp, false);
        UIHelper.writeLog("portFtp = " + Parametros.portFtp, false);
        UIHelper.writeLog("userFtp = " + Parametros.userFtp, false);
        UIHelper.writeLog("passFtp = " + Parametros.passFtp, false);
        UIHelper.writeLog("dirFtp = " + Parametros.dirFtp, false);
        UIHelper.writeLog("ficInv = " + Parametros.ficInv, false);
        UIHelper.writeLog("ficLog = " + Parametros.ficLog, false);
        UIHelper.writeLog("enviosFtp = " + Parametros.enviosFtp, false);
        UIHelper.writeLog("pitido = " + Parametros.pitido, false);
        UIHelper.writeLog("servicio = " + Parametros.servicio, false);
        UIHelper.writeLog("epcROB = " + Parametros.epcROB, false);
        UIHelper.writeLog("epcINAC = " + Parametros.epcINAC, false);
    }

    private void vistaPreferenciasReader(){
        // Reader
        UIHelper.writeLog("AJUSTES DE READER: -------------", false);
        UIHelper.writeLog("reader = " + Parametros.reader, false);
        UIHelper.writeLog("potencia = " + Parametros.potencia, false);
        UIHelper.writeLog("idxSesion = " + Parametros.idxSesion, false);
        UIHelper.writeLog("idxTarget = " + Parametros.idxTarget, false);
        UIHelper.writeLog("idxRFLink = " + Parametros.idxRFLink, false);

    }

    private void checkReadWritePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 0);
                finish();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
            }
        }
        // Permiso para el auto-arranque de la App con el boot del móvil
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (! Settings.canDrawOverlays(this)) {
                    startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName())));
                }
            }
        } catch (Exception e){

        }
    }
}
