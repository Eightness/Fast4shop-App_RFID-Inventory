package com.example.f4sINV.activity;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.f4sINV.Parametros;
import com.example.f4sINV.R;
import com.example.f4sINV.fragment.ToolInventarioFragment;
import com.example.f4sINV.fragment.ToolLogFragment;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;


public class TabToolsActivity extends AppCompatActivity {

    private static final String TAG = "TabToolsActivity";

    public static final String pref_PANTA = "pantaTabTools";
    TabLayout tabs;

    private ToolLogFragment toolLogFragment;
    private ToolInventarioFragment toolInventarioFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_tools);

        // Guarda los valores por defecto de inicio en Preferencias si es la 1º carga de la Apll
        SharedPreferences preferencias = getSharedPreferences(Parametros.PREFERENCIAS_app, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putString(pref_PANTA, "TABtool1");        //Inicializa siempre con PANTA2
        editor.apply();


        // Pestaña
        // Código de 3 Tabs sin VewPager en version Prueba 10.0
        tabs = findViewById(R.id.tabs_tools);
        tabs.addTab(tabs.newTab().setText("ACTIVIDAD").setTag("TABtool1"));
        tabs.addTab(tabs.newTab().setText("INVENTARIO").setTag("TABtool2"));

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectFragment(Objects.requireNonNull(tab.getTag()).toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences preferencias = getSharedPreferences(Parametros.PREFERENCIAS_app, MODE_PRIVATE);
        String panta = preferencias.getString(pref_PANTA, "TABtool1");

        // Recupera la pestaña que corresponda
        selectFragment(panta);
    }

    private void selectFragment(String keyTab){
        switch (keyTab){
            case "TABtool1":
                if(toolLogFragment ==null) {
                    // Ejemplo de envío de dos parámetros
                    toolLogFragment = ToolLogFragment.newInstance();
                }

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragtab, toolLogFragment)
                        .commit();
                break;
            case "TABtool2":
                if (toolInventarioFragment == null) {
                    // Ejemplo de envío de dos parámetros
                    toolInventarioFragment = toolInventarioFragment.newInstance();
                }

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragtab, toolInventarioFragment)
                        .commit();
                break;
        }
    }
}
