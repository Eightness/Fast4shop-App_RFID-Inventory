package com.example.f4sINV.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.f4sINV.Parametros;
import com.example.f4sINV.R;
import com.example.f4sINV.activity.TabToolsActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class ToolInventarioFragment extends Fragment {
    private TextView tv_titulo;
    private TextView tv_log;
    private TabToolsActivity mContext;

    public ToolInventarioFragment() {
        // Required empty public constructor
    }

    public static ToolInventarioFragment newInstance() {
        ToolInventarioFragment fragment = new ToolInventarioFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tool_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContext = (TabToolsActivity) getActivity();

        tv_titulo = getView().findViewById(R.id.tv_titulo);
        tv_log = getView().findViewById(R.id.tv_log);

        // Guarda valor
        SharedPreferences preferencias = mContext.getSharedPreferences(Parametros.PREFERENCIAS_app, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putString(mContext.pref_PANTA, "TABtool2");
        editor.apply();

        salidaInventario();
    }
    private void salidaInventario () {
        tv_titulo.setText("EPCs en fichero." + Parametros.ficInv);
        if (Parametros.servicio){
            File fic = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Parametros.ficInv);

            String texto="";
            try {
                FileInputStream f = new FileInputStream(fic);
                BufferedReader entrada = new BufferedReader(new InputStreamReader(f));
                int n=0;
                String linea;
                do {
                    linea = entrada.readLine();
                    if (linea!=null){
                        texto+=linea+"\n";
                    }
                } while (linea!=null);
                f.close();
                tv_log.setText(texto);
            }
            catch (Exception ignored) {

            }
        } else {
            tv_log.setText("Ver etiquetas de Inventario en " + Parametros.ficInv);
        }
    }
}
