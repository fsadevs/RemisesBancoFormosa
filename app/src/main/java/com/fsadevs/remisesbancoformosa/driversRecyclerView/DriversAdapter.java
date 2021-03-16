package com.fsadevs.remisesbancoformosa.driversRecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fsadevs.remisesbancoformosa.R;
import com.fsadevs.remisesbancoformosa.RidePlannerActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriversAdapter extends RecyclerView.Adapter<DriversAdapter.ViewHolder> implements View.OnClickListener {

    private int resource;
    private ArrayList<DriverObject> objectList;
    private String driverID, driverPicURL;
    private View.OnClickListener listener;

    public DriversAdapter(ArrayList<DriverObject> objectList, int resource){
        this.objectList = objectList;
        this.resource = resource;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(resource, viewGroup,false);
        itemView.setOnClickListener(this);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int index) {
        DriverObject driverObject = objectList.get(index);
        viewHolder.driverName.setText(driverObject.getDriverName());
        viewHolder.driverProveedor.setText(driverObject.getDriverProv());
        driverPicURL = driverObject.getDriverPicURL();
        if (!driverPicURL.equals("")) {
            Picasso.get().load(driverPicURL).into(viewHolder.driverPic);
        }
        driverID = driverObject.getDriverID();
    }

    @Override
    public int getItemCount() {
        return objectList.size();
    }

    public void setOnClickListener(View.OnClickListener listener){
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        if (listener!=null){
            listener.onClick(view);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView driverName, driverProveedor;
        private CircleImageView driverPic;
        private View itemView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            driverName = itemView.findViewById(R.id.txt_rv_drivername);
            driverProveedor = itemView.findViewById(R.id.txt_rv_proveedor);
            driverPic = itemView.findViewById(R.id.rv_driver_profileimage);
        }

    }
}
