package com.example.f4sINV.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.f4sINV.Parametros;
import com.example.f4sINV.activity.TabInventarioActivity;
import com.example.f4sINV.R;

import java.util.HashMap;
import java.util.Objects;


public class UHFReadTagFragment4 extends Fragment {
    private static final String TAG = "UHFReadTagFragment4";

    private HashMap<String, Integer> mapTempDatas = new HashMap<String, Integer>();

    MyAdapter adapter;
    TextView tv_count;
    //TextView tv_total;

    ListView LvTags;
    private TabInventarioActivity mContext;
    private HashMap<String, String> map;

    //public static final String TAG_EPC = "tagEPC";
    public static final String TAG_EPC_TID = "tagEpcTID";
    public static final String TAG_COUNT = "tagCount";
    public static final String TAG_RSSI = "tagRssi";

    private static final String DIALOGO = "Dlg" + TAG;              //Nombre único
    private static final int REQUEST_result = 0;

    public UHFReadTagFragment4() {
        // Required empty public constructor
    }
    public static UHFReadTagFragment4 newInstance() {
        UHFReadTagFragment4 fragment = new UHFReadTagFragment4();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uhf_readtag_fragment4, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContext = (TabInventarioActivity) getActivity();

        tv_count = (TextView) getView().findViewById(R.id.tv_count);
        LvTags = (ListView) getView().findViewById(R.id.LvTags);
        adapter=new MyAdapter(mContext);
        LvTags.setOnItemClickListener((parent, view1, position, id) -> {
            adapter.setSelectItem(position);
            adapter.notifyDataSetInvalidated();
        });
        LvTags.setAdapter(adapter);

        tv_count.setText(mContext.tagList.size()+"");

        // Recupera el diciconario de EPCs que se han grabado en esta sesion o simplemente está vacio y fue creado por UHFReadTagFrangment2
        mapTempDatas = mContext.uhfInfo.getTempDatas();

        // Guarda valor
        SharedPreferences preferencias = mContext.getSharedPreferences(Parametros.PREFERENCIAS_app, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putString(mContext.pref_PANTA, "PANTA4");
        editor.apply();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String extra_accion = (String) data.getSerializableExtra(Parametros.EXTRA_ACCION);

        switch (Objects.requireNonNull(extra_accion)){
            case Parametros.DIALOGO_ALERT_CON_BTN_ATRAS:
                // Regresa a AcivityMain
                getActivity().onBackPressed();
                break;
            case Parametros.DIALOGO_ALERT_SIN_BTN_ATRAS:
                // No hacer nada
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Desacoplamiento del Fragment de la Actividad por invocación de otra pestaña
        // -> Si está escaneando RFID pararlo y salir a otra espetaña que se ha invocado
    }

    //Estrategia elegida para bloquear la acción “Botón atrás” si hay inventario en marcha y puede ELIMINAR objetos en curso
    //----------------------------------------------------------------------------------------------------------------------
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Si hay inventario en CURSO no se puede ir atrás ya que esto ELIMINARIA DATOS INVENTARIO
                if (mapTempDatas.isEmpty()){
                    this.setEnabled(false);
                    requireActivity().onBackPressed();
                } else {
                    // No se puede ir atrás si hay inventario en curso
                    mContext.playSound(5);
                    salidaAlerta("Atrás NO permitido",
                            "Abandonar solo si:",
                            "1.- Sesión de inventario guardada para seguir más tarde.\n\n" +
                                    "2.- Inventario finalizado y enviado al servidor.\n\n" +
                                    "3.- Sesión de inventario eliminada.",
                            R.drawable.baseline_warning_amber_24, Parametros.DIALOGO_ALERT_SIN_BTN_ATRAS);
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    // Clase Holder, inicializa antes selectItem=-1;
    private int  selectItem=-1;
    public final class ViewHolder {
        public TextView tvEPCTID;
        public TextView tvTagCount;
        public TextView tvTagRssi;
    }

    // Clase Adaptadora para el ListView
    public class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        public MyAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }
        public int getCount() {
            return mContext.tagList.size();
        }
        public Object getItem(int arg0) {
            return mContext.tagList.get(arg0);
        }
        public long getItemId(int arg0) {
            return arg0;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.listtag_items, null);
                holder.tvEPCTID = convertView.findViewById(R.id.TvTagUii);
                holder.tvTagCount = convertView.findViewById(R.id.TvTagCount);
                holder.tvTagRssi = convertView.findViewById(R.id.TvTagRssi);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tvEPCTID.setText(mContext.tagList.get(position).get(TAG_EPC_TID));
            holder.tvTagCount.setText(mContext.tagList.get(position).get(TAG_COUNT));
            holder.tvTagRssi.setText(mContext.tagList.get(position).get(TAG_RSSI));

            if (position == selectItem) {
                convertView.setBackgroundColor(mContext.getResources().getColor(R.color.lfile_colorPrimary));
            }
            else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
            }
            return convertView;
        }
        public  void setSelectItem(int select) {
            if(selectItem==select){
                selectItem=-1;
                mContext.uhfInfo.setSelectItem("");
                mContext.uhfInfo.setSelectIndex(selectItem);
            }else {
                selectItem = select;
                mContext.uhfInfo.setSelectItem(mContext.tagList.get(select).get(TAG_EPC_TID));
                mContext.uhfInfo.setSelectIndex(selectItem);
            }
        }
    }
    private void salidaAlerta(String argTitulo, String argSubTitulo, String argMsg, int image, String dialogo_alert) {
        MiDialogAlert dialog = MiDialogAlert.newInstance(argTitulo, argSubTitulo, argMsg, image, dialogo_alert);
        FragmentManager manager = getFragmentManager();
        dialog.setTargetFragment(UHFReadTagFragment4.this, REQUEST_result);
        dialog.show(manager, DIALOGO);
    }
}

