package com.example.f4sINV.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.f4sINV.R;
import com.example.f4sINV.adapter.WarehouseAdapter;
import com.example.f4sINV.model.Warehouse;
import com.rscja.barcode.BarcodeDecoder;
import com.rscja.barcode.BarcodeFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class RegisterWarehouseActivity extends Activity implements WarehouseAdapter.OnWarehouseActionListener {

    private static final String TAG = "RegisterWarehouseActivity";
    private static final String PREFS_KEY = "almacenesRegistrados";
    private static final String PREFS_ACTIVE_KEY = "almacenActivo";

    private BarcodeDecoder barcodeDecoder;
    private Button btnScanToggle;
    private RecyclerView recyclerView;
    private WarehouseAdapter adapter;
    private List<Warehouse> warehouseList = new ArrayList<>();
    private TextView tvSelect;

    private boolean isScanning = false;
    private String activeWarehouseCode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_warehouse);

        btnScanToggle = findViewById(R.id.btnScanToggle);
        recyclerView = findViewById(R.id.recyclerViewWarehouses);
        tvSelect = findViewById(R.id.tvSelect);
        tvSelect.setVisibility(View.GONE); // Not visible by default

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WarehouseAdapter(warehouseList, this);
        recyclerView.setAdapter(adapter);

        activeWarehouseCode = getSharedPreferences("config", MODE_PRIVATE).getString(PREFS_ACTIVE_KEY, null);
        adapter.setActiveWarehouse(activeWarehouseCode);

        barcodeDecoder = BarcodeFactory.getInstance().getBarcodeDecoder();
        btnScanToggle.setOnClickListener(v -> toggleScan());

        loadWarehouseList();
        new InitTask().execute();
    }

    // Corrects characters (ej: ISO-8859-1 → UTF-8)
    private String fixEncoding(String input) {
        // Does not work because the characters are interpreted as "?" from the chainway engine, it seems like is not possible to change the encode (investigating)
        try {
            return new String(input.getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return input;
        }
    }

    private class InitTask extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(RegisterWarehouseActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("Inicializando escáner...");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            openScanner();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (dialog != null && dialog.isShowing()) dialog.dismiss();
        }
    }

    private void openScanner() {
        try {
            barcodeDecoder.open(this);
            barcodeDecoder.setParameter(1001, 1);
            barcodeDecoder.setDecodeCallback(barcodeEntity -> runOnUiThread(() -> {
                if (barcodeEntity.getResultCode() == BarcodeDecoder.DECODE_SUCCESS) {
                    String data = fixEncoding(barcodeEntity.getBarcodeData());
                    processQRCode(data);
                } else {
                    handleScanError("Error al leer el código");
                }
            }));
        } catch (Exception e) {
            Log.e(TAG, "Error abriendo escáner", e);
        }
    }

    private void toggleScan() {
        if (!isScanning) {
            startScan();
            btnScanToggle.setText(R.string.detener_escaneo);
            isScanning = true;
        } else {
            stopScan();
            btnScanToggle.setText(R.string.iniciar_escaneo);
            isScanning = false;
        }
    }

    private void startScan() {
        try {
            barcodeDecoder.startScan();
        } catch (Exception e) {
            handleScanError("Error al iniciar el escaneo.");
        }
    }

    private void stopScan() {
        try {
            barcodeDecoder.stopScan();
        } catch (Exception e) {
            Log.e(TAG, "Error al detener escaneo", e);
        }
    }

    private void handleScanError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        stopScan();
        isScanning = false;
        btnScanToggle.setText(R.string.iniciar_escaneo);
    }

    private void processQRCode(String qrData) {
        Log.i(TAG, "QR leído: " + qrData);

        if (qrData != null && qrData.startsWith("%") && qrData.endsWith("@") && qrData.contains("&")) {
            try {
                int ampIndex = qrData.indexOf("&");
                int atIndex = qrData.lastIndexOf("@");

                String codigo = qrData.substring(1, ampIndex).trim();
                String descripcion = qrData.substring(ampIndex + 1, atIndex).trim();

                if (!codigo.isEmpty() && !descripcion.isEmpty()) {
                    boolean added = addWarehouse(codigo, descripcion);
                    stopScan();
                    isScanning = false;
                    btnScanToggle.setText(R.string.iniciar_escaneo);
                    showWarehouseDialog(codigo, descripcion, added);
                    return;
                }
            } catch (Exception e) {
                handleScanError("Error procesando QR");
                return;
            }
        }
        handleScanError("Código QR no válido. Formato esperado: %c&d@");
    }

    private boolean addWarehouse(String codigo, String descripcion) {
        try {
            JSONArray almacenes = loadWarehouseJSONArray();
            HashSet<String> codigosExistentes = new HashSet<>();

            for (int i = 0; i < almacenes.length(); i++) {
                JSONObject obj = almacenes.getJSONObject(i);
                codigosExistentes.add(obj.getString("codigo"));
            }

            if (!codigosExistentes.contains(codigo)) {
                almacenes.put(new JSONObject().put("codigo", codigo).put("descripcion", descripcion));
                saveWarehouseJSONArray(almacenes);
                warehouseList.add(new Warehouse(codigo, descripcion));
                adapter.notifyItemInserted(warehouseList.size() - 1);
                tvSelect.setVisibility(View.VISIBLE);
                return true;
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error guardando almacén", e);
        }
        return false;
    }

    private void showWarehouseDialog(String codigo, String descripcion, boolean added) {
        String title = added ? "Almacén registrado" : "Almacén ya existente";
        String msg = (added ? "Se ha registrado:\n\n" : "Ya existía:\n\n") +
                "Código: " + codigo + "\nDescripción: " + descripcion;

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("Aceptar", (d, w) -> d.dismiss())
                .show();
    }

    private JSONArray loadWarehouseJSONArray() {
        try {
            String json = getSharedPreferences("config", MODE_PRIVATE).getString(PREFS_KEY, "[]");
            return new JSONArray(json);
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    private void saveWarehouseJSONArray(JSONArray almacenes) {
        getSharedPreferences("config", MODE_PRIVATE)
                .edit()
                .putString(PREFS_KEY, almacenes.toString())
                .apply();
    }

    private void loadWarehouseList() {
        warehouseList.clear();
        JSONArray almacenes = loadWarehouseJSONArray();
        for (int i = 0; i < almacenes.length(); i++) {
            try {
                JSONObject obj = almacenes.getJSONObject(i);
                warehouseList.add(new Warehouse(
                        obj.getString("codigo"),
                        obj.getString("descripcion")
                ));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (warehouseList.size() > 0) {
            tvSelect.setVisibility(View.VISIBLE);
        } else {
            tvSelect.setVisibility(View.GONE);
        }

        adapter.setActiveWarehouse(activeWarehouseCode);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        stopScan();
        try {
            barcodeDecoder.close();
        } catch (Exception e) {
            Log.e(TAG, "Error cerrando escáner", e);
        }
        super.onDestroy();
    }

    // CALLBACKS DEL ADAPTER

    @Override
    public void onDeleteClicked(int position) {
        // Eliminar de lista y SharedPreferences
        Warehouse toDelete = warehouseList.get(position);
        warehouseList.remove(position);

        // Si se elimina el seleccionado, limpiamos selección
        if (toDelete.getCodigo().equals(activeWarehouseCode)) {
            activeWarehouseCode = null;
            getSharedPreferences("config", MODE_PRIVATE).edit().remove(PREFS_ACTIVE_KEY).apply();
        }

        // Actualizamos JSON persistido
        JSONArray newArray = new JSONArray();
        for (Warehouse w : warehouseList) {
            try {
                newArray.put(new JSONObject().put("codigo", w.getCodigo()).put("descripcion", w.getDescripcion()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (warehouseList.isEmpty()) {
            tvSelect.setVisibility(View.GONE);
        }

        saveWarehouseJSONArray(newArray);
        adapter.setActiveWarehouse(activeWarehouseCode);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClicked(int position) {
        // Select active warehouse
        Warehouse selected = warehouseList.get(position);
        activeWarehouseCode = selected.getCodigo();

        getSharedPreferences("config", MODE_PRIVATE)
                .edit()
                .putString(PREFS_ACTIVE_KEY, activeWarehouseCode)
                .apply();

        adapter.setActiveWarehouse(activeWarehouseCode);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Almacén " + selected.getCodigo() + " seleccionado por defecto.", Toast.LENGTH_LONG).show();
    }
}
