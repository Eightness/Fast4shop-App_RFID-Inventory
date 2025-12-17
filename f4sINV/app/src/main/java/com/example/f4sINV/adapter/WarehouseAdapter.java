package com.example.f4sINV.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.f4sINV.R;
import com.example.f4sINV.model.Warehouse;

import java.util.List;

public class WarehouseAdapter extends RecyclerView.Adapter<WarehouseAdapter.ViewHolder> {

    public interface OnWarehouseActionListener {
        void onDeleteClicked(int position);
        void onItemClicked(int position);
    }

    private List<Warehouse> warehouseList;
    private OnWarehouseActionListener listener;
    private String activeWarehouseCode; // código del almacén activo

    public WarehouseAdapter(List<Warehouse> warehouseList, OnWarehouseActionListener listener) {
        this.warehouseList = warehouseList;
        this.listener = listener;
    }

    // Método para establecer el código activo desde la actividad
    public void setActiveWarehouse(String codigo) {
        this.activeWarehouseCode = codigo;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvDescription;
        ImageView btnDelete;
        LinearLayout itemContainer;

        public ViewHolder(View v) {
            super(v);
            tvCode = v.findViewById(R.id.tvWarehouseCode);
            tvDescription = v.findViewById(R.id.tvWarehouseDescription);
            btnDelete = v.findViewById(R.id.btnDeleteWarehouse);
            itemContainer = v.findViewById(R.id.itemContainer);
        }
    }

    @NonNull
    @Override
    public WarehouseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_warehouse, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull WarehouseAdapter.ViewHolder holder, int position) {
        Warehouse warehouse = warehouseList.get(position);

        holder.tvCode.setText(warehouse.getCodigo());
        holder.tvDescription.setText(warehouse.getDescripcion());

        // Selección visual (fondo diferente si es el activo)
        if (warehouse.getCodigo().equals(activeWarehouseCode)) {
            holder.itemContainer.setBackgroundColor(Color.parseColor("#D0E8FF")); // Azul claro
        } else {
            holder.itemContainer.setBackgroundColor(Color.WHITE);
        }

        // Clic para selección
        holder.itemContainer.setOnClickListener(v -> {
            if (listener != null) listener.onItemClicked(position);
        });

        // Clic para eliminar
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClicked(position);
        });
    }

    @Override
    public int getItemCount() {
        return warehouseList.size();
    }

    public void updateList(List<Warehouse> newList) {
        this.warehouseList = newList;
        notifyDataSetChanged();
    }
}
