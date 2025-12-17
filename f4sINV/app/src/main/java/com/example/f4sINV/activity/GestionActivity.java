package com.example.f4sINV.activity;

import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.KeyEvent;

import com.example.f4sINV.Parametros;
import com.example.f4sINV.R;
import com.example.f4sINV.fragment.FragmentBajas;
import com.example.f4sINV.fragment.FragmentTraspasos;
import com.example.f4sINV.tools.UIHelper;
import com.google.android.material.tabs.TabLayout;

import java.util.HashMap;
import java.util.Objects;

public class GestionActivity extends BaseAppActivity {

    FragmentBajas fragmentBajas;
    FragmentTraspasos fragmentTraspasos;

    private String fragmentoActivo = "";

    HashMap<Integer, Integer> soundMap = new HashMap<>();
    private SoundPool soundPool;
    private AudioManager am;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion);

        // Initialize Tabs
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setText("BAJAS").setTag("BAJAS"));
        tabs.addTab(tabs.newTab().setText("TRASPASOS").setTag("TRASPASOS"));

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectFragment(Objects.requireNonNull(tab.getTag()).toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        // Initialize UHF and Sound
        initUHF();
        initSound();

        // Default selection
        selectFragment("BAJAS");
    }

    private void selectFragment(String keyTab) {
        fragmentoActivo = keyTab;

        switch (keyTab) {
            case "BAJAS":
                if (fragmentBajas == null) {
                    fragmentBajas = new FragmentBajas();
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragtab, fragmentBajas)
                        .commit();
                break;
            case "TRASPASOS":
                if (fragmentTraspasos == null) {
                    fragmentTraspasos = new FragmentTraspasos();
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragtab, fragmentTraspasos)
                        .commit();
                break;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == 4) { // Back button
            this.onBackPressed();
            return false;
        }

        // Handle physical scan buttons (C72/C5/C66)
        if (keyCode == 293 || keyCode == 291) {
            if ("BAJAS".equals(fragmentoActivo) && fragmentBajas != null) {
                fragmentBajas.triggerScan();
                return true;
            } else if ("TRASPASOS".equals(fragmentoActivo) && fragmentTraspasos != null) {
                fragmentTraspasos.triggerScan();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        releaseSoundPool();
        if (mReader != null) {
            mReader.free();
        }
        super.onDestroy();
    }

    private void initSound() {
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
        soundMap.put(1, soundPool.load(this, R.raw.barcodebeep, 1));
        soundMap.put(2, soundPool.load(this, R.raw.beep_stop_read, 1));
        soundMap.put(3, soundPool.load(this, R.raw.beep_error, 1));
        soundMap.put(4, soundPool.load(this, R.raw.beep_ftp_ok, 1));
        soundMap.put(5, soundPool.load(this, R.raw.beep_warning_soft, 1));
        soundMap.put(6, soundPool.load(this, R.raw.beep_warning_hard, 1));

        am = (AudioManager) this.getSystemService(AUDIO_SERVICE);
    }

    public void playSound(int id) {
        float audioMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float audioCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        float volumnRatio = audioCurrentVolume / audioMaxVolume;
        try {
            soundPool.play(soundMap.get(id), volumnRatio, volumnRatio, 1, 0, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseSoundPool() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
