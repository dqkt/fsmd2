package com.example.dq.fsmd2;

import java.io.Serializable;
import java.util.Date;
import java.util.ArrayList;

/**
 * Created by DQ on 12/16/2017.
 */

public class Item implements Serializable {
    private static final String defaultName = "ITEM #";

    private static int numItems = 0;
    private static int numSafe = 0;
    private static int numVibrating = 0;
    private static int numLost = 0;

    public static int NO_STATUS        = -1;
    public static int SAFE_STATUS      = 0;
    public static int VIBRATING_STATUS = 1;
    public static int LOST_STATUS      = 2;

    private String name;
    private String ip;
    private Date dateAdded;

    private int status;
    private ArrayList<MonitorData> dataList;

    public Item() {
        this.name = defaultName + String.valueOf(numItems);
        setStatus(NO_STATUS);
        this.dataList = new ArrayList<MonitorData>();
        numItems++;
        this.dateAdded = new Date();
    }

    public Item(String name, String ip) {
        this.name = name;
        this.ip = ip;
        this.status = NO_STATUS;
        this.dataList = new ArrayList<MonitorData>();
        numItems++;
        this.dateAdded = new Date();
    }

    public Item(String name, String ip, Date dateAdded) {
        this.name = name;
        this.ip = ip;
        this.status = NO_STATUS;
        this.dataList = new ArrayList<MonitorData>();
        numItems++;
        this.dateAdded = dateAdded;
    }

    public Item(Item other) {
        this.name = new String(other.name);
        this.ip = new String(other.ip);
        this.status = other.status;
        this.dataList = new ArrayList<MonitorData>(other.dataList);
        this.dateAdded = new Date(other.dateAdded.getTime());
    }

    public void setName(String name) { this.name = name; }
    public String getName() { return name; }
    public void setIP(String ip) { this.ip = ip; }
    public String getIP() { return ip; }
    public Date getDateAdded() { return dateAdded; }
    public void setStatus(int status) {
        if (this.status == SAFE_STATUS) {
            numSafe--;
        }
        else if (this.status == VIBRATING_STATUS) {
            numVibrating--;
        }
        else if (this.status == LOST_STATUS) {
            numLost--;
        }

        this.status = status;

        if (this.status == SAFE_STATUS) {
            numSafe++;
        }
        else if (this.status == VIBRATING_STATUS) {
            numVibrating++;
        }
        else if (this.status == LOST_STATUS) {
            numLost++;
        }
    }
    public int getStatus() { return status; }
    public void addMonitorData(MonitorData data) { dataList.add(data); }

    public ArrayList<MonitorData> getMonitorDataList() { return dataList; }
    public static void setNumSafe(int newNumSafe) { numSafe = newNumSafe; }
    public static void setNumVibrating(int newNumVibrating) { numVibrating = newNumVibrating; }

    public static void setNumLost(int newNumLost) { numLost = newNumLost; }
    public static int getNumSafe() { return numSafe; }
    public static int getNumVibrating() { return numVibrating; }

    public static int getNumLost() { return numLost; }
}