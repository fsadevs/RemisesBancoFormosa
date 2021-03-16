package com.fsadevs.remisesbancoformosa.historyRecyclerView;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.fsadevs.remisesbancoformosa.R;

import java.util.ArrayList;


public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private int resource;
    private ArrayList<HistoryObject> objectList;

    public HistoryAdapter(ArrayList<HistoryObject> objectList, int resource){
        this.objectList = objectList;
        this.resource = resource;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(resource, viewGroup,false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int index) {
        HistoryObject historyObject = objectList.get(index);
        viewHolder.txtTime.setText(historyObject.getHistory_timestart());
        viewHolder.txtDriverName.setText(historyObject.getHistory_driver_name());
        viewHolder.txtStartLocation.setText(historyObject.getHistory_startlocation());
        viewHolder.txtDestination.setText(historyObject.getHistory_destination());
        viewHolder.txtDistance.setText(historyObject.getHistory_distance());

    }

    @Override
    public int getItemCount() {
        return objectList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView txtTime,txtDriverName,txtStartLocation,txtDestination,txtDistance;

        public View itemView;

        public ViewHolder(View itemView){
            super(itemView);

            txtTime = itemView.findViewById(R.id.history_timestart);
            txtDriverName = itemView.findViewById(R.id.history_driver_name);
            txtStartLocation = itemView.findViewById(R.id.history_start_location);
            txtDestination = itemView.findViewById(R.id.history_destino);
            txtDistance = itemView.findViewById(R.id.history_distance);

        }
    }

}