package com.example.f4sINV.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.f4sINV.R;
import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class TagFinderActivity extends Activity {

    private static final String TAG = "TagFinderActivity";

    // RFID Variables
    private RFIDWithUHFUART mReader;
    private boolean readerInitialized = false;
    private AtomicBoolean isScanning = new AtomicBoolean(false);
    private Handler scanHandler = new Handler();
    private static final int SCAN_INTERVAL_MS = 100;   // Frecuencia de lectura del buffer
    private static final int BEEP_INTERVAL_MS = 250;   // Radar: beep cada 250 ms

    // UI Components
    private TextView tvResult, textViewPowerValue;
    private EditText editTextTagId, editTextDescription;
    private CheckBox checkBoxDeactivated;
    private SeekBar seekBarPower;
    private Button btnScanToggle;

    // Sound
    private SoundPool soundPool;
    private HashMap<Integer, Integer> soundMap = new HashMap<>();
    private AudioManager am;
    private long lastBeepTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_finder);

        // UI References
        tvResult = findViewById(R.id.tvResult);
        textViewPowerValue = findViewById(R.id.textViewPowerValue);
        editTextTagId = findViewById(R.id.editTextTagId);
        editTextDescription = findViewById(R.id.editTextDescription);
        checkBoxDeactivated = findViewById(R.id.checkBoxDeactivated);
        seekBarPower = findViewById(R.id.seekBarPower);
        btnScanToggle = findViewById(R.id.btnScanToggle);

        // Button disabled until the reader is initialized
        btnScanToggle.setEnabled(false);

        btnScanToggle.setOnClickListener(v -> toggleScan());

        initSound();       // Initialize sound
        initPowerSeekBar();// Configure conversion 1-100% -> 1-30 dBm
        new InitTask().execute(); // Initialize RFID reader with indicator
    }

    // UHF reader initialization
    private class InitTask extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(TagFinderActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("Inicializando lector UHF...");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                mReader = RFIDWithUHFUART.getInstance();
                return mReader != null && mReader.init(this.dialog.getContext());
            } catch (Exception e) {
                Log.e(TAG, "Error inicializando lector UHF", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (dialog != null && dialog.isShowing()) dialog.dismiss();

            if (result) {
                readerInitialized = true;
                btnScanToggle.setEnabled(true);
                Toast.makeText(TagFinderActivity.this, "Lector UHF inicializado correctamente", Toast.LENGTH_SHORT).show();
            } else {
                readerInitialized = false;
                Toast.makeText(TagFinderActivity.this, "Error al inicializar el lector UHF", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Convert potency 1-100% -> 1-30 dBm
    private void initPowerSeekBar() {
        seekBarPower.setMax(100);
        seekBarPower.setProgress(100);

        seekBarPower.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 1) progress = 1;
                int realPower = 1 + (progress * 29 / 100);
                textViewPowerValue.setText("Potencia: " + progress + "% (" + realPower + " dBm)");

                if (mReader != null && readerInitialized) {
                    mReader.setPower(realPower);
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    // Alternate scanning
    private void toggleScan() {
        if (!isScanning.get()) {
            String code = editTextTagId.getText().toString().trim();
            String counter = editTextDescription.getText().toString().trim();

            if (TextUtils.isEmpty(code) || TextUtils.isEmpty(counter)) {
                Toast.makeText(this, "Debe rellenar Código de artículo y Contador antes de iniciar.", Toast.LENGTH_LONG).show();
                return;
            }

            // Disable fields
            editTextTagId.setEnabled(false);
            editTextDescription.setEnabled(false);
            seekBarPower.setEnabled(false);
            checkBoxDeactivated.setEnabled(false);

            startScan();
            btnScanToggle.setText(R.string.detener_escaneo);
            isScanning.set(true);
        } else {
            stopScan();
            btnScanToggle.setText(R.string.iniciar_escaneo);
            isScanning.set(false);

            // Enable fields
            editTextTagId.setEnabled(true);
            editTextDescription.setEnabled(true);
            seekBarPower.setEnabled(true);
            checkBoxDeactivated.setEnabled(true);
        }
    }


    // Start continuous scan
    private void startScan() {
        if (mReader == null || !readerInitialized) {
            Toast.makeText(this, "Lector no inicializado.", Toast.LENGTH_SHORT).show();
            return;
        }

        mReader.startInventoryTag();
        tvResult.setText("Escaneando etiquetas...");

        scanHandler.post(scanRunnable);
    }

    // Continuous scanning from buffer Runnable
    private Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isScanning.get()) return;

            UHFTAGInfo tagInfo = mReader.readTagFromBuffer();
            if (tagInfo != null) {
                processTag(tagInfo.getEPC());
            }

            scanHandler.postDelayed(this, SCAN_INTERVAL_MS);
        }
    };

    // Process each EPC
    private void processTag(String epc) {
        runOnUiThread(() -> tvResult.setText("EPC detectado:\n" + epc));

        // Get checkbox value
        boolean isInactiveMode = checkBoxDeactivated.isChecked();
        String requiredPrefix = isInactiveMode ? "1111" : "3008";

        // Skipping if the prefix is not valid
        if (!epc.startsWith(requiredPrefix)) {
            return;
        }

        // If we are here, the EPC has a correct mask active/inactive
        String codArticuloDec = editTextTagId.getText().toString().trim();
        String contadorDec = editTextDescription.getText().toString().trim();

        if (epc.length() >= 24) {
            String codArticuloHex = epc.substring(8, 16);
            String contadorHex = epc.substring(16, 24);

            String codArticuloInputHex = String.format("%08X", Integer.parseInt(codArticuloDec));
            String contadorInputHex = String.format("%08X", Integer.parseInt(contadorDec));

            if (codArticuloHex.equalsIgnoreCase(codArticuloInputHex)
                    && contadorHex.equalsIgnoreCase(contadorInputHex)) {

                long now = System.currentTimeMillis();
                if (now - lastBeepTime >= BEEP_INTERVAL_MS) {
                    playSound(3); // Radar sound
                    lastBeepTime = now;
                }
            }
        }
    }

    // Stop scanning
    private void stopScan() {
        isScanning.set(false);
        scanHandler.removeCallbacks(scanRunnable);

        if (mReader != null) {
            mReader.stopInventory();
        }

        tvResult.setText("Escaneo detenido");
    }

    // Sound
    private void initSound() {
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
        soundMap.put(1, soundPool.load(this, R.raw.barcodebeep, 1));
        soundMap.put(2, soundPool.load(this, R.raw.beep_stop_read, 1));
        soundMap.put(3, soundPool.load(this, R.raw.beep_error, 1));
        soundMap.put(4, soundPool.load(this, R.raw.beep_ftp_ok, 1));
        soundMap.put(5, soundPool.load(this, R.raw.beep_warning_soft, 1));
        soundMap.put(6, soundPool.load(this, R.raw.beep_warning_hard, 1));

        am = (AudioManager) getSystemService(AUDIO_SERVICE);
    }

    private void playSound(int id) {
        float maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        float volumeRatio = currentVolume / maxVolume;

        soundPool.play(soundMap.get(id), volumeRatio, volumeRatio, 1, 0, 1);
    }

    private void releaseSoundPool() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    @Override
    protected void onDestroy() {
        stopScan();
        releaseSoundPool();
        if (mReader != null) {
            mReader.free();
        }
        super.onDestroy();
    }
}
