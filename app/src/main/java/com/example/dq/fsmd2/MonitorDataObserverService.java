package com.example.dq.fsmd2;

import android.app.Activity;
import android.app.IntentService;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class MonitorDataObserverService extends IntentService {

    private boolean error;
    private String responseString;
    private SimpleDateFormat formatter;

    public static final int DATA_INTERVAL = 16000;

    public static final String FSMD_WLAN = "FSMD WLAN";
    public static final String HUB_IP_ADDRESS = "192.168.1.2";
    public static final String HUB_DIRECTORY = "data-request";

    private String ssid;

    private String dataPageUrl;
    private Context context;
    private Callback activity;

    public MonitorDataObserverService() {
        super("MonitorDataObserverService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        formatter = new SimpleDateFormat("HHmmssddMMyy", Locale.US);

        ssid = "";

        dataPageUrl = "http://" + HUB_IP_ADDRESS + "/" + HUB_DIRECTORY;
        context = this.getApplicationContext();
    }

    @Override
    public void onDestroy() {
        Log.d("DEBUG", "On destroy service");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class LocalBinder extends Binder {
        public MonitorDataObserverService getServiceInstance() {
            return MonitorDataObserverService.this;
        }
    }

    public void registerClient(Activity activity){
        this.activity = (Callback) activity;
    }

    public interface Callback {
        void addMonitorData(MonitorData monitorData);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (manager.isWifiEnabled()) {
            WifiInfo wifiInfo = manager.getConnectionInfo();
            if (wifiInfo != null) {
                NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
                if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                    ssid = wifiInfo.getSSID().replaceAll("^\\s+|\\s+$", "").replaceAll("\"", "");
                }
            }
        }
        error = false;
        final Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.d("DEBUG", "checking if correct network");
                if (ssid.equals(FSMD_WLAN)) {
                    Log.d("DEBUG", "connected to correct network");
                    try {
                        URLConnection connection = new URL(dataPageUrl).openConnection();

                        BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                        StringBuilder sb = new StringBuilder();
                        String output;
                        while ((output = br.readLine()) != null) {
                            sb.append(output);
                            sb.append(" ");
                        }
                        responseString = sb.toString();
                    } catch (Exception e) {
                        error = true;
                        responseString = e.getLocalizedMessage();
                    }

                    if (!error) {
                        Log.d("DEBUG", "no error");
                        String result = responseString.replace("HTTP/1.1 200 OKContent-type:text/html", "");
                        Log.d("DEBUG", responseString);
                        Scanner dataScanner = new Scanner(result);

                        boolean isData = false;

                        byte itemID = 0;
                        float peakX = 0, peakY = 0, peakZ = 0;
                        float avgX = 0, avgY = 0, avgZ = 0;
                        float latitude = 0, longitude = 0;
                        int time = 0, date = 0;
                        Date dateAdded;
                        byte valid = 0;

                        if (dataScanner.hasNextByte()) {
                            itemID = dataScanner.nextByte();
                            isData = true;
                        }

                        if (isData) {
                            Log.d("DEBUG", "after 1");
                            if (dataScanner.hasNextFloat()) {
                                avgX = dataScanner.nextFloat();
                            }
                            Log.d("DEBUG", "after 2");
                            if (dataScanner.hasNextFloat()) {
                                peakX = dataScanner.nextFloat();
                            }
                            Log.d("DEBUG", "after 3");
                            if (dataScanner.hasNextFloat()) {
                                avgY = dataScanner.nextFloat();
                            }
                            Log.d("DEBUG", "after 4");
                            if (dataScanner.hasNextFloat()) {
                                peakY = dataScanner.nextFloat();
                            }
                            Log.d("DEBUG", "after 5");
                            if (dataScanner.hasNextFloat()) {
                                avgZ = dataScanner.nextFloat();
                            }
                            Log.d("DEBUG", "after 6");
                            if (dataScanner.hasNextFloat()) {
                                peakZ = dataScanner.nextFloat();
                            }

                            Log.d("DEBUG", "after 7");
                            if (dataScanner.hasNextFloat()) {
                                latitude = dataScanner.nextFloat();
                            }
                            Log.d("DEBUG", "after 8");
                            if (dataScanner.hasNextFloat()) {
                                longitude = dataScanner.nextFloat();
                            }

                            Log.d("DEBUG", "after 9");
                            if (dataScanner.hasNextFloat()) {
                                time = (int) dataScanner.nextFloat();
                            }
                            Log.d("DEBUG", "after 10");
                            if (dataScanner.hasNextInt()) {
                                date = (int) dataScanner.nextFloat();
                            }

                            Log.d("DEBUG", "after 11");
                            if (dataScanner.hasNextByte()) {
                                valid = dataScanner.nextByte();
                            }

                            Log.d("DEBUG", "after valid");

                            if (valid == 1) {
                                dateAdded = new Date(formatter.format(String.valueOf(time) + String.valueOf(date)));
                            } else {
                                dateAdded = new Date();
                            }

                            Log.d("DEBUG", "after date");
                            MonitorData monitorData = new MonitorData(itemID, peakX, peakY, peakZ, avgX, avgY, avgZ, latitude, longitude, dateAdded);
                            monitorData.setItemID(itemID);
                            Log.d("DEBUG", "after making data");
                            activity.addMonitorData(monitorData);
                            Log.d("DEBUG", "after adding to database");
                        } else {
                            Log.d("DEBUG", "not data");
                        }
                    } else {
                        Log.d("DEBUG", responseString);
                    }
                }
            }
        }, 0, DATA_INTERVAL / 2);
    }
}