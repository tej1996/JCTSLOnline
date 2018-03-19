package com.jctsl.jctslonline;

/**
 * Created by Tejasv on 07-01-2018.
 */

public class NearbyBusData {

    String busname;
    String busnumber;
    String cusername;
    String srcstationname;
    String deststationname;
    String totalfare;
    String crowd;
    String arrtime;

    public NearbyBusData() {
    }

    public NearbyBusData(String busname, String busnumber, String cusername, String srcstationname, String deststationname, String totalfare, String crowd,String arrtime) {
        this.busname = busname;
        this.busnumber = busnumber;
        this.cusername = cusername;
        this.srcstationname = srcstationname;
        this.deststationname = deststationname;
        this.totalfare = totalfare;
        this.crowd = crowd;
        this.arrtime = arrtime;
    }

    public String getArrtime() {
        return arrtime;
    }

    public String getTotalfare() {
        return totalfare;
    }

    public String getCrowd() {
        return crowd;
    }

    public String getBusname() {
        return busname;
    }

    public String getBusnumber() {
        return busnumber;
    }

    public String getCusername() {
        return cusername;
    }

    public String getSrcstationname() {
        return srcstationname;
    }

    public String getDeststationname() {
        return deststationname;
    }
}

