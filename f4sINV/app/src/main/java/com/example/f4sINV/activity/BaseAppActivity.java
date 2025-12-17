package com.example.f4sINV.activity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.Gen2Entity;


public class BaseAppActivity extends AppCompatActivity {

    private static final String TAG = "BaseAppActivity";

    public RFIDWithUHFUART mReader;

    private static final String[] listSesion = {"S0","S1","S2","S3"};
    private static final String[] listTarget = {"A","B","AB"};
    private static final String[] listFreq = {
            "0 -Ni se sabe qué zona (¿?MHz)",
            "1 -Ni se sabe qué zona (¿?MHz)",
            "2 -Ni se sabe qué zona (¿?MHz)",
            "3 -Ni se sabe qué zona (¿?MHz)",
            "ETSI Standard (865-868MHz)",
            "Otros..."};
    private static final String[] listRFLink = {
            "DSB_ASK/FM0/40KHz",
            "PR_ASK/Miller4/250KHz",
            "PR_ASK/Miller4/300KHz",
            "DSB_ASK/FM0/400KHz"};

    public String sesion, target, qVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void initUHF() {
        try {
            Log.i(TAG, "initUHF() try");
            mReader = RFIDWithUHFUART.getInstance();
        } catch (Exception ex) {
            Log.i(TAG, "Exception ex)");
            toastMessage("wow");
            return;
        }

        if (mReader != null) {
            //Inicializa reader
            new InitTask().execute();
        }
    }

    public void toastMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    public void getParamEPC(){
        // Carga la configuración por defecto
        sesion = "Sin IdSesión"; // ID Sesion S0, S1, S2, S3
        target = "Sin target A/B"; // Target A, B o AB
        qVal = "Sin Q"; // Target A, B o AB
        int idx;
        Gen2Entity p = mReader.getGen2();

        if (p != null) {
            idx = p.getQuerySession();
            if (idx != -1) {
                sesion = "Idx:" + idx + " - " + listSesion[p.getQuerySession()];
            }
            idx = p.getQueryTarget();
            if (idx != -1) {
                target = "Idx:" + idx + " - " + listTarget[idx];
            }
            idx = p.getQ();
            if (idx != -1) {
                qVal = "Valor Q = " + idx;
            }
        }
    }

    public boolean setParamEPC(int idxSesion, int idxTarget) {
        if (mReader != null) {
            Gen2Entity p= mReader.getGen2();

            if (p != null) {
                p.setQuerySession(idxSesion);
                p.setQueryTarget(idxTarget);
                return mReader.setGen2(p);
            }
        }
        return false;
    }

    public String getPower() {
        String result = "Falla Reader";

        if (mReader != null) {
            int idx = mReader.getPower();
            if (idx != -1) {
                result = idx + " mW";
            }
        } else {
            result = "Reader no conectado";
        }
        return result;
    }

    public boolean setPower(int mDb){
        return mReader.setPower(mDb);
    }

    public String getFreq() {
        String result = "Falla Reader";
        if (mReader != null) {
            int idx = mReader.getFrequencyMode();
            if (idx != -1) {
                int count = listFreq.length;
                result = "Idx:" + idx + " - " + listFreq[Math.min(idx, count - 1)];
            }
        } else {
            result =  "Reader no conectado";
        }
        return result;
    }

    public String getRFLink() {
        String result = "Falla Reader";

        if (mReader != null) {
            int idx = mReader.getRFLink();
            if (idx != -1) {
                int count = listRFLink.length;
                result = "Idx:" + idx + " - " + listRFLink[Math.min(idx, count - 1)];
            }
        } else {
            result = "Reader no conectado";
        }
        return result;
    }

    public boolean setRFLink(int idx){
        return mReader.setRFLink(idx);
    }

    public String getSWVersion() {
        if (mReader != null) {
            return mReader.getVersion();
        }

        return "Sin Reader conectado";
    }

    public class InitTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;

        @Override
        protected Boolean doInBackground(String... params) {
            return mReader.init(this.mypDialog.getContext());
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mypDialog.cancel();
            if (!result) {
                Toast.makeText(BaseAppActivity.this, "init fail", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mypDialog = new ProgressDialog(BaseAppActivity.this);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setMessage("init...");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }
    }
}
