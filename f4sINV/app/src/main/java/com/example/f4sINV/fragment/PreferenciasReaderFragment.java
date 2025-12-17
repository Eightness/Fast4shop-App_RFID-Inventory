package com.example.f4sINV.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import com.example.f4sINV.R;
import com.example.f4sINV.activity.MainActivity;

public class PreferenciasReaderFragment extends PreferenceFragmentCompat {
    private MainActivity mContext;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferencias_reader, rootKey);
        mContext = (MainActivity) getActivity();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
