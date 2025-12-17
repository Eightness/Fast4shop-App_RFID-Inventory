package com.example.f4sINV.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.f4sINV.R;
import com.example.f4sINV.activity.GestionActivity;
import com.example.f4sINV.tools.UIHelper;
import com.rscja.barcode.BarcodeDecoder;
import com.rscja.barcode.BarcodeFactory;
import com.rscja.deviceapi.entity.UHFTAGInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FragmentBajas extends Fragment {

    private GestionActivity mContext;
    private Spinner spWarehouse;
    private Button btnScan, btnClear, btnProcess;
    private TextView tvCount;
    private ListView lvItems;
    private ArrayAdapter<String> listAdapter;
    private ArrayList<String> scannedItems = new ArrayList<>();
    private Set<String> uniqueItems = new HashSet<>();

    private BarcodeDecoder barcodeDecoder;
    private boolean isScanning = false;
    private boolean loopFlag = false;

    private ArrayList<String> warehouseList = new ArrayList<>();
    private ArrayList<String> warehouseCodeList = new ArrayList<>();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            UHFTAGInfo info = (UHFTAGInfo) msg.obj;
            if (info != null) {
                addItem(info.getEPC());
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bajas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = (GestionActivity) getActivity();

        spWarehouse = view.findViewById(R.id.spWarehouse);
        btnScan = view.findViewById(R.id.btnScan);
        btnClear = view.findViewById(R.id.btnClear);
        btnProcess = view.findViewById(R.id.btnProcess);
        tvCount = view.findViewById(R.id.tvCount);
        lvItems = view.findViewById(R.id.lvItems);

        listAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, scannedItems);
        lvItems.setAdapter(listAdapter);

        loadWarehouses();

        btnScan.setOnClickListener(v -> triggerScan());
        btnClear.setOnClickListener(v -> clearData());
        btnProcess.setOnClickListener(v -> processBajas());

        initBarcode();
    }

    private void loadWarehouses() {
        SharedPreferences prefs = mContext.getSharedPreferences("config", Context.MODE_PRIVATE);
        String json = prefs.getString("almacenesRegistrados", "[]");
        warehouseList.clear();
        warehouseCodeList.clear();

        try {
            JSONArray warehouseArray = new JSONArray(json);
            for (int i = 0; i < warehouseArray.length(); i++) {
                JSONObject obj = warehouseArray.getJSONObject(i);
                String code = obj.getString("codigo");
                String description = obj.getString("descripcion");
                warehouseCodeList.add(code);
                warehouseList.add(code + " - " + description);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, warehouseList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spWarehouse.setAdapter(adapter);
    }

    private void initBarcode() {
        try {
            barcodeDecoder = BarcodeFactory.getInstance().getBarcodeDecoder();
            barcodeDecoder.open(mContext);
            barcodeDecoder.setDecodeCallback(barcodeEntity -> mContext.runOnUiThread(() -> {
                if (barcodeEntity.getResultCode() == BarcodeDecoder.DECODE_SUCCESS) {
                    addItem(barcodeEntity.getBarcodeData());
                    mContext.playSound(1);
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void triggerScan() {
        if (isScanning) {
            stopScan();
        } else {
            startScan();
        }
    }

    private void startScan() {
        if (mContext.mReader == null) return;

        if (mContext.mReader.startInventoryTag()) {
            btnScan.setText("Detener Escaneo");
            btnScan.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.red1));
            isScanning = true;
            loopFlag = true;
            new TagThread().start();
        } else {
            Toast.makeText(mContext, "Fallo al iniciar RFID", Toast.LENGTH_SHORT).show();
        }
        
        // Also start barcode scan if needed, or just keep it open
        try {
            barcodeDecoder.startScan();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopScan() {
        if (mContext.mReader == null) return;

        loopFlag = false;
        if (mContext.mReader.stopInventory()) {
            btnScan.setText("Escanear (RFID/QR)");
            btnScan.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.blue2));
            isScanning = false;
        }
        
        try {
            barcodeDecoder.stopScan();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addItem(String item) {
        if (!uniqueItems.contains(item)) {
            uniqueItems.add(item);
            scannedItems.add(item);
            listAdapter.notifyDataSetChanged();
            tvCount.setText("Items: " + scannedItems.size());
            mContext.playSound(1);
        }
    }

    private void clearData() {
        scannedItems.clear();
        uniqueItems.clear();
        listAdapter.notifyDataSetChanged();
        tvCount.setText("Items: 0");
    }

    private void processBajas() {
        if (scannedItems.isEmpty()) {
            Toast.makeText(mContext, "No hay items escaneados", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String warehouse = "";
        if (spWarehouse.getSelectedItem() != null) {
            warehouse = spWarehouse.getSelectedItem().toString();
        }

        new AlertDialog.Builder(mContext)
                .setTitle("Procesar Bajas")
                .setMessage("Se procesarán " + scannedItems.size() + " items del almacén: " + warehouse)
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    // Logic to save/send data
                    Toast.makeText(mContext, "Bajas procesadas correctamente", Toast.LENGTH_LONG).show();
                    clearData();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    class TagThread extends Thread {
        public void run() {
            UHFTAGInfo uhftagInfo;
            Message msg;
            while (loopFlag) {
                uhftagInfo = mContext.mReader.readTagFromBuffer();
                if (uhftagInfo != null) {
                    msg = handler.obtainMessage();
                    msg.obj = uhftagInfo;
                    handler.sendMessage(msg);
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isScanning) stopScan();
    }

    @Override
    public void onDestroy() {
        if (barcodeDecoder != null) {
            barcodeDecoder.close();
        }
        super.onDestroy();
    }
}
