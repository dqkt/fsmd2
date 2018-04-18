package com.example.dq.fsmd2;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.Update;
import android.util.Log;

import java.io.Serializable;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by DQ on 12/16/2017.
 */

@Entity
@TypeConverters(DateConverter.class)
public class Item implements Serializable {

    private static final String DEFAULT_NAME = "ITEM #";
    private static final String DEFAULT_IP = "0.0.0.0";

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

    public Item() {
        this.name = DEFAULT_NAME;
        this.ip = "0.0.0.0";
        this.status = NO_STATUS;
        this.dateAdded = new Date();
    }

    public Item(String name, String ip) {
        this.name = name;
        this.ip = ip;
        this.status = NO_STATUS;
        this.dateAdded = new Date();
    }

    public Item(String name, String ip, Date dateAdded) {
        this.name = name;
        this.ip = ip;
        this.status = NO_STATUS;
        this.dateAdded = dateAdded;
    }

    public Item(Item other) {
        this.name = new String(other.name);
        this.ip = new String(other.ip);
        this.status = other.status;
        this.dateAdded = new Date(other.dateAdded.getTime());
    }

    public int getItemID() {
        return itemID;
    }
    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public void setName(String name) { this.name = name; }
    public String getName() { return name; }
    public void setIp(String ip) { this.ip = ip; }
    public String getIp() { return ip; }
    public Date getDateAdded() { return dateAdded; }
    public void setDateAdded(Date dateAdded) { this.dateAdded = dateAdded; }
    public void setStatus(int status) {
        this.status = status;
    }
    public int getStatus() { return status; }

    public static int getStatusColor(int status) {
        switch (status) {
            case NO_STATUS:
                return R.color.noStatusColor;
            case SAFE_STATUS:
                return R.color.safeStatusColor;
            case VIBRATING_STATUS:
                return R.color.vibratingStatusColor;
            case LOST_STATUS:
                return R.color.lostStatusColor;
        }
        return R.color.noStatusColor;
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

        @Update
        void update(Item item);

        @Query("SELECT COUNT(1) FROM item WHERE ip = :ip")
        Integer itemIpInUse(String ip);

        @Query("SELECT * FROM item")
        LiveData<List<Item>> getAllItems();

        @Query("SELECT * FROM item WHERE itemID = :itemID")
        LiveData<Item> getItem(int itemID);

        @Query("SELECT COUNT(*) from item")
        int getNumItems();

        @Query("SELECT COUNT(*) from item WHERE status = :status")
        int getNumItemsWithStatus(int status);
    }
}
