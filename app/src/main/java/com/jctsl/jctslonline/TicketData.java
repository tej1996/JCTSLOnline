package com.jctsl.jctslonline;

/**
 * Created by Tejasv on 02-01-2018.
 */

public class TicketData {

    String datetime;
    String busname;
    String src;
    String dest;
    String qty;
    String totalfare;
    String qrcode;

    public TicketData() {
    }

    public TicketData(String datetime, String busname, String src, String dest, String qty, String totalfare, String qrcode) {
        this.datetime = datetime;
        this.busname = busname;
        this.src = src;
        this.dest = dest;
        this.qty = qty;
        this.totalfare = totalfare;
        this.qrcode = qrcode;
    }
}
