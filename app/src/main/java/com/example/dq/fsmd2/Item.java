package com.example.dq.fsmd2;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;

import java.io.Serializable;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by DQ on 12/16/2017.
 */

@Entity
public class Item implements Serializable {
    private static final String defaultName = "ITEM #";

    private static int numItems = 0;
    private static int numSafe = 0;
    private static int numVibrating = 0;
    private static int numLost = 0;

    public static final int NO_STATUS        = -1;
    public static final int SAFE_STATUS      = 0;
    public static final int VIBRATING_STATUS = 1;
    public static final int LOST_STATUS      = 2;

    public static final int LOST_THRESHOLD = 130;
    public static final int VIB_THRESHOLD = 100;

    @PrimaryKey(autoGenerate = true)
    private int itemID;
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

    public static int getStatusColor(int status) {
        switch (status) {
            case SAFE_STATUS:
                return R.color.safeStatusColor;
            case VIBRATING_STATUS:
                return R.color.vibratingStatusColor;
            case LOST_STATUS:
                return R.color.lostStatusColor;
        }
        return R.color.safeStatusColor;
    }

    public static int determineStatus(double vibLevel) {
        if (vibLevel > LOST_THRESHOLD) {
            return LOST_STATUS;
        } else if (vibLevel > VIB_THRESHOLD) {
            return VIBRATING_STATUS;
        } else {
            return SAFE_STATUS;
        }
    }

    @Dao
    public interface ItemDao {
        @Insert
        void insertAll(Item... items);

        @Delete
        void delete(Item item);

        @Query("SELECT * FROM item")
        LiveData<List<Item>> getAllItems();
    }
}
