package com.fsadevs.remisesbancoformosa.driversRecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriverObject {

    private String driverName,driverProv, driverPicURL, driverID;

    public DriverObject(String driverName, String driverProv, String driverPicURL, String driverID) {
        this.driverName = driverName;
        this.driverProv = driverProv;
        this.driverPicURL = driverPicURL;
        this.driverID = driverID;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getDriverProv() {
        return driverProv;
    }

    public String getDriverPicURL() {
        return driverPicURL;
    }

    public String getDriverID() {
        return driverID;
    }
}
