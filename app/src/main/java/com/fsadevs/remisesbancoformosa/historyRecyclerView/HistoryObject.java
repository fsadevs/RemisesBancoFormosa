package com.fsadevs.remisesbancoformosa.historyRecyclerView;


public class HistoryObject {

    private String history_timestart, history_driver_name, history_startlocation, history_destination, history_distance;

    public HistoryObject(){}

    public HistoryObject(String history_timestart, String history_driver_name, String history_startlocation, String history_destination, String history_distance) {
        this.history_timestart = history_timestart;
        this.history_driver_name = history_driver_name;
        this.history_startlocation = history_startlocation;
        this.history_destination = history_destination;
        this.history_distance = history_distance;
    }

    public String getHistory_timestart() {
        return history_timestart;
    }

    public void setHistory_timestart(String history_timestart) {
        this.history_timestart = history_timestart;
    }

    public String getHistory_driver_name() {
        return history_driver_name;
    }

    public void setHistory_driver_name(String history_driver_name) {
        this.history_driver_name = history_driver_name;
    }

    public String getHistory_startlocation() {
        return history_startlocation;
    }

    public void setHistory_startlocation(String history_startlocation) {
        this.history_startlocation = history_startlocation;
    }

    public String getHistory_destination() {
        return history_destination;
    }

    public void setHistory_destination(String history_destination) {
        this.history_destination = history_destination;
    }

    public String getHistory_distance() {
        return history_distance;
    }

    public void setHistory_distance(String history_distance) {
        this.history_distance = history_distance;
    }
}

