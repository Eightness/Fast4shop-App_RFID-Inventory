package com.example.f4sINV.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


import com.example.f4sINV.Parametros;
import com.example.f4sINV.R;

public class MiDialogFragment extends DialogFragment {
    // Parámetros del DialogFragment
    private static final String ARG_Fuente = "ARG_Fuente";
    private static final String ARG_Accion = "ARG_Accion";

    private String argFuente;
    private String argAccion;
    private String extra_editTexto;

    TextView txtTitulo,txtSubTitulo, txtMsg, txtEdit;
    EditText editTexto;
    LinearLayout layoutEdit;
    Button btSi, btNo;

    public static MiDialogFragment newInstance(String argFuente, String argAccion) {
        Bundle args = new Bundle();
        args.putString(ARG_Fuente, argFuente);
        args.putString(ARG_Accion, argAccion);
        MiDialogFragment fragment = new MiDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        argFuente = (String) getArguments().getSerializable(ARG_Fuente);
        argAccion = (String) getArguments().getSerializable(ARG_Accion);

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_fragment,null);

        txtTitulo = v.findViewById(R.id.txtTitulo);
        txtSubTitulo = v.findViewById(R.id.txtSubTitulo);
        txtMsg = v.findViewById(R.id.txtMsg);
        txtEdit= v.findViewById(R.id.txtEdit);
        btSi = v.findViewById(R.id.btSi);
        btNo = v.findViewById(R.id.btNo);
        layoutEdit = v.findViewById(R.id.layoutEdit);

        // Campo
        editTexto= v.findViewById(R.id.edit_texto);

        switch (argFuente){
            case "UHFReadTagFragment5":
                switch (argAccion){
                    case Parametros.DIALOGO_ACCION_SAVE:
                        txtTitulo.setText("Guardar sesión");
                        txtSubTitulo.setText("¿Quiere guardar  las lecturas de etiquetas de esta sesión.?");
                        txtMsg.setText("- Si lo confirma podrá seguir con el inventario en otra sesión sin perder estas lecturas.\n\n" +
                                "- Se guardan en la memoria del dispositivo.");
                        txtEdit.setText("Observación de esta sesión");
                        layoutEdit.setVisibility(View.VISIBLE);
                        break;
                    case Parametros.DIALOGO_ACCION_FTP:
                        txtTitulo.setText("Envio de Inventario");
                        txtSubTitulo.setText("¿Quiere enviar el inventario con todas sus sesiones de lectura?");
                        txtMsg.setText("- Si lo confirma se envía al servidor para seguir alli con el proceso de actualización del inventario.\n\n" +
                                "- Tras el envío se borran todas las lecturas del inventario en curso.");
                        txtEdit.setText("");
                        layoutEdit.setVisibility(View.INVISIBLE);
                        break;
                }
                break;
            case "UHFReadTagFragment2":
                // case "BtClear":
                if (argAccion.equals(Parametros.DIALOGO_ACCION_CLEAR)) {
                    txtTitulo.setText("Eliminar sesión");
                    txtSubTitulo.setText("¿ELIMINAR las etiquetas de esta sesión?");
                    txtMsg.setText("- Se eliminan las lecturas de la sesión y se mantienen las lecturas de las anteriores.\n\n" +
                            "- Para eliminar todo el Inventario en Curso vaya a Opciones.");
                    txtEdit.setText("Observación de la eliminación");
                    layoutEdit.setVisibility(View.VISIBLE);
                }
                break;
        }

        // Botón de Aceptación
        btSi.setOnClickListener(v1 -> {

            extra_editTexto = editTexto.getText().toString();

            sendResult(Activity.RESULT_OK, argAccion, extra_editTexto);
            dismiss(); // Abandonar el DialogAlert
        });

        // Botón de Cancelación
        btNo.setOnClickListener(v12 -> {

            //sendResult(Activity.RESULT_CANCELED, 0);
            sendResult(Activity.RESULT_CANCELED, argAccion, "");
            dismiss(); // Abandonar el DialogAlert
        });
        return new AlertDialog.Builder(getActivity()).setView(v).create();
    }

    private  void sendResult(int resultCode, String accion, String campo){
        if (getTargetFragment()==null){
            return;
        }
        Intent intent = new Intent();

        intent.putExtra(Parametros.EXTRA_ACCION, accion);
        intent.putExtra(Parametros.EXTRA_EDIT_TEXT, campo);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
