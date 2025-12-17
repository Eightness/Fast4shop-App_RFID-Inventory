package com.example.f4sINV.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import com.example.f4sINV.R;
import com.example.f4sINV.activity.MainActivity;

public class PreferenciasFragment extends PreferenceFragmentCompat {
    private MainActivity mContext;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferencias, rootKey);
        mContext = (MainActivity) getActivity();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
