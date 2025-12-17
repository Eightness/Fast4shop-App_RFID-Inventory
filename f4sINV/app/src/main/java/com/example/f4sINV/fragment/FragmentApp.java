package com.example.f4sINV.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.f4sINV.R;

public class FragmentApp extends Fragment implements View.OnClickListener{

    private static final String TAG = "FragmentApp";
    private View view;
    private TextView tv_appVersion;
    private ImageButton imgLogo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view=inflater.inflate(R.layout.fragment_app, container, false);
        Log.i(TAG, "onCreateView");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgLogo = getView().findViewById(R.id.imgbt_logof4s);
        imgLogo.setOnClickListener(this);
        tv_appVersion = getView().findViewById(R.id.tv_appVersion);
        tv_appVersion.setText(getVersionName());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView()");
    }

    @Override
    public void onClick(View v) {

        String url="";
        if (v.getId() == R.id.imgbt_logof4s){
            url="http://www.fast4shop.com";
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    private String getVersionName(){
        try {
            return this.getContext().getPackageManager().getPackageInfo(this.getContext().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }
}